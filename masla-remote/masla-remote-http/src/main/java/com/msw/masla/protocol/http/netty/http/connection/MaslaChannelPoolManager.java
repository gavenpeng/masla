package com.msw.masla.protocol.http.netty.http.connection;

import com.google.common.base.Preconditions;
import com.msw.masla.common.pojo.ServiceApp;
import com.msw.masla.protocol.http.netty.config.NettyConfig;
import com.msw.masla.protocol.http.netty.factory.EventLoopFactory;
import com.msw.masla.protocol.http.netty.factory.MaslaEventLoopGroupFactory;
import com.msw.masla.protocol.http.netty.http.HostInstance;
import com.msw.masla.protocol.http.netty.pool.MaslaChannelPool;
import com.msw.masla.protocol.http.netty.util.SystemUtil;
import com.msw.masla.protocol.http.netty.pool.FixedChannelPool;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gavin.peng on 2017/5/16.
 */
public class MaslaChannelPoolManager {


    private static final Logger LOG = LoggerFactory.getLogger(MaslaChannelPoolManager.class);
    private EventLoopGroup ioGroup = null;
    private Bootstrap bootstrap;
    private Class<? extends SocketChannel> SocketChannel;
    private ConcurrentHashMap<HostInstance, MaslaChannelPool> poolMap;
    private ConcurrentHashMap<String,HostInstance> hostMap;//used by copy
    private static final int LOW_WATER_MARK = 1024 * 8;
    private static final int HIGH_WATER_MARK = 1024 * 128;
    private int ioThreadCount = 10;


    private static class MaslaChannelPoolManagerHolder {

        static MaslaChannelPoolManager instance = new MaslaChannelPoolManager();
    }

    public static MaslaChannelPoolManager getInstance(){
        return MaslaChannelPoolManagerHolder.instance;
    }

    private MaslaChannelPoolManager(){
        this.poolMap = new ConcurrentHashMap<HostInstance, MaslaChannelPool>();
        this.hostMap = new ConcurrentHashMap<String, HostInstance>();
        //this.initIOGroup();
    }


    /**
     *
     * @param ioThreadCount
     * @param connectionTimeout
     * @param acquireConnectionTimeout
     * @param maxConnections
     * @param maxPendingAcquires
     */
    public void initIOParams(int ioThreadCount,int soReadTimeout,int connectionTimeout,
                            int acquireConnectionTimeout,int maxConnections,
                            int maxPendingAcquires,int numDirectArenas,int numHeapArenas){
        this.ioThreadCount = ioThreadCount;
//        this.soReadTimeout = soReadTimeout;
//        this.connectionTimeout = connectionTimeout;
//        this.acquireConnectionTimeout = acquireConnectionTimeout;
//        this.maxConnections = maxConnections;
//        this.maxPendingAcquires = maxPendingAcquires;
//        this.numDirectArenas = numDirectArenas;
//        this.numHeapArenas = numHeapArenas;

    }


    public void initIOGroup(){
        try {
            if (SystemUtil.canUseNative() ) {
                ioGroup = new EpollEventLoopGroup(ioThreadCount,
                        new DefaultThreadFactory("ioEpollEventLoopGroup" + "-" + "Masla"));
                SocketChannel = EpollSocketChannel.class;
            } else {
                ioGroup = new NioEventLoopGroup(ioThreadCount,
                        new DefaultThreadFactory("ioNioEventLoopGroup" + "-" + "Masla"));
                ((NioEventLoopGroup)ioGroup).setIoRatio(100);
                SocketChannel = NioSocketChannel.class;
            }
        } catch (AbstractMethodError e) {
            LOG.error("Masla init netty group error:{}",e);
        }
    }

    private void addChannelPool(HostInstance HostInstance, MaslaChannelPool channelPool){
        //this.ioGroup.getClass().
        this.poolMap.putIfAbsent(HostInstance,channelPool);
    }

