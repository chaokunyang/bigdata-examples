package com.timeyang.common.util

import java.io.{FileNotFoundException, InputStream, IOException}
import java.util.Properties

import scala.collection.JavaConverters._
import scala.io.Source

/**
 * 资源工具类
 */
object ResourceUtils {

  /**
   * 资源是否存在
   *
   * @param resourceName 资源名称
   * @return 资源是否存在
   */
  def existsResources(resourceName: String): Boolean = {
    val loader = Thread.currentThread().getContextClassLoader
    val url = loader.getResource(resourceName)
    var stream: InputStream = null
    try {
      if (url != null) {
        stream = url.openStream
      }
    } catch {
      case _: IOException =>
    } finally {
      if (stream != null) {
        stream.close()
      }
    }

    stream != null
  }

  /**
   * 获取properties
   *
   * @param propertiesName properties文件名
   * @return properties对象
   */
  def getProperties(propertiesName : String) : Properties = {
    val properties = new Properties()
    try {
      val list = Source.fromFile("conf.properties").getLines().toList
      val map = list
        .filter(_.trim.length > 0)
        .map(line => {
          val splits = line.split("=")
          val key = splits(0).trim
          val value = splits(1).trim
          (key, value)
        })
        .toMap.asJava

      properties.putAll(map)
    } catch {
      case _: FileNotFoundException =>
        // val loader = Thread.currentThread().getContextClassLoader
        val loader = this.getClass.getClassLoader
        autoClose(loader.getResourceAsStream(propertiesName)) {properties.load}
    }

    properties
  }

  /**
   * 模仿Java的Try With Resource语法，自动关闭资源
   *
   * @param closeable 可关闭的资源
   * @param fun       function
   * @tparam A 可关闭的资源的子类
   * @tparam B 返回结果类型
   * @return
   */
  def autoClose[A <: AutoCloseable, B](closeable: A)(fun: (A) ⇒ B): B = {
    try {
      fun(closeable)
    } finally {
      closeable.close()
    }
  }
}
