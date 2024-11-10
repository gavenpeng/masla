package com.msw.masla.common.enums;

/**
 * 日志采样错误类型标示
 *
 * @author gavin.peng
 */
public enum ErrorType {
  PROTOCOL_ERROR("EPROTOCOL"),
  GLOBAL_BLACK_ERROR("EGBLACK"),
  APP_BLACK_ERROR("EBLACK"),
  TIMEOUT_ERROR("ETIMEOUT"),
  RETRY_MAX_ERROR("EMAXRETRY"),
  CONN_CLOSE_ERROR("ESCLOSED"),
  AUTH_ERROR("EAUTH"),
  RESPONSE_ERROR("ECODE"),
  CONN_ERROR("ECONN"),
  URL_NOT_FOUND_ERROR("EUNF"),
  HEADER_TOO_LARGE_ERROR("EHTL"),
  DEFAULT_ERROR("EDEFAULT"),
  REQUEST_LOG("REQLOG");

  private String code;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  ErrorType(String code) {
    this.code = code;
  }
}
