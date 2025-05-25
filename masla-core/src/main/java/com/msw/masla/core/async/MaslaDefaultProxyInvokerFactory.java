package com.msw.masla.core.async;

import com.msw.masla.common.config.MaslaServerConfig;
import com.msw.masla.core.async.repsone.MaslaHttpDecode;
import com.msw.masla.core.async.pool.MaslaPushExecutors;
import com.msw.masla.core.discovery.healthcheck.ServiceHealthcheckManager;
import com.msw.masla.core.discovery.nacos.MaslaNacosDiscoveryProperties;
import com.msw.masla.core.discovery.nacos.MaslaNacosServiceDiscovery;
import com.msw.masla.core.discovery.nacos.MaslaNacosServiceManager;
import com.msw.masla.core.invoker.ClusterProxyAsyncInvoker;
import com.msw.masla.core.invoker.ProxyInvoker;
import com.msw.masla.core.invoker.loadbalance.ConsistentHashLoadBalance;
import com.msw.masla.core.invoker.loadbalance.LoadBalanceFactory;
import com.msw.masla.core.invoker.loadbalance.MaslaDefaultLoadBalanceFactory;
import com.msw.masla.core.invoker.loadbalance.RoundRobinLoadBalance;
import com.msw.masla.core.router.DefaultRouteRuleFactory;
import com.msw.masla.protocol.http.netty.config.NettyConfig;
import com.msw.masla.protocol.http.netty.http.HostInstance;
import com.msw.masla.protocol.http.netty.http.connection.MaslaChannelPoolManager;
import com.msw.masla.protocol.http.netty.factory.MaslaEventLoopGroupFactory;
import com.msw.masla.core.async.repsone.MaslaDirectResponse;
import com.msw.masla.core.async.pool.MaslaProcessorExecutors;
import com.msw.masla.core.push.engine.AsyncPushEngine;
import com.msw.masla.core.push.engine.PushEngine;
import com.msw.masla.core.push.engine.SyncPushEngine;

