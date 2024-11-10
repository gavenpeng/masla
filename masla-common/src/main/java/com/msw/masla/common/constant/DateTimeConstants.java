package com.msw.masla.common.constant;

/**
 * DateTimeConstants is a non-instantiable class of constants used in the date time system.
 * !!! Mind int overflow issue.
 */
public class DateTimeConstants {


  /**
   * second in millisecond
   */
  public static final int SECOND = 1000;

  /**
   * minute in millisecond
   */
  public static final int MINUTE = 60 * SECOND;

  /**
   * hour in millisecond
   */
  public static final int HOUR = 60 * MINUTE;

  /**
   * day in millisecond
   */
  public static final int DAY = 24 * HOUR;


  private DateTimeConstants() {
  }


}
