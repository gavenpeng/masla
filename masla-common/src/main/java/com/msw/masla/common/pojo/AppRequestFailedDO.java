package com.msw.masla.common.pojo;

import lombok.Data;

/**
 *
 * @author Gavin.peng
 */
@Data
public class AppRequestFailedDO {

  private Long appId;

  private String appName;

  private String businessGroup;

  private String serviceId;


  private String host;

  /**
   * 黑名单禁止访问请求数量
   */
  private long blackForbiddenCount;


  /**
   * 登陆校验失败请求数量
   */
  private long noLoginCount;


  private long requestTooLarge;

  private long timestamp;

  /**
   * 应用连接池连接失败
   */
  private long channelConnFail;

  /**
   * 应用连接池等待队列已满，直接拒绝
   */
  private long channelQueueFull;

  /**
   * 在获取链接等待队列超时
   */
  private long channelAcquireTimeout;

  /**
   * 命中varnish缓存响应数量
   */
  private long varnishCacheHitCount;


  /**
   * 命中自定义响应返回数量
   */
  private long customizedResponseHitCount;


  /**
   * 请求端关闭连接
   */
  private long clientConnClosedCount;

  /**
   * 超过APP最大连接数限制
   */
  private long exceedSessionCount;

  /**
   * 找不到路由机器
   */
  private long noAvailableHostCount;


  /**
   * 头部过大
   */
  private long headerTooLargeCount;

  /**
   * 请求行过大
   */
  private long httpLineTooLargeCount;

  /**
   * 请求头包含非法资字符
   */
  private long headerInvalidCharacterCount;

  /**
   * 读取请求时连接关闭
   */
  private long readConnClosedCount;

  /**
   * 网关收到请求行和请求头，没有读到body的请求
   * */
  private long noReceivePostBodyCount;

  /**
   * 风控拦截
   * */
  private long moonForbiddenCount;

  /**
   * 队列打满
   * */
  private long queueFullCount;

  public void aggregateAppRequestFailedDO(AppRequestFailedDO newDO) {
    this.blackForbiddenCount += newDO.getBlackForbiddenCount();
    this.noLoginCount += newDO.getNoLoginCount();
    this.requestTooLarge += newDO.getRequestTooLarge();
    this.channelConnFail += newDO.getChannelConnFail();
    this.channelQueueFull += newDO.getChannelQueueFull();
    this.channelAcquireTimeout += newDO.getChannelAcquireTimeout();
    this.varnishCacheHitCount += newDO.getVarnishCacheHitCount();
    this.customizedResponseHitCount += newDO.getCustomizedResponseHitCount();
    this.clientConnClosedCount += newDO.getClientConnClosedCount();
    this.exceedSessionCount += newDO.getExceedSessionCount();
    this.noAvailableHostCount += newDO.getNoAvailableHostCount();
    this.headerTooLargeCount += newDO.getHeaderTooLargeCount();
    this.httpLineTooLargeCount += newDO.getHttpLineTooLargeCount();
    this.headerInvalidCharacterCount += newDO.getHeaderInvalidCharacterCount();
    this.readConnClosedCount += newDO.getReadConnClosedCount();
    this.noReceivePostBodyCount += newDO.getNoReceivePostBodyCount();
    this.moonForbiddenCount += newDO.getMoonForbiddenCount();
    this.queueFullCount += newDO.queueFullCount;
  }

}
