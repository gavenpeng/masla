package com.msw.masla.common.enums;

/**
 * 数据库记录状态
 */
public enum Status {

  ACTIVE(1), INACTIVE(0),TEMP_INACTIVE(2),NEED_REVIEW(3);

  private int code;

  Status(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
