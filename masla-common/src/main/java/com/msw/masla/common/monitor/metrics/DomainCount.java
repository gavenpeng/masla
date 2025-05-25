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
package com.msw.masla.common.monitor.metrics;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

/**
 * 域名及nginx带宽,QPS统计
 *
 * @author jimmy.zhong
 */
@Data
public class DomainCount {

  public static final String IOS = "os";
  public static final String IPHONE = "iPhone";
  public static final String ANDROIDS = "android";
  public static final String WINDOW = "wp";

  // ios_watch
  public static final String IOS_WATCH = "ios_watch";

  // ios_pad
  public static final String IOS_PAD = "ios_pad";

  public DomainCount() {
    inBandwidth = new AtomicLong(0);
    outBandwidth = new AtomicLong(0);
    queryCount = new AtomicLong(0);
    flowControlNums = new AtomicLong(0);
    iosQps = new AtomicLong(0);
    iosH2Qps = new AtomicLong(0);
    iosHTTPSQps = new AtomicLong(0);
    androidQps = new AtomicLong(0);
    androidH2Qps = new AtomicLong(0);
    androidHTTPSQps = new AtomicLong(0);
    iosInBandWidth = new AtomicLong(0);
    androidInBandWidth = new AtomicLong(0);
    wpQps = new AtomicLong(0);
    wpInBandWidth = new AtomicLong(0);
    iosWatchQps = new AtomicLong(0);
    iosWatchInBandWidth = new AtomicLong(0);
    iosPadQps = new AtomicLong(0);
    iosPadInBandWidth = new AtomicLong(0);
    iosReQps = new AtomicLong(0);
    androidReQps = new AtomicLong(0);
    wpReQps = new AtomicLong(0);
    ipv6Qps = new AtomicLong(0);
    //支持自定义扩展字段，方便统计新的头时，网关不需要开发
    extendKeyMap = new ConcurrentHashMap<String, AtomicLong>(4);
    this.lastUpdateTime = System.currentTimeMillis();
  }

  /**
   * 域名
   */
  private String domain;
  /**
   * nginx ip
   */
  private String nginxIp;

  /**
   * 机房
   */
  private String dc;

  /**
   * 入站带宽
   */
  private AtomicLong inBandwidth;

  /**
   * 出站带宽
   */
  private AtomicLong outBandwidth;

  /**
   * 访问量
   */
  private AtomicLong queryCount;

  /**
   * 流控次数
   */
  private AtomicLong flowControlNums;


  //ios的qps
  private AtomicLong iosQps;
  private AtomicLong iosH2Qps;
  private AtomicLong iosHTTPSQps;
  private AtomicLong iosInBandWidth;

  //android的qps
  private AtomicLong androidQps;
  private AtomicLong androidH2Qps;
  private AtomicLong androidHTTPSQps;
  private AtomicLong androidInBandWidth;

  //windowPhone的qps
  private AtomicLong wpQps;
  private AtomicLong wpInBandWidth;

  // iosWatch的qps
  private AtomicLong iosWatchQps;
  private AtomicLong iosWatchInBandWidth;

  // iosPad的pqs
  private AtomicLong iosPadQps;
  private AtomicLong iosPadInBandWidth;

  //android和ios的重试qps
  private AtomicLong iosReQps;
  private AtomicLong androidReQps;
  private AtomicLong wpReQps;

  private AtomicLong ipv6Qps;

  private ConcurrentHashMap<String,AtomicLong> extendKeyMap;




  /**
   * 应用
   */
  private String app;

  private Long appId;


  private String group;

  private long timestamp;

  /**
   * 网关机器
   */
  private String host;
  /**
   * 记录更新时间，防止域名和nginx关系变更后导致内存泄漏
   */
  @JSONField(serialize = false)
  private long lastUpdateTime;

  public void resetExtendKeyMap(DomainCount domainCount){
    for(Map.Entry<String,AtomicLong> entry:this.extendKeyMap.entrySet()){
      domainCount.getExtendKeyMap().put(entry.getKey(),new AtomicLong(entry.getValue().getAndSet(0)));
    }
  }
}
