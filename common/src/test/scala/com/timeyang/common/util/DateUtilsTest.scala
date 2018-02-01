package com.timeyang.common.util

import java.text.SimpleDateFormat
import java.util.Calendar

import org.junit.Test

/**
 * 日期工具测试
 */
@Test
class DateUtilsTest {

  @Test
  def test(): Unit = {
    // scalastyle:off
    println(DateUtils.current())

    val formatter = new SimpleDateFormat("yyyyMMddHH")
    println(formatter.format(System.currentTimeMillis()))
    // scalastyle:on
  }

  @Test
  def testTime(): Unit = {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    // scalastyle:off println
    println(hour)
    // scalastyle:on println
    calendar.add(Calendar.MILLISECOND, 60 * 60 * 1000)
    val hourOfNext = calendar.get(Calendar.HOUR_OF_DAY)
    print(hourOfNext)
  }

}
