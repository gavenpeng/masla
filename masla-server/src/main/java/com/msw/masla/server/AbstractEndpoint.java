package com.msw.masla.server;


import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.protocol.http.netty.config.NettyConfig;
import com.msw.masla.protocol.http.netty.util.SystemUtil;
import com.msw.masla.server.dispatch.MaslaDispatch;
import com.msw.masla.server.processor.AcceptQueue;
import com.msw.masla.server.processor.MaslaThreadPoolExecutor;
import com.msw.masla.server.processor.WorkerThreadFactory;
import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Gavin.peng on 2023/9/25.
 */
public abstract class AbstractEndpoint {

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractEndpoint.class);

    protected final static int DEFAULT_PORT = 80;
    protected final static int DEFAULT_SSL_PORT = 443;
    protected final static int DEFAULT_BACK_LOG = 10240;
    //session 在队列的等待时间,如果超过该时间，说明队列已经满了，则自动超时
    protected static long SESSION_ENQUEUE_TIME_OUT = 4000;
    //超时检查线程运行间隔
    protected static long ASYNC_TIME_OUT_INTERVAL = 2000;
    private final static int MAX_QUEUE_SIZE = 50000;
    private final static int DEFAULT_HANDLE_SIZE = 20;
    private final static int DEFAULT_PRIORITY_HANDLE_SIZE = 50;

    protected enum BindState {
        UNBOUND, BOUND_ON_INIT, BOUND_ON_START
    }


    //单机最大连接数
    private int maxChannel = 10240;
    //单个app最大连接数
    private int maxAppChannel = 30000;
    //优先级线程组个数
    private int priorityHandlers = DEFAULT_PRIORITY_HANDLE_SIZE;
    //backup线程组个数
    private int backupHandlers = DEFAULT_HANDLE_SIZE;

    //一个连接可以重用的最大次数
    private int maxKeepAliveRequests = 10240;

    protected int acceptThreadCnt = 2;
    protected int serverIOThreadCnt = 50;

    private  int acceptQueueSize = 200;
    private  int priorityQueueSize = MAX_QUEUE_SIZE;
    private  int backupQueueSize = MAX_QUEUE_SIZE;
    private  int workerThreadCoreSize = 50;
    private  int workerThreadMaxSize = 200;

    //http1
    protected int sessionIdleTime = 20000;
    //http2
    protected int sessionTimeout = 120000;
    protected int maxContentLength = 1024*1024;


    protected int port = DEFAULT_SSL_PORT;
    protected int sslPort = DEFAULT_SSL_PORT;

    //LINUX tcp 底层会起OS和应用层的最小值
    protected int backLog = DEFAULT_BACK_LOG;

    private static boolean doInit;


    private ConcurrentMap<Long, ConcurrentSet<String>> appSessionsMap = new ConcurrentHashMap<Long, ConcurrentSet<String>>();
    private ConcurrentMap<Long, AtomicInteger> appSessionsNums = new ConcurrentHashMap<Long,AtomicInteger>();

    protected ServerBootstrap bootstrap;
    protected static EventLoopGroup acceptorGroup;
    protected static EventLoopGroup ioGroup;

    protected Class<? extends ServerChannel> serverSocketChannelClass;


    private MaslaDispatch maslaDispatch;

    /**
     * Allow a customized the server header for the tin-foil hat folks.
     */
    private String server = null;


    protected volatile BindState bindState = BindState.UNBOUND;
    private boolean bindOnInit = false;

    private Executor executor = null;

    private AcceptQueue taskqueue;

    //backup queue,enqueue backup queue while task queue is full
    private BlockingQueue backupQueue;

    private BlockingQueue priorityQueue;

    private ConcurrentMap<String, IOSession> sessions;


    public void setExecutor(Executor executor) {
        this.executor = executor;
        //this.internalExecutor = (executor == null);
    }


    public MaslaDispatch getMaslaDispatch() {
        return maslaDispatch;
    }

    public void setMaslaDispatch(MaslaDispatch maslaDispatch) {
        this.maslaDispatch = maslaDispatch;
    }

    public Executor getExecutor() { return executor; }


    public abstract void bind() throws Exception;

    public abstract void unbind() throws Exception;

    public abstract void startInternal() throws Exception;

    public abstract void stopInternal() throws Exception;


    public void createExecutor() {
        //internalExecutor = true;
        this.backupQueue = new LinkedBlockingDeque(this.backupQueueSize);
        this.priorityQueue = new LinkedBlockingDeque(this.priorityQueueSize);
        taskqueue = new AcceptQueue(this.acceptQueueSize);
        WorkerThreadFactory tf = new WorkerThreadFactory("MaslaWorkerThread-", false, 1);
        executor = new MaslaThreadPoolExecutor(workerThreadCoreSize, workerThreadMaxSize, 60, TimeUnit.SECONDS,taskqueue, tf);
        taskqueue.setParent( (MaslaThreadPoolExecutor) executor);
    }

    private void initConfig(NettyConfig config){

        if(config.getPort() >0){
            this.port = config.getPort();
        }

        if (config.getPort() >0) {
            this.sslPort = config.getSslPort();
        }
        if(config.getBacklog() >0){
            this.backLog = config.getBacklog();
        }
        if(config.getMaxSession()>0){
            this.maxChannel = config.getMaxSession();
        }
        if(config.getMaxAppSession()>0){
            this.maxAppChannel = config.getMaxAppSession();
        }

        if(config.getPriorityHandlers()>0){
            this.priorityHandlers = config.getPriorityHandlers();
        }

        if(config.getBackupHandlers()>0){
            this.backupHandlers = config.getBackupHandlers();
        }

        if(config.getMaxKeepAliveRequests() >0){
            this.maxKeepAliveRequests = config.getMaxKeepAliveRequests();
        }
        if(config.getAcceptThreadCnt() >0){
            this.acceptThreadCnt = config.getAcceptThreadCnt();
        }
        if(config.getServerIOThreadCnt() >0){
            this.serverIOThreadCnt = config.getServerIOThreadCnt();
        }

        if(config.getAcceptQueueSize() >0){
            this.acceptQueueSize = config.getAcceptQueueSize();
        }

        if(config.getPriorityQueueSize() >0){
            this.priorityQueueSize = config.getPriorityQueueSize();
        }

        if(config.getBackupQueueSize() >0){
            this.backupQueueSize = config.getBackupQueueSize();
        }

        if(config.getWorkerThreadCoreSize() >0){
            this.workerThreadCoreSize = config.getWorkerThreadCoreSize();
        }
        if(config.getWorkerThreadMaxSize() >0){
            this.workerThreadMaxSize = config.getWorkerThreadMaxSize();
        }

        if(config.getSessionIdleTime() >0){
            this.sessionIdleTime = config.getSessionIdleTime();
        }

        if(config.getMaxContentLength() >0){
            this.maxContentLength = config.getMaxContentLength();
        }

        if(config.getH2SessionTimeout() >0){
            this.sessionTimeout = config.getH2SessionTimeout();
        }

        this.sessions = new ConcurrentHashMap<String, IOSession>();

    }

    public void init(NettyConfig nettyConfig) throws Exception {
        if(doInit){
            return;
        }
        initConfig(nettyConfig);
        serverSocketChannelClass = null;
        try {

            if (SystemUtil.canUseNative()) {
                acceptorGroup = new EpollEventLoopGroup(this.acceptThreadCnt,
                        new DefaultThreadFactory("ServerAcceptorEpollEventLoopGroup"));
                ioGroup = new EpollEventLoopGroup(this.serverIOThreadCnt,
                        new DefaultThreadFactory("ServerNIOEpollEventLoopGroup"));
                serverSocketChannelClass = EpollServerSocketChannel.class;
            } else {
                acceptorGroup = new NioEventLoopGroup(this.acceptThreadCnt,
                        new DefaultThreadFactory("ServerAcceptorEventLoopGroup"));
                ioGroup = new NioEventLoopGroup(this.serverIOThreadCnt,
                        new DefaultThreadFactory("ServerNIOEventLoopGroup"));

                serverSocketChannelClass = NioServerSocketChannel.class;
            }
        } catch (AbstractMethodError e) {
            LOG.error("Masla init event group failed:",e);
            throw e;
        }
        if (bindOnInit) {
            bind();
            bindState = BindState.BOUND_ON_INIT;
        }else{
            bindState = BindState.UNBOUND;
        }
        doInit = true;


    }



    public final void start() throws Exception {

        startInternal();
        if (bindState == BindState.UNBOUND) {
            bind();
            bindState = BindState.BOUND_ON_START;
        }
        //addShutdownHook();
    }

    public void remveSession(String sessionKey, IOSession session){
        if(session == null){
            return;
        }

    }


    public void clearAllSession(){
        this.appSessionsNums.clear();
    }

    public void shutdownExecutor() {
        Executor executor = this.executor;
        if (executor != null) {
            this.executor = null;
            if (executor instanceof ThreadPoolExecutor) {
                //this is our internal one, so we need to shut it down
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
                tpe.shutdownNow();
                try {
                    tpe.awaitTermination(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // Ignore
                }

                LOG.warn("Masla worker thread exit!!!");

                AcceptQueue queue = (AcceptQueue) tpe.getQueue();
                queue.setParent(null);
            }else{
                LOG.warn("Masla worker thread exit!!!");
            }
        }
    }

    protected void shutdownEventLoopGroup(){

        if(acceptorGroup != null){
            acceptorGroup.shutdownGracefully().awaitUninterruptibly(3000);
        }
        if(ioGroup != null){
            ioGroup.shutdownGracefully().awaitUninterruptibly(3000);
        }

        LOG.warn("Masla server io thread exit...");

    }

    public int getMaxChannel() {
        return maxChannel;
    }

    public int getMaxAppChannel() {
        return maxAppChannel;
    }

    public void setMaxChannel(int maxChannel) {
        this.maxChannel = maxChannel;
    }

    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }

    public void setMaxKeepAliveRequests(int maxKeepAliveRequests) {
        this.maxKeepAliveRequests = maxKeepAliveRequests;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public AcceptQueue getTaskqueue(){
        return this.taskqueue;
    }

    public BlockingQueue getBackupqueue(){
        return this.backupQueue;
    }

    public BlockingQueue getPriorityQueue(){
        return this.priorityQueue;
    }


    public int getPriorityHandlers() {
        return priorityHandlers;
    }

    public int getBackupHandlers() {
        return backupHandlers;
    }

    public ConcurrentMap<String, IOSession> getSessions() {
        return sessions;
    }

    public int getSessionTimeout(){
        int timeout = MaslaSpringContextUtil.getMaslaConfConfigBean().getServerSessionTimeout();
        if(timeout > 0){
            this.sessionTimeout = timeout;
        }
        return sessionTimeout;
    }
}
