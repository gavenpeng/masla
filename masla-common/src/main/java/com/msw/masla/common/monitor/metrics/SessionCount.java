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
 * 应用session统计
 */
@Data
public class SessionCount {

  public SessionCount(String appName, int count,int https1SessionNum,int https2SessionNum, String host, long timestamp) {
    this.appName = appName;
    this.count = count;
    this.https1SessionNum = https1SessionNum;
    this.https2SessionNum = https2SessionNum;
    this.host = host;
    this.timestamp = timestamp;

  }

  public SessionCount() {
  }

  /**
   * 应用名称
   */
  private String appName;
  /**
   * 连接数
   */
  private int count;

  /**
   * HTTPS2连接数
   */
  private int https2SessionNum;
  /**
   * HTTPS1连接数
   */
  private int https1SessionNum;

  /**
   * 网关当前机器IP
   */
  private String host;
  /**
   * 网关当前分组
   */
  private String group;

  /**
   * 时间戳
   */
  private long timestamp;

}