import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.PostConstruct;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("proxyInvokerFactory")
@Data
public class MaslaDefaultProxyInvokerFactory implements DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(MaslaDefaultProxyInvokerFactory.class);

    private ProxyInvoker asyncProxyInvoker;

	private boolean open = false;

	@Autowired
	private MaslaServerConfig maslaServerConfig;

	@Autowired
    private MaslaPushExecutors maslaPushExecutors;

	@Autowired
	private MaslaNacosDiscoveryProperties maslaNacosDiscoveryProperties;

	@Autowired
	private MaslaNacosServiceManager maslaNacosServiceManager;

	private MaslaNacosServiceDiscovery nacosServiceDiscovery;


	public final boolean isOpen() {

		return open;
	}

	public final void setOpen(boolean open) {

		this.open = open;
	}

	private int logtype = 1;


    @PostConstruct
	public void init() throws Exception {

		this.nacosServiceDiscovery = new MaslaNacosServiceDiscovery(this.maslaNacosDiscoveryProperties, this.maslaNacosServiceManager);
        this.asyncProxyInvoker = new ClusterProxyAsyncInvoker(this, nacosServiceDiscovery);

		MaslaEventLoopGroupFactory eventLoopGroupFactory = MaslaEventLoopGroupFactory.getInstance();
		NettyConfig nettyConfig = NettyConfig.getInstance();
		nettyConfig.setDefalutEventLoopThreadCount(maslaServerConfig.getIoThreadCount());
		nettyConfig.setSlowEventLoopThreadCount(maslaServerConfig.getSlowIOThreadCount());
		nettyConfig.setAcquireConnectionTimeout(maslaServerConfig.getAcquireConnectionTimeout());
		nettyConfig.setMaxPendingAcquires(maslaServerConfig.getMaxPendingAcquires());
		nettyConfig.setConnectionTimeout(maslaServerConfig.getConnectionTimeout());
		nettyConfig.setMaxConnections(maslaServerConfig.getMaxConnections());
		nettyConfig.setNumDirectArenas(maslaServerConfig.getNumDirectArenas());
		nettyConfig.setNumHeapArenas(maslaServerConfig.getNumHeapArenas());
		nettyConfig.setSoReadTimeout(maslaServerConfig.getReadTimeout());
		nettyConfig.setPageSize(maslaServerConfig.getPageSize());

		//malsa server config
		nettyConfig.setPort(maslaServerConfig.getPort());
		nettyConfig.setSslPort(maslaServerConfig.getSslPort());
		nettyConfig.setBacklog(maslaServerConfig.getBacklog());
		nettyConfig.setMaxSession(maslaServerConfig.getMaxSession());
		//https 链接超时时间
		nettyConfig.setH2SessionTimeout(maslaServerConfig.getSessionTimeout());
		nettyConfig.setMaxKeepAliveRequests(maslaServerConfig.getMaxKeepAliveRequests());
		nettyConfig.setAcceptThreadCnt(maslaServerConfig.getAcceptThreadCnt());
		nettyConfig.setServerIOThreadCnt(maslaServerConfig.getServerIOThreadCnt());
		nettyConfig.setAcceptQueueSize(maslaServerConfig.getAcceptQueueSize());
		nettyConfig.setWorkerThreadCoreSize(maslaServerConfig.getWorkerThreadCoreSize());
		nettyConfig.setWorkerThreadMaxSize(maslaServerConfig.getWorkerThreadMaxSize());
		nettyConfig.setSessionIdleTime(maslaServerConfig.getSessionIdleTime());
		nettyConfig.setDirectMemorySize(maslaServerConfig.getDirectMemorySize());
		nettyConfig.setBackupHandlers(maslaServerConfig.getBackupHandlers());
		nettyConfig.setBackupQueueSize(maslaServerConfig.getBackupQueueSize());
		nettyConfig.setPriorityHandlers(maslaServerConfig.getPriorityHandlers());
		nettyConfig.setPriorityQueueSize(maslaServerConfig.getPriorityQueueSize());
		nettyConfig.setMaxContentLength(maslaServerConfig.getMaxContentLength());


		eventLoopGroupFactory.initEventLoopGroup(nettyConfig);
		//init masla http decode
		MaslaHttpDecode.getInstance().initPushEngine(this);
		MaslaDirectResponse maslaDirectResponse = MaslaDirectResponse.getInstance();
		maslaDirectResponse.initPushEngine(this);
		SyncPushEngine.getPushEngine(this);

		//init masla load balance
		LoadBalanceFactory<HostInstance> loadBalanceFactory = MaslaDefaultLoadBalanceFactory.getInstance();
		loadBalanceFactory.registerLoadBalance(new RoundRobinLoadBalance());
		loadBalanceFactory.registerLoadBalance(new ConsistentHashLoadBalance());

		//init masla api route properties
		DefaultRouteRuleFactory routeRuleFactory = DefaultRouteRuleFactory.getDefaultRouteRuleFactoryInstance();
		routeRuleFactory.intRouteRuleFile(nacosServiceDiscovery);

		//register healthcheck
		ServiceHealthcheckManager.getInstance().registerMaslaNacosServiceDiscovery(nacosServiceDiscovery);
		ServiceHealthcheckManager.getInstance().registerMaslaSeverConfig(maslaServerConfig);
		ServiceHealthcheckManager.getInstance().startDoHealthcheck();

	}


	public ThreadPoolExecutor getPushEngineExecutor(){
	    return this.maslaPushExecutors.getExecutor();
    }

	public void releaseResources(){

		//TODO release netty bootstrap;
		MaslaChannelPoolManager maslaChannelPoolManager = MaslaChannelPoolManager.getInstance();
		maslaChannelPoolManager.destory();
		//release netty eventLoopGroup
		MaslaEventLoopGroupFactory eventLoopGroupFactory = MaslaEventLoopGroupFactory.getInstance();
		eventLoopGroupFactory.destroy();
		MaslaProcessorExecutors.getInstance().release();

	}

    @Override
    public void destroy() throws Exception {
        this.releaseResources();
        PushEngine pushEngine = AsyncPushEngine.getPushEngine(this);
        pushEngine.releaseResource();
    }





}
