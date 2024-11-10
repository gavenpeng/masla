package com.msw.masla.common.monitor.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptQueueCount {

    private long timestamp;

    private String host;

    private Integer queueSize = 0;

}
