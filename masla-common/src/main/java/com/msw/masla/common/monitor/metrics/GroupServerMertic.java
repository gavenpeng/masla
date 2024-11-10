package com.msw.masla.common.monitor.metrics;

import lombok.Data;

@Data
public class GroupServerMertic {

    /**
     * 网关当前机器IP
     */
    private String serverHost;

    /**
     * 时间戳
     */
    private long timestamp;
    /**
     * 接入的app数
     */
    private int appNums;

    /**
     * qps
     */
    private long qps;

    /**
     * 接入端连接数
     */
    private int inSessions;

    /**
     * 后端应用连接数
     */
    private int outSessions;



}
