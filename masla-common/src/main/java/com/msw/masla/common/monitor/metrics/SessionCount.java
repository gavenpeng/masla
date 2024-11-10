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
