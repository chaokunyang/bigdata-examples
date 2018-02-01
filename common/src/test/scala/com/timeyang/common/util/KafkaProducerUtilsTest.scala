package com.timeyang.common.util

import org.junit.Test

@Test
class KafkaProducerUtilsTest {

  @Test
  def testSend(): Unit = {
    (0 to 50).foreach(i => {
      KafkaProducerUtils.send("test1", s"hello$i")
    })
  }

}
