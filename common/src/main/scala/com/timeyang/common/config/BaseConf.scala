package com.timeyang.common.config

import com.timeyang.common.util.ResourceUtils

/**
 * 基础组件配置
 */
object BaseConf {

  private val baseProperties = ResourceUtils.getProperties("base.properties")

  /** zookeeper list */
  val zookeeperList: String = baseProperties.getProperty("zookeeper.list")
  /** kafka brokers list */
  val kafkaBrokerList: String = baseProperties.getProperty("broker.list")
  /** hdfs url */
  val hdfsUrl: String = baseProperties.getProperty("hdfs.url")
  /** redis host */
  val redisHost: String = baseProperties.getProperty("redis.host.name")
  /** redis port */
  val redisPort: Int = baseProperties.getProperty("redis.host.port").toInt

}
