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
