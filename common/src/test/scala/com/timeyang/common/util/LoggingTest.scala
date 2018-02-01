package com.timeyang.common.util

import org.junit.Test

@Test
class LoggingTest extends Logging {

  @Test
  def test(): Unit = {
    info("test info")
    error("test error")
  }

}
