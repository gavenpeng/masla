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

import lombok.Data;

/**
 * 后端应用连接池统计
 */
@Data
public class ChannelPoolCount {

  public ChannelPoolCount(String appName, int connCount, int pendingCount,long pendingOutboundBytes,long sendedOutboundBytes, String appHost, String host, String group, long timestamp) {
    this.appName = appName;
    this.connCount = connCount;
    this.pendingCount = pendingCount;
    this.pendingOutboundBytes = pendingOutboundBytes;
    this.sendedOutboundBytes = sendedOutboundBytes;
    this.appHost = appHost;
    this.host = host;
    this.group = group;
    this.timestamp = timestamp;


  }

  public ChannelPoolCount() {
  }

  /**
   * 应用名称
   */
  private String appName;
  /**
   * 连接数
   */
  private int connCount;

  /**
   * 等待获取连接数量
   */
  private int pendingCount;


  //链接上需要发送的字节数
  private long pendingOutboundBytes;

  //链接上已经发完的字节数从 长春长春长春国贸在
  private long sendedOutboundBytes;


  /**
   * 应用机器
   */
  private String appHost;
  /**
   * 网关当前分组
   */
  private String group;

  /**
   * 网关机器
   */
  private String host;

  /**
   * 时间戳
   */
  private long timestamp;

}
