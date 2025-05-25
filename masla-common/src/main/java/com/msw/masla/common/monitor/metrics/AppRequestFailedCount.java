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

import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

/**
 * 应用级别请求失败统计
 *
 * @author jimmy.zhong
 */
@Data
public class AppRequestFailedCount {



  public AppRequestFailedCount(String serviceId) {
    this.serviceId = serviceId;
    this.blackForbiddenCount = new AtomicLong(0);
    this.noLoginCount = new AtomicLong(0);
    this.requestTooLarge = new AtomicLong(0);
    this.customizedResponseHitCount = new AtomicLong(0);
//    this.varnishCacheHitCount = new AtomicLong(0);
    this.noAvailableHostCount = new AtomicLong(0);
    this.headerTooLargeCount = new AtomicLong(0);
    this.headerInvalidCharacterCount = new AtomicLong(0);
    this.readConnClosedCount = new AtomicLong(0);
    this.httpLineTooLargeCount = new AtomicLong(0);
    this.noReceivePostBodyCount = new AtomicLong(0);
    this.moonForbiddenCount = new AtomicLong(0);
    this.queueFullCount = new AtomicLong(0);
    this.preQps = 0l;
    this.lastActiveTime = -1;
  }


  private Long preQps = 0l;
  private long lastActiveTime = -1;

  private String serviceId;

  /**
   * 黑名单禁止访问请求数量
   */
  private AtomicLong blackForbiddenCount;


  /**
   * 登陆校验失败请求数量
   */
  private AtomicLong noLoginCount;


  /**
   * 请求过大
   */
  private AtomicLong requestTooLarge;

  /**
   * 命中varnish缓存响应数量
   */
//  private AtomicLong varnishCacheHitCount;

  /**
   * 命中自定义响应数量
   */
  private AtomicLong customizedResponseHitCount;


  /**
   * 找不到路由信息
   */
  private AtomicLong noAvailableHostCount;

  /**
   * 头部过大
   */
  private AtomicLong headerTooLargeCount;

  /**
   * 请求行过大
   */
  private AtomicLong httpLineTooLargeCount;

  /**
   * 请求头包含非法资字符
   */
  private AtomicLong headerInvalidCharacterCount;

  /**
   * 读取请求时连接关闭
   */
  private AtomicLong readConnClosedCount;

  /**
   * 网关收到请求行和请求头，没有读到body的请求
   * */
  private AtomicLong noReceivePostBodyCount;

  /**
   * 风控拦截
   * */
  private AtomicLong moonForbiddenCount;

  /**
   * 队列打满
   * */
  private AtomicLong queueFullCount;

  public boolean hasFailedCount(){
    if(this.blackForbiddenCount.get() >0
      || this.customizedResponseHitCount.get() > 0
      || this.headerInvalidCharacterCount.get() >0
      || this.headerTooLargeCount.get() > 0
      || this.moonForbiddenCount.get() >0
      || this.noAvailableHostCount.get() > 0
      || this.noLoginCount.get() > 0
      || this.noReceivePostBodyCount.get() >0
      || this.queueFullCount.get() > 0
      ){
      return true;
    }
    return false;
  }

}
