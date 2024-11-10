package com.msw.masla.common.monitor.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * created by lingxiaochen on 2019/12/27
 */
@Getter
@Setter
public class TaskQueueMonitorVO implements Comparable {
    private String gwGroup;
    private String host;
    private String loopGroupType;
    private Integer pendingTask;
    private Integer loopGroupSize;
    private Long ts;

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
