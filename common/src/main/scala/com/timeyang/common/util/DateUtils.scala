package com.timeyang.common.util

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

object DateUtils {

  /**
   * 获取指定格式的时间
   *
   * @param timestamp timestamp
   * @param format    时间格式
   * @return 指定格式的时间
   */
  def getTime(timestamp: Long, format: String): String = {
    val formatter = new SimpleDateFormat(format)
    formatter.format(timestamp)
  }

  /**
   * 获取指定格式的日期
   *
   * @param calendar  Calendar实例
   * @param formatStr 格式化字符串
   * @return 指定格式的日期
   */
  def getDay(calendar: Calendar, formatStr: String = "yyyyMMdd"): String = {
    val format = new SimpleDateFormat(formatStr)
    format.format(calendar.getTimeInMillis)
  }

  /**
   * 以当前日期为基准，获取某天日期
   *
   * @param offset    日期前后偏移
   * @param formatStr 日期格式
   * @return
   */
  def getSomeDay(offset: Int, formatStr: String = "yyyyMMdd"): String = {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, offset)
    val format = new SimpleDateFormat(formatStr)
    format.format(calendar.getTimeInMillis)
  }

  /**
   * 获取某天日期
   *
   * @param day       基准日期，格式：yyyy/MM/dd
   * @param offset    日期前后偏移
   * @param formatStr 日期格式
   * @return 某天日期
   */
  def getSomeDay(day: String, offset: Int, formatStr: String): String = {
    val calendar = parseDayToCalendar(day)
    calendar.add(Calendar.DAY_OF_MONTH, offset)
    val format = new SimpleDateFormat(formatStr)
    format.format(calendar.getTimeInMillis)
  }

  /**
   * 获取指定日期开始的指定数量的最近日期列表
   *
   * @param day 指定日期，格式：yyyy/MM/dd
   * @param num 待获取日期数量
   * @return List[String]，示例：List("2017/10/13", "2017/10/12")
   */
  def getRecentDays(day: String, num: Int, format: String = "yyyy/MM/dd"): List[String] = {
    val calendar = parseDayToCalendar(day)
    val time = calendar.getTimeInMillis

    val days = new ListBuffer[String]
    val formatter = new SimpleDateFormat(format)
    for (i <- -num + 1 to 0) {
      calendar.setTimeInMillis(time)
      calendar.add(Calendar.DAY_OF_MONTH, i)
      val d = formatter.format(calendar.getTimeInMillis)
      days.append(d)
    }

    days.toList
  }

  /**
   * 获取一月当中的所有日期
   *
   * @param month  月份。格式：yyyy/MM
   * @param format 日期格式
   * @return 一月当中的所有日期
   */
  def getDaysOfMonth(month: String, format: String = "yyyy/MM/dd"): List[String] = {
    val formatter = new SimpleDateFormat(format)

    val calendar = parseDayToCalendar(month + "/01")
    val start = calendar.get(Calendar.DAY_OF_MONTH)

    calendar.add(Calendar.MONTH, 1)
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    val end = calendar.get(Calendar.DAY_OF_MONTH)

    val days = new ArrayBuffer[String]()
    for (i <- start to end) {
      calendar.set(Calendar.DAY_OF_MONTH, i)
      days.append(formatter.format(calendar.getTimeInMillis))
    }

    days.toList
  }

  /**
   * 判断日期是否在周末
   *
   * @param day 日期格式：yyyyMMdd
   * @return 日期是否在周末
   */
  def onWeekend(day: String): Boolean = {
    val year = day.substring(0, 4).toInt
    val month = day.substring(4, 6).toInt
    val currentDay = day.substring(6, 8).toInt

    val calendar = Calendar.getInstance
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONDAY, month - 1) // month starts from 0
    calendar.set(Calendar.DAY_OF_MONTH, currentDay)

    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    dayOfWeek == 1 | dayOfWeek == 7
  }

  /**
   * 获取当月最后一天
   *
   * @param month 月份格式：yyyy/MM
   * @return 当月最后一天
   */
  def getLastDayOfMonth(month: String, format: String = "yyyy/MM/dd"): String = {
    val calendar = parseDayToCalendar(month + "/01")
    calendar.add(Calendar.MONTH, 1)
    calendar.add(Calendar.DAY_OF_MONTH, -1)

    val formatter = new SimpleDateFormat(format)
    formatter.format(calendar.getTimeInMillis)
  }

  /**
   * 判断指定日期是否是本月最后一天
   *
   * @param day 日期格式：yyyyMMdd
   * @return 指定日期是否是本月最后一天
   */
  def isLastDayOfMonth(day: String): Boolean = {
    val calendar = parseDayToCalendar(day)

    val month = calendar.get(Calendar.MONDAY)
    calendar.add(Calendar.DAY_OF_MONTH, 1)

    month != calendar.get(Calendar.MONDAY)
  }

  /**
   * 解析日期字符串为Calendar对象
   *
   * @param day 日期。格式：yyyy/MM/dd
   * @return Calendar对象
   */
  def parseDayToCalendar(day: String): Calendar = {
    val year = day.substring(0, 4).toInt
    val month = day.substring(5, 7).toInt
    val currentDay = day.substring(8, 10).toInt

    val calendar = Calendar.getInstance
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONDAY, month - 1) // month starts from 0
    calendar.set(Calendar.DAY_OF_MONTH, currentDay)

    calendar
  }

  /**
   * 返回指定格式的当前时刻
   *
   * @param format 格式
   * @return 指定格式的当前时刻
   */
  def current(format: String = "yyyyMMddHHmm"): String = {
    val date = new Date()
    val formatter = new SimpleDateFormat(format)
    formatter.format(date)
  }

  /**
   * 获取今天的日期
   *
   * @return 今天的日期，格式：yyyy/MM/dd
   */
  def today: String = DateUtils.getSomeDay(0, "yyyy/MM/dd")

  /**
   * 获取昨天的日期
   *
   * @return 昨天的日期，格式：yyyy/MM/dd
   */
  def yesterday: String = DateUtils.getSomeDay(-1, "yyyy/MM/dd")

  /**
   * 从命令行参数获取分析日期
   *
   * @param args 命令行参数(date)
   * @return 分析日期
   */
  def dayFromArgs(args: Array[String]): Option[String] = {
    if (args != null && args.length > 0) {
      val inputDay = args(0)
      if (inputDay != null && inputDay.indexOf("/") != -1) {
        Some(inputDay)
      } else {
        None
      }
    } else {
      None
    }
  }

}
