package com.timeyang.flink.streaming

import com.timeyang.common.util.Logging
import org.apache.flink.api.scala._

object WordCountApp extends Logging {

  def main(args: Array[String]): Unit = {
    val env = ExecutionEnvironment.getExecutionEnvironment
    val input = env.fromElements("Please count", "the words", "but not this")
    input
      .filter(!_.contains("not"))
      .flatMap(_.split(" "))
      .map(word => (word, 1))
      .groupBy(0)
      .sum(1)
      .print()
  }

}