    public MaslaChannelPool getChannelPool(HostInstance HostInstance, ServiceApp appDO) throws Exception{
        MaslaChannelPool fixedChannelPool = this.poolMap.get(HostInstance);
        if(fixedChannelPool != null)
            return fixedChannelPool;
        //new host need to create channel pool
        synchronized (poolMap) {
            if((fixedChannelPool = this.poolMap.get(HostInstance)) == null) {
                fixedChannelPool = this.createChannelPool(HostInstance, appDO);
                this.addChannelPool(HostInstance,fixedChannelPool);
            }
        }
        return fixedChannelPool;
    }


    public MaslaChannelPool removeChannelPoolByHost(String host) {
        HostInstance HostInstance = this.hostMap.remove(host);
        if(HostInstance != null) {
            return this.poolMap.remove(HostInstance);
        }
        return null;
    }

    public MaslaChannelPool removeChannelPool(HostInstance HostInstance) {
        return this.poolMap.remove(HostInstance);
    }


    private MaslaChannelPool createChannelPool(HostInstance HostInstance, ServiceApp appDO) throws Exception{

        //这里还是tomcat 的 catalina work 线程

        String remoteIp = HostInstance.getHost();
        Preconditions.checkNotNull(remoteIp, "remote service ip should not be null!");
        EventLoopFactory eventLoopFactory = MaslaEventLoopGroupFactory.getInstance();
        NettyConfig nettyConfig = eventLoopFactory.getNettyConfig();

        int maxConnections = nettyConfig.getMaxConnections();

        if(maxConnections < 1000){
            maxConnections = 1000;
        }

        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopFactory.getClientEventLoopGroup(appDO.getPoolGroupType(), appDO.getName()));
        bootstrap.channel(eventLoopFactory.getSocketChannel());
        bootstrap.option(ChannelOption.ALLOCATOR, eventLoopFactory.getBufAllocator(appDO.getPoolGroupType(), appDO.getName()));
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, nettyConfig.getConnectionTimeout());
        bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(LOW_WATER_MARK, HIGH_WATER_MARK));
        bootstrap.remoteAddress(new InetSocketAddress(remoteIp, HostInstance.getPort()));
        MaslaClientChannelPoolHandler nettyClientChannelPoolHandler = new MaslaClientChannelPoolHandler(nettyConfig.getSoReadTimeout(),
                null);
        final MaslaChannelPool fixedChannelPool = new MaslaChannelPool(bootstrap, nettyClientChannelPoolHandler, new ChannelHealthChecker() {

            @Override
            public io.netty.util.concurrent.Future<Boolean> isHealthy(Channel channel) {
                EventLoop loop = channel.eventLoop();
                return channel.isOpen() && channel.isActive() && channel.isWritable() ? loop.newSucceededFuture(Boolean.TRUE)
                        : loop.newSucceededFuture(Boolean.FALSE);
            }},
            FixedChannelPool.AcquireTimeoutAction.FAIL, nettyConfig.getAcquireConnectionTimeout(), maxConnections,nettyConfig.getMaxPendingAcquires(),
                true,HostInstance);

        return fixedChannelPool;
    }




    public void destory(){

        if(this.poolMap != null && this.poolMap.size()>0){
            Set<Map.Entry<HostInstance, MaslaChannelPool>> channelPools = this.poolMap.entrySet();
            for(Map.Entry<HostInstance, MaslaChannelPool> entry:channelPools){
                MaslaChannelPool channelPool = entry.getValue();
                channelPool.close();
            }
        }

        if (ioGroup != null) {
            ioGroup.shutdownGracefully();
        }


    }


    public ConcurrentHashMap<HostInstance, MaslaChannelPool> getPoolMap() {
        return poolMap;
    }


    public static void main(String[] args){
        MaslaChannelPoolManager poolManager = new MaslaChannelPoolManager();
//        try {
//            final FixedChannelPool pool = poolManager.getChannelPool("localhost",8081);
//            for(int i=0;i<10;i++) {
//                Future p = pool.acquire();
//                p.addListener(new FutureListener<Void>() {
//                    @Override
//                    public void operationComplete(Future<Void> future) throws Exception {
//                        if (future.isSuccess()) {
//                            System.out.println("connection ok......");
//                            //pool.release()
//                        } else {
//                            System.out.println("connection failed......");
//
//                        }
//                    }
//                });
//            }
//            Thread.sleep(20000000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


}
