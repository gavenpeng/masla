package com.msw.masla.common.enums;

/**
 * 流量分发类型
 */
public enum RequestDispatchMode {
  DEFAULT,
  REDIRECT,
  COPY,
  URL_REDIRECT,
  MQ,
  CROSS_ZONE,
  VARNISH_DISPATCH;

}
