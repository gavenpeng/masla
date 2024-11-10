package com.msw.masla.common.monitor.vo;

import lombok.Data;

@Data
public class DirectMemoryMonitorVO {

    private String host;
    private long timestamp;
    private long appUsedMemory;
    private long serverUsedMemory;
    private long totalUsedMemory;
}
