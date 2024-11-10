package com.msw.masla.common.monitor.metrics;

import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

/**
 * 带宽使用情况统计
 *
 * @author jimmy.zhong
 */
@Data
public class BandwidthCount {


  public BandwidthCount() {
    this.inLineBWCount = new AtomicLong(0);
    this.inHeaderBWCount = new AtomicLong(0);
    this.inBodyBWCount = new AtomicLong(0);

    this.outLineBWCount = new AtomicLong(0);
    this.outHeaderBWCount = new AtomicLong(0);
    this.outBodyBWCount = new AtomicLong(0);



    lastActiveTime = System.currentTimeMillis();
  }

  private String group;

  private String appName;

  private String host;

  private String serviceId;

  private long timestamp;
  //入站带宽统计
  private AtomicLong inLineBWCount;
  private AtomicLong inHeaderBWCount;
  private AtomicLong inBodyBWCount;


  //出站带宽统计
//  private AtomicLong outBandwidthCount;

  private AtomicLong outLineBWCount;
  private AtomicLong outHeaderBWCount;
  private AtomicLong outBodyBWCount;
  private long lastActiveTime;

}
