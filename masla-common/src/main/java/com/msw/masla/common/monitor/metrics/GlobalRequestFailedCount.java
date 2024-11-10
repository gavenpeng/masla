package com.msw.masla.common.monitor.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

/**
 * 网关未转发给后端请求处理失败数据统计
 *
 * @author jimmy.zhong
 */

@Data
public class GlobalRequestFailedCount {

  public GlobalRequestFailedCount() {
    requestTooLarge = new AtomicLong(0);
    exceedMaxSession = new AtomicLong(0);
    globalBlack = new AtomicLong(0);
    urlNotFound = new AtomicLong(0);
    queueFull = new AtomicLong(0);
    urlNotFoundMap = new ConcurrentHashMap<String, AtomicInteger>();
  }

  private String group;

  private String host;


  /**
   * 请求过大禁止数量
   */
  private AtomicLong requestTooLarge;

  /**
   * 超出网关最大session设置数量
   */
  private AtomicLong exceedMaxSession;


  /**
   * 全局黑名单禁止访问数量
   */
  private AtomicLong globalBlack;

  /**
   * 未找到配置的转发路径数量
   */
  private AtomicLong urlNotFound;

  private ConcurrentHashMap<String, AtomicInteger> urlNotFoundMap;

  /**
   * 队列打满
   */
  private AtomicLong queueFull;

  private long timestamp;

}
