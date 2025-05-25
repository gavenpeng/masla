/*
 * Copyright 2025 msw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.msw.masla.protocol.http.netty.config;

/**
 * Created by Gavin.peng on 2017/7/14.
 */
public class NettyConfig {



    public static final long MAX_DIRECT_MEMORY_SIZE = 1000 * 1000 * 1000 * 6l;

    private static final NettyConfig instance = new NettyConfig();

    private int connectionTimeout = 2000;

    private int h2SessionTimeout = 5000;

    private int soReadTimeout = 5000;

    private int defalutEventLoopThreadCount = 30;

    private int fastEventLoopThreadCount = 50;

    private int slowEventLoopThreadCount = 20;

    private int acquireConnectionTimeout = 200;

    private int maxConnections = 50;

    private int topAppMaxConnections = 1000;

    private int maxPendingAcquires = 200;

    private int numDirectArenas = 1;

    private int numHeapArenas = 1;

    private int pageSize = 4096;

    private long directMemorySize = 2l;



    //server config
    private int port = 20018;

    private int sslPort = 443;

    private int backlog = 10240;

    private boolean supportHttps;

    private int serverIOThreadCnt = 30;

    private int acceptThreadCnt = 2;

    private int maxSession = 2;

    private int maxAppSession = 2;

    private int priorityHandlers = 2;

    private int backupHandlers = 2;

    private int maxKeepAliveRequests = 2;

    private  int acceptQueueSize = 1000;

    private  int priorityQueueSize = 1000;

    private  int backupQueueSize = 1000;

    private  int workerThreadCoreSize = 50;

    private  int workerThreadMaxSize = 200;

    private  int sessionIdleTime = 20000;

    private  int maxContentLength = 1024*1024;


    public int getConnectionTimeout() {
        return connectionTimeout;
    }


    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoReadTimeout() {
        return soReadTimeout;
    }

    public void setSoReadTimeout(int soReadTimeout) {
        this.soReadTimeout = soReadTimeout;
    }

    public int getDefalutEventLoopThreadCount() {
        return defalutEventLoopThreadCount;
    }

    public void setDefalutEventLoopThreadCount(int defalutEventLoopThreadCount) {
        this.defalutEventLoopThreadCount = defalutEventLoopThreadCount;
    }

    public int getFastEventLoopThreadCount() {
        return fastEventLoopThreadCount;
    }

    public void setFastEventLoopThreadCount(int fastEventLoopThreadCount) {
        this.fastEventLoopThreadCount = fastEventLoopThreadCount;
    }

    public int getSlowEventLoopThreadCount() {
        return slowEventLoopThreadCount;
    }

    public void setSlowEventLoopThreadCount(int slowEventLoopThreadCount) {
        this.slowEventLoopThreadCount = slowEventLoopThreadCount;
    }

    public int getAcquireConnectionTimeout() {
        return acquireConnectionTimeout;
    }

    public void setAcquireConnectionTimeout(int acquireConnectionTimeout) {
        this.acquireConnectionTimeout = acquireConnectionTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getMaxPendingAcquires() {
        return maxPendingAcquires;
    }

    public void setMaxPendingAcquires(int maxPendingAcquires) {
        this.maxPendingAcquires = maxPendingAcquires;
    }

    public int getNumDirectArenas() {
        return numDirectArenas;
    }

    public void setNumDirectArenas(int numDirectArenas) {
        this.numDirectArenas = numDirectArenas;
    }

    public int getNumHeapArenas() {
        return numHeapArenas;
    }

    public void setNumHeapArenas(int numHeapArenas) {
        this.numHeapArenas = numHeapArenas;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getSslPort() {
        return sslPort;
    }

    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public int getServerIOThreadCnt() {
        return serverIOThreadCnt;
    }

    public void setServerIOThreadCnt(int serverIOThreadCnt) {
        this.serverIOThreadCnt = serverIOThreadCnt;
    }

    public int getAcceptThreadCnt() {
        return acceptThreadCnt;
    }

    public void setAcceptThreadCnt(int acceptThreadCnt) {
        this.acceptThreadCnt = acceptThreadCnt;
    }

    public int getMaxSession() {
        return maxSession;
    }

    public void setMaxSession(int maxSession) {
        this.maxSession = maxSession;
    }

    public int getMaxAppSession() {
        return maxAppSession;
    }

    public void setMaxAppSession(int maxAppSession) {
        this.maxAppSession = maxAppSession;
    }

    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }

    public void setMaxKeepAliveRequests(int maxKeepAliveRequests) {
        this.maxKeepAliveRequests = maxKeepAliveRequests;
    }

    public int getWorkerThreadCoreSize() {
        return workerThreadCoreSize;
    }

    public void setWorkerThreadCoreSize(int workerThreadCoreSize) {
        this.workerThreadCoreSize = workerThreadCoreSize;
    }

    public int getWorkerThreadMaxSize() {
        return workerThreadMaxSize;
    }

    public void setWorkerThreadMaxSize(int workerThreadMaxSize) {
        this.workerThreadMaxSize = workerThreadMaxSize;
    }

    public int getSessionIdleTime() {
        return sessionIdleTime;
    }

    public void setSessionIdleTime(int sessionIdleTime) {
        this.sessionIdleTime = sessionIdleTime;
    }

    public int getAcceptQueueSize() {
        return acceptQueueSize;
    }

    public void setAcceptQueueSize(int acceptQueueSize) {
        this.acceptQueueSize = acceptQueueSize;
    }


    public long getDirectMemorySize() {
        return directMemorySize;
    }

    public void setDirectMemorySize(long directMemorySize) {
        this.directMemorySize = directMemorySize;
    }

    public int getPriorityHandlers() {
        return priorityHandlers;
    }

    public void setPriorityHandlers(int priorityHandlers) {
        this.priorityHandlers = priorityHandlers;
    }

    public int getBackupHandlers() {
        return backupHandlers;
    }

    public void setBackupHandlers(int backupHandlers) {
        this.backupHandlers = backupHandlers;
    }

    public int getPriorityQueueSize() {
        return priorityQueueSize;
    }

    public void setPriorityQueueSize(int priorityQueueSize) {
        this.priorityQueueSize = priorityQueueSize;
    }

    public int getBackupQueueSize() {
        return backupQueueSize;
    }

    public void setBackupQueueSize(int backupQueueSize) {
        this.backupQueueSize = backupQueueSize;
    }

    public int getMaxContentLength() {
        return maxContentLength;
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public int getH2SessionTimeout() {
        return h2SessionTimeout;
    }

    public void setH2SessionTimeout(int h2SessionTimeout) {
        this.h2SessionTimeout = h2SessionTimeout;
    }

    public static NettyConfig getInstance() {
        return instance;
    }
}
