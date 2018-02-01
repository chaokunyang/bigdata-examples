package com.timeyang.common.util

import scala.collection.JavaConverters._

import java.util
import org.junit.Test

@Test
class JsonUtilsTest {

  @Test
  def testList: Unit = {
    val json = """[1,2,3]"""

    // scalastyle:off println
    println(JsonUtils.toJson(List(1, 2, 3).asJava))
    println(JsonUtils.fromJson(json, classOf[Array[String]]).toList)
    println(JsonUtils.fromJson(json, classOf[util.ArrayList[String]]))
    // scalastyle:on println
  }

}
