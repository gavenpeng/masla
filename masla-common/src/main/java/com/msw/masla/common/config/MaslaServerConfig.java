package com.msw.masla.common.config;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Author: Gavin.peng
 * Date: 2024/4/14
 * Description:
 * Masla global config item
 */
@Component("maslaServerConfig")
@DependsOn("nacosConfiguration")
@Data
@Slf4j
public class MaslaServerConfig {

    public MaslaServerConfig() {
        log.info("Masla init server config instance....");
    }

    @NacosValue(value = "${masla.server.protocol.support.https:false}", autoRefreshed = true)
    private boolean supportHttps;

    @NacosValue(value = "${masla.server.client.io.work.threadCount:5}", autoRefreshed = true)
    private Integer ioThreadCount;

    @NacosValue("${masla.server.client.io.work.slow.threadCount:2}")
    private Integer slowIOThreadCount;

    @NacosValue("${masla.server.processor.processorCount:10}")
    private Integer processorCount;

    @NacosValue("${masla.server.readTimeout:10000}")
    private Integer readTimeout;

    @NacosValue("${masla.server.client.connectionTimeout:2000}")
    private Integer connectionTimeout;

    @NacosValue("${masla.server.client.acquireConnectionTimeout:2000}")
    private Integer acquireConnectionTimeout;

    @NacosValue("${masla.server.client.maxPendingAcquires:1000}")
    private Integer maxPendingAcquires;

    @NacosValue("${masla.server.client.maxConnections:100}")
    private Integer maxConnections;


    @NacosValue("${masla.server.buffer.pool.numDirectArenas:16}")
    private Integer numDirectArenas;

    @NacosValue("${masla.server.buffer.pool.numHeapArenas:1}")
    private Integer numHeapArenas;

    @NacosValue("${masla.server.buffer.pool.pageSize:4096}")
    private Integer pageSize;

    @NacosValue("${masla.server.buffer.pool.directMemorySize:4}")
    private Long directMemorySize;

    @NacosValue("${masla.servlet.context.default.timeout.pending:1000}")
    private Long defaultTimeoutPending;


    @NacosValue(value = "${masla.server.port:6081}", autoRefreshed = true)
    private Integer port;

    @NacosValue("${masla.server.sslPort:443}")
    private Integer sslPort;

    @NacosValue(value = "${masla.server.backlog:10240}", autoRefreshed = true)
    private Integer backlog;

    @NacosValue(value = "${masla.server.maxSession:20000}", autoRefreshed = true)
    private Integer maxSession;

    @NacosValue("${masla.server.sessionTimeout:120000}")
    private Integer sessionTimeout;

    @NacosValue("${masla.server.maxKeepAliveRequests:100}")
    private Integer maxKeepAliveRequests;

    @NacosValue("${masla.server.acceptThreadCnt:2}")
    private Integer acceptThreadCnt;

    @NacosValue("${masla.server.serverIOThreadCnt:2}")
    private Integer serverIOThreadCnt;

    @NacosValue("${masla.server.workerThreadCoreSize:10}")
    private Integer workerThreadCoreSize;

    @NacosValue("${masla.server.acceptQueueSize:2000}")
    private Integer acceptQueueSize;

    @NacosValue("${masla.server.priorityQueueSize:1000}")
    private Integer priorityQueueSize;

    @NacosValue("${masla.server.backupQueueSize:500}")
    private Integer backupQueueSize;

    @NacosValue("${masla.server.workerThreadMaxSize:20}")
    private Integer workerThreadMaxSize;

    @NacosValue("${masla.server.priorityHandlers:10}")
    private Integer priorityHandlers;

    @NacosValue("${masla.server.backupHandlers:5}")
    private Integer backupHandlers;

    @NacosValue("${masla.server.sessionIdleTime:60000}")
    private Integer sessionIdleTime;

    @NacosValue("${masla.server.maxContentLength:400000}")
    private Integer maxContentLength;


    @NacosValue("${masla.metrics.collector.server.address:}")
    private String maslaCollectorServerAddress;


    public void setPort(Integer port) {
        this.port = port;
    }

}
