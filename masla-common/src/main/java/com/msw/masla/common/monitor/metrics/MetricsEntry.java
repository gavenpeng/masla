package com.msw.masla.common.monitor.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MetricsEntry {
  private String type;
  private Object metrics;
}
