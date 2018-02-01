package com.timeyang.spark

import org.apache.spark.{SparkConf, SparkContext}

/**
 * Spark Utils
 */
object SparkUtils {

  def newSparkConf(appName: String): SparkConf = {
    val conf = new SparkConf().setAppName(appName)
    conf.set("spark.shuffle.consolidateFiles", "true")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.set("spark.default.parallelism", "50")
    conf
  }

  /**
   * get number of executors
   *
   * @param sc SparkContext
   * @return number of executors
   */
  def getExecutorNumbers(sc: SparkContext): Int = {
    sc.getExecutorMemoryStatus.size - 1 // minus driver
  }

  /**
   * Method that just returns the current active/registered executors excluding the driver.
   *
   * @param sc The spark context to retrieve registered executors.
   * @return a list of executors each in the form of host:port.
   */
  def currentActiveExecutors(sc: SparkContext): Seq[String] = {
    val allExecutors = sc.getExecutorMemoryStatus.keys
    val driverHost: String = sc.getConf.get("spark.driver.host")
    allExecutors.filter(host => !host.split(":")(0).equals(driverHost)).toList
  }

}
