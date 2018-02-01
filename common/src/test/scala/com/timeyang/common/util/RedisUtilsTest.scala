package com.timeyang.common.util

import org.junit.Test

@Test
class RedisUtilsTest {

  @Test
  def testVarargs(): Unit = {
    RedisUtils.withClient(client => {
      client.del("test")
      client.sadd("test", 0, Array(1, 2, 3): _*)
      client.expire("test", 60)

      // scalastyle:off println
      println(client.smembers("test"))
      // scalastyle:on println
    })
  }

}
