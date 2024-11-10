package com.msw.masla.common.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author zhuyuping
 * @version 1.0
 * @created 2013-3-2 上午9:53:53
 * @functions：时间处理 这里我没用有joda-time
 */
public class TimeDateUtil {

  private static TimeZone timeZone = TimeZone.getDefault();
  private static int offHour = timeZone.getRawOffset() / 3600000;

  /**
   * yyyy-mm-dd
   */
  public static String getDate(long time) {

    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(time);
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    return dateformat.format(c.getTime());
  }

  public static String getDate3(long time) {

    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(time);
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    return dateformat.format(c.getTime());
  }

  public static java.sql.Date getTodayDate() {

    Date date = new Date();
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, 0);//把日期往后增加一天.整数往后推,负数往前移动
    date = calendar.getTime(); //这个时间就是日期往后推一天的结果
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //String dateString = formatter.format(date);
    return new java.sql.Date(date.getTime());
  }

  /**
   * 描述 获得过去的几天
   */
  public static java.sql.Date getBack(String time, int dsize) {

    Date date = new Date(getLongDate2(time));
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, -dsize);//把日期往后增加一天.整数往后推,负数往前移动
    date = calendar.getTime(); //这个时间就是日期往后推一天的结果
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //String dateString = formatter.format(date);
    return new java.sql.Date(date.getTime());
  }

  public static java.sql.Date getYesterDay() {
    Date date = new Date();
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, -1);//把日期往后增加一天.整数往后推,负数往前移动
    date = calendar.getTime(); //这个时间就是日期往后推一天的结果
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //String dateString = formatter.format(date);
    return new java.sql.Date(date.getTime());

  }

  public static java.sql.Date getYesterDay(Date date) {

    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, -1);//把日期往后增加一天.整数往后推,负数往前移动
    date = calendar.getTime(); //这个时间就是日期往后推一天的结果
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //String dateString = formatter.format(date);
    return new java.sql.Date(date.getTime());

  }

  public static java.sql.Date getLastDate(Date date, int size) {

    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, -size);//把日期往后增加一天.整数往后推,负数往前移动
    date = calendar.getTime(); //这个时间就是日期往后推一天的结果
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //String dateString = formatter.format(date);
    return new java.sql.Date(date.getTime());

  }

  public static java.sql.Date getBeforeYesterDay() {
    Date date = new Date();
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, -2);//把日期往后增加一天.整数往后推,负数往前移动
    date = calendar.getTime(); //这个时间就是日期往后推一天的结果
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //String dateString = formatter.format(date);
    return new java.sql.Date(date.getTime());

  }

  public static java.sql.Date getBeforeYesterDay(Date date) {

    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, -2);//把日期往后增加一天.整数往后推,负数往前移动
    date = calendar.getTime(); //这个时间就是日期往后推一天的结果
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //String dateString = formatter.format(date);
    return new java.sql.Date(date.getTime());

  }

  /**
   * HH:mm
   */
  public static String getSimpltTime(long time) {

    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(time);
    SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm");
    return dateformat.format(c.getTime());
  }


  /**
   * yyyy-mm-dd
   */
  public static String getDate(Date date) {

    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    return dateformat.format(date);
  }

  /**
   * yyyy-mm-dd
   */
  public static String getNumberDate(Date date) {

    SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
    return dateformat.format(date);
  }

  /**
   * HH:mm:ss
   */
  public static String getTime(long time) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(time);
    SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
    return dateformat.format(c.getTime());
  }

  /**
   * HH:mm:ss
   */
  public static String getTime(Date date) {
    SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
    return dateformat.format(date);
  }

  /**
   * yyyy-mm-dd HH:mm:ss
   */
  public static String getDateTime(long time) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(time);
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return dateformat.format(c.getTime());
  }

  /**
   * yy/MM/dd
   */
  public static String getSimpleMonth(long time) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(time);
    SimpleDateFormat dateformat = new SimpleDateFormat("yy/MM/dd");
    return dateformat.format(c.getTime());
  }

  /**
   * mm/dd HH
   */
  public static String getSimpleDate(long time) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(time);
    SimpleDateFormat dateformat = new SimpleDateFormat("MM/dd HH");
    return dateformat.format(c.getTime());
  }

  /**
   * mm-dd HH:mm
   */
  public static String getSimpleDateTime(long time) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(time);
    SimpleDateFormat dateformat = new SimpleDateFormat("MM-dd HH:mm");
    return dateformat.format(c.getTime());
  }

  /**
   * yyyy-mm-dd HH:mm:ss
   */
  public static String getDateTime(Date date) {
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return dateformat.format(date);
  }

  /**
   * yyyy-mm-dd HH:mm
   */
  public static String getDateTime2(Date date) {
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    return dateformat.format(date);
  }

  /**
   * 返回timeinmills
   *
   * @param yyyy-MM-dd HH:mm:ss
   */
  public static Long getLongDate(String date) {

    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    try {
      return dateformat.parse(date).getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return 0L;
  }

  /**
   * 返回timeinmills
   *
   * @param yyyy-MM-dd HH:mm:ss
   */
  public static Long getLongDate3(String date) {

    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    try {
      return dateformat.parse(date).getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return 0L;
  }

  public static Long getLongDate2(String date) {

    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      return dateformat.parse(date).getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return 0L;
  }


  /**
   * 返回timeinmills
   */
  public static Long getLongDay(String date) {
    if (date == null || "".equals(date)) {
      return null;
    }
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      return dateformat.parse(date).getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return 0L;
  }

  /**
   * 返回timeinmills
   */
  public static Long getLongDayNoDay(String date) {
    if (date == null || "".equals(date)) {
      return null;
    }
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM");
    try {
      return dateformat.parse(date).getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return 0L;
  }

  /**
   * 返回timeinmills
   */
  public static Long getLongDayYear(String date) {
    if (date == null || "".equals(date)) {
      return null;
    }
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy");
    try {
      return dateformat.parse(date).getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return 0L;
  }

  /**
   * 返回timeinmills
   *
   * @param yyyy-MM-dd HH:mm
   */
  public static Long getLongDateExcepSec(String date) {
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    try {
      return dateformat.parse(date).getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return 0L;
  }

  /**
   * 取得 yyyy-MM
   */
  public static String getMonth(long date) {
    //		date = date/1000;//s
    //		date += timeZone.getRawOffset();
    //		long s = date % 60;
    //		long m = (date % 3600)/60;
    //		long h = (date % 86400)/3600 ;
    //		System.out.println("s="+s+",m="+m+",h="+h);
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM");
    return dateformat.format(new Date(date));
  }

  /**
   * 取得date中的年月日时间，忽略时分秒。即xxxx年xx月xx日0时0分0秒 输入输出参数都是the difference, measured in milliseconds,
   * between the current time and midnight, January 1, 1970 UTC.
   */
  public static long getDayTime(long date) {

    date += offHour * 3600000;
    return date - date % 86400000 - offHour * 3600000;
  }

  /**
   * 计算在sTime时间开始持续tTime秒的东西，到目前为止，还有多少秒 sTime是毫秒时间，tTime为秒
   */
  public static int getRemainSeconds(int tTime, long sTime) {
    long df = tTime - System.currentTimeMillis() / 1000 + sTime / 1000;
    return (int) (df < 0 ? 0 : df);
  }

  /**
   * t1 - t1的日期差多少日 t1,t2为long型时间
   */
  public static int dayDiff(long t1, long t2) {
    //		Calendar c1 = Calendar.getInstance();
    //		c1.setTimeInMillis(t1);
    //		Calendar c2 = Calendar.getInstance();
    //		c2.setTimeInMillis(t2);
    //
    //		c1.set(Calendar.HOUR_OF_DAY, 0);
    //	    c1.set(Calendar.MINUTE, 0);
    //	    c1.set(Calendar.SECOND, 0);
    //	    c1.set(Calendar.MILLISECOND, 0);
    //
    //	    c2.set(Calendar.HOUR_OF_DAY, 0);
    //	    c2.set(Calendar.MINUTE, 0);
    //	    c2.set(Calendar.SECOND, 0);
    //	    c2.set(Calendar.MILLISECOND, 0);
    //
    //		long diff = c1.getTimeInMillis() - c2.getTimeInMillis();
    //		return (int)(diff /(24 * 3600* 1000));

    //		t1 = t1/1000;
    //		t1 = t1 - t1%86400/* - offHour*3600*/;
    //		t2 = t2/1000;
    //		t2 = t2 - t2%86400/* - offHour*3600*/;
    //		return (int) ((t1-t2)/86400);

    t1 = t1 - t1 % 86400000;
    t2 = t2 - t2 % 86400000;
    return (int) ((t1 - t2) / 86400000);
  }

  /**
   * 描述
   *
   * @param t1 以后的时间
   * @param t2 以前的时间
   */
  public static long dayDiff(java.sql.Date t1, java.sql.Date t2) {

    long st1 = t1.getTime();
    long st2 = t2.getTime();

    return Math.abs(st1 - st2);
  }

  /**
   * 描述
   *
   * @return 判断当前日期为星期几
   */
  public static int dayForWeek(String pTime) throws Exception {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Calendar c = Calendar.getInstance();
    c.setTime(format.parse(pTime));
    int dayForWeek = 0;
    if (c.get(Calendar.DAY_OF_WEEK) == 1) {
      dayForWeek = 7;
    } else {
      dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
    }
    return dayForWeek;
  }

  public static void main(String[] args) {
    System.out.println(getDateTime(TimeDateUtil.getDayTime(System.currentTimeMillis())));
    System.out.println(
        getDateTime(TimeDateUtil.getDayTime(System.currentTimeMillis()) + 24 * 3600 * 1000));
    System.out.println(getDateTime(System.currentTimeMillis()));
    System.out.println(getMonth(System.currentTimeMillis()));
    //		long st = System.currentTimeMillis();
    //		int i=1000;
    //		while(i-->0)
    //			dayDiff(1281494058406L,1283214328406L);
    //		long et = System.currentTimeMillis();
    //		System.out.println(et-st);
    //		long d = 1281494058406L;
    //		System.out.println(getDateTime(d));
    //		System.out.println(d);
    //		d=d/1000;
    //		System.out.println(d);
    //		d=d - d%(24*3600) - 8*3600;
    //		//d = d&0xFFFFFFF8;
    //		System.out.println(d);
    //		System.out.println(getDateTime(d*1000));
    //		long d1=1283214328406L;
    //		d1 = d1/1000;
    //		d1 = d1 - d1%(24*3600) - 8*3600;
    //
    //		System.out.println((d-d1)/(24*3600));
    System.out.println(dayDiff(1281494058406L, 1283214328406L));
    //		TimeZone tz = TimeZone.getDefault();
    //		System.out.println(tz.getRawOffset()/3600000);

    System.out.println(getLastWeekBeginDay());
    System.out.println(getLastWeekEndDay());
  }

  public static java.sql.Date asStringToSDate(String wstart) {

    return new java.sql.Date(getLongDate2(wstart));
  }

  public static long getMonth(Date date, int size) {

    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    //把日期往后增加一天.整数往后推,负数往前移动

    calendar.add(Calendar.MONTH, -size);    //得到前ji个月
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    calendar.set(year, month, 1, 0, 0);
    //注意月份加一
    //这个时间就是日期往后推一天的结果
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //String dateString = formatter.format(date);
    return calendar.getTimeInMillis();
    //return month;

  }

  public static String getMonthString(Date date, int size) {

    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    //把日期往后增加一天.整数往后推,负数往前移动

    calendar.add(Calendar.MONTH, -size);
    //得到前ji个月
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    calendar.set(year, month, 1, 0, 0);
    //注意月份加一
    //这个时间就是日期往后推一天的结果
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    String dateString = formatter.format(calendar.getTime());
    return dateString;
    //return month;

  }

  public static String getLastDayOfMonth(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    cal.add(Calendar.MONTH, 0);    //得到前ji个月
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH);

    cal.set(year, month, 1, 0, 0);

    cal.setTime(cal.getTime());
    cal.add(Calendar.DATE, -1);

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    String dateString = formatter.format(cal.getTime());
    return dateString;// 获得月末是几号
  }

  public static String getMonthString(Date date, int ysize, int msize, int dsize) {

    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    //把日期往后增加一天.整数往后推,负数往前移动

    calendar.add(Calendar.YEAR, -ysize);
    calendar.add(Calendar.MONTH, -msize);
    calendar.add(Calendar.DATE, -dsize); //得到前ji个月
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DATE);
    calendar.set(year, month, day, 0, 0);
    //注意月份加一
    //这个时间就是日期往后推一天的结果
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    String dateString = formatter.format(calendar.getTime());
    return dateString;
    //return month;

  }

  //获得上一个月的月初
  public static java.sql.Date getLastMouth(String time, int size) {
    Date date = new Date(getLongDate2(time));
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    //把日期往后增加一天.整数往后推,负数往前移动

    calendar.add(Calendar.MONTH, -size);    //得到前ji个月
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH) + 1;
    calendar.set(year, month, 1, 0, 0);
    //注意月份加一
    date = calendar.getTime(); //这个时间就是日期往后推一天的结果
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //String dateString = formatter.format(date);
    return new java.sql.Date(date.getTime());
    //return month;

  }

  public static int getLastMouth2(String time, int size) {
    Date date = new Date(getLongDate2(time));
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    //把日期往后增加一天.整数往后推,负数往前移动
    date = calendar.getTime();
    calendar.add(Calendar.MONTH, -size);    //得到前一个月
    //int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH) + 1;
    //注意月份加一

    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //String dateString = formatter.format(date);
    return month;
    //return month;

  }

  /**
   * 时间向前推进 i s 的时间点
   *
   * @param i 秒数
   */
  public static Timestamp getForwardTime(Timestamp time, long i) {

    return new Timestamp(time.getTime() + i);
  }

  public static int timetoInt(long tm) {
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");

    return Integer.parseInt(dateformat.format(new Date(tm)));
  }

  /**
   * 现在过去多少小时 的小时数
   */
  public static long getHours(int h) {

    return new Date().getTime() / (1000 * 60 * 60) - h;
  }

  public static long getHours() {

    return new Date().getTime() / (1000 * 60 * 60);
  }

  public static long getMinuts(int m) {
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");

    return TimeDateUtil.getLongDate2(dateformat.format(new Date())) / (1000 * 60) - m;
  }

  public static long getMinuts() {
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    return TimeDateUtil.getLongDate2(dateformat.format(new Date())) / (1000 * 60);
  }

  public static long getMinuts(int m, long stime) {

    return stime / (1000 * 60) - m;
  }

  public static long getMinuts(long etime) {

    return etime / (1000 * 60);
  }

  public static String getInitTime() {
    Date date = new Date();
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");

    //        Calendar cale = Calendar.getInstance();
    //        cale.setTime(date);
    //        cale.add(Calendar.DATE,0);
    ////        String sdate = "2013-12-4";
    //  Calendar calendar = new GregorianCalendar();
    //   calendar.set(2000, 0, 1);
    //
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(TimeDateUtil.getLongDate2(dateformat.format(date)));

    calendar.add(Calendar.DATE, -3);//把日期往后增加一天.整数往后推,负数往前移动
    date = calendar.getTime();

    return dateformat.format(date);

  }

  public static String getSDate(Date date) {
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, -1);//把日期往后增加一天.整数往后推,负数往前移动
    date = calendar.getTime();
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    return dateformat.format(date);
  }

  /**
   * 将传入的字符串按yyyy-MM-dd HH:mm:ss格式转换成对应的日期对象
   *
   * @param str 需要转换的字符串
   */
  public synchronized static Date StringToDateTime(String str) {
    String _pattern = "yyyy-MM-dd HH:mm:ss";
    return StringToDate(str, _pattern);
  }

  /**
   * 将插入的字符串按格式转换成对应的日期对象
   *
   * @param str 字符串
   * @param pattern 格式
   */
  public synchronized static Date StringToDate(String str, String pattern) {
    Date dateTime = null;
    try {
      if (str != null && !str.equals("")) {
        SimpleDateFormat formater = new SimpleDateFormat(pattern);
        dateTime = formater.parse(str);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return dateTime;
  }

  /**
   * 两个时间相差的月数
   */
  public static int calculateMonthIn(Date date1, Date date2) {
    Calendar cal1 = new GregorianCalendar();
    cal1.setTime(date1);
    Calendar cal2 = new GregorianCalendar();
    cal2.setTime(date2);
    int c = (cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR)) * 12
        + cal1.get(Calendar.MONTH) - cal2.get(Calendar.MONTH);
    return c;
  }

  /**
   * 获得下几个月的时间对象
   */
  public static Date getNextMonth(Date date, int count) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.MONTH, count);
    return c.getTime();
  }

  /**
   * 获得星座的 月 日的整数
   */
  public static Integer getXinZuoDate(long currentTimeMillis) {

    SimpleDateFormat dateformat = new SimpleDateFormat("MMdd");

    return Integer.valueOf(dateformat.format(new Date(currentTimeMillis)));
  }

  public static String getDate4(long currentTimeMillis) {
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
    return dateformat.format(new Date(currentTimeMillis));
  }

  public static String getDate5(long currentTimeMillis) {

    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy");
    return dateformat.format(new Date(currentTimeMillis));

  }

  public static String getDateNum(Date date) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * reset the time fields with values
   *
   * @param date origin time
   * @param hour hour field value
   * @param minute minute filed value
   * @param second second value t
   * @param millisecond millisecond field value
   * @return reset new date
   */
  public static Date resetTime(Date date, int hour, int minute, int second, int millisecond) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR, hour);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, second);
    calendar.set(Calendar.MILLISECOND, millisecond);
    return calendar.getTime();
  }

  /**
   * reset the time fields with values
   *
   * @param date origin time
   * @param minute minute filed value
   * @param second second value t
   * @param millisecond millisecond field value
   * @return reset new date
   */
  public static Date resetTime(Date date, int minute, int second, int millisecond) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, second);
    calendar.set(Calendar.MILLISECOND, millisecond);
    return calendar.getTime();
  }

  /**
   * reset the time fields with values
   *
   * @param date origin time
   * @param second second value t
   * @param millisecond millisecond field value
   * @return reset new date
   */
  public static Date resetTime(Date date, int second, int millisecond) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.SECOND, second);
    calendar.set(Calendar.MILLISECOND, millisecond);
    return calendar.getTime();
  }


  public static Date getBeginDayOfWeek() {
    Date date = new Date();
    if (date == null) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
    if (dayofweek == 1) {
      dayofweek += 7;
    }
    cal.add(Calendar.DATE, 2 - dayofweek);
    return getDayStartTime(cal.getTime());
  }


  public static Date getLastWeekBeginDay() {
    Date date = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
    if (dayofweek == 1) {
      dayofweek += 7;
    }
    cal.add(Calendar.DATE, 2 - dayofweek-7);
    return getDayStartTime(cal.getTime());
  }

  public static Date getEndDayOfWeek() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(getBeginDayOfWeek());
    cal.add(Calendar.DAY_OF_WEEK, 6);
    Date weekEndSta = cal.getTime();
    return getDayEndTime(weekEndSta);
  }


  public static Date getLastWeekEndDay() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(getLastWeekBeginDay());
    cal.add(Calendar.DAY_OF_WEEK, 6);
    Date weekEndSta = cal.getTime();
    return getDayEndTime(weekEndSta);
  }

  public static Timestamp getDayStartTime(Date d) {
    Calendar calendar = Calendar.getInstance();
    if (null != d) {
      calendar.setTime(d);
    }
    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return new Timestamp(calendar.getTimeInMillis());
  }

  public static Timestamp getDayEndTime(Date d) {
    Calendar calendar = Calendar.getInstance();
    if (null != d) {
      calendar.setTime(d);
    }
    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return new Timestamp(calendar.getTimeInMillis());
  }


  public static Date getYesterdayBeginTime() {
    final Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, -1);
    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }


  public static Date getTodayBeginTime() {
    final Calendar calendar = Calendar.getInstance();
    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  /**
   * 将日期字符串转换成 Date
   *
   */
  public static Date getDateTimeStringAsDate(String dateTime) throws ParseException {
    final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    return dateformat.parse(dateTime);
  }

  /**
   * 将相对时间(ns)转换成日期格式
   * @param now ns
   * @return String
   */
  public static String getTimeNanoSecAsString(long now) {
    final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return dateformat.format(now / 1000 / 1000);
  }

  /**
   * 获取特定时间点(ms)设定时间间隔前的时间
   * @param now ms
   * @param interval s
   * @return ns
   */
  public static long getXTimeBeforeNanoSec(long now, int interval) {
    now -= interval * 1000;
    return now * 1000 * 1000;
  }

  /**
   * 获取距离现在最近的influxdb可查询时间点
   * @param now ms
   * @return long ms
   */
  public static long getInfluxdbQueryPoint(long now) {
    //时间由ms转换成s从而去掉后面多余的尾数
    long queryPoint = now / 1000;

    int seconds = (int) queryPoint % 60;

    if (seconds == 0 || seconds == 30) {
      queryPoint -= 30;
    } else if (seconds > 30) {
      queryPoint = queryPoint - (seconds - 30);
    } else {
      queryPoint = queryPoint - seconds;
    }

    //时间由s转换成ms
    queryPoint *= 1000;
    return queryPoint;
  }

  /**
   * influxdb timezone格式时间转换为标准时间
   * @param time "yyyy-MM-dd'T'HH:mm:ssXXX"
   * @return String "yyyy-MM-dd HH:mm:ss"
   */
  public static String convertInfluxDBTimeToStandardDateTime(String time) throws ParseException{
    return getDateTime(getDateTimeStringAsDate(time));
  }

  /**
   * 纳秒转秒
   */
  public static int convertNanosToSeconds(long nanos) {
    return (int) (nanos / 1000 / 1000 / 1000);
  }
}

