package com.timeyang.common.util

import java.util.Properties

import com.timeyang.common.config.BaseConf
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

object KafkaProducerUtils {

  @volatile lazy private val producer: KafkaProducer[String, String] = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BaseConf.kafkaBrokerList)
    props.put("acks", "all")
    props.put("retries", 1: Integer)
    props.put("batch.size", 16384: Integer)
    props.put("linger.ms", 1: Integer)
    props.put("buffer.memory", 33554432: Integer)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])

    new KafkaProducer[String, String](props)
  }

  def sendJsonMessages(topic: String, event: Object, events: Object*): Unit = {
    for (event <- event +: events) {
      val record = new ProducerRecord[String, String](topic, null, JsonUtils.toJson(event))
      producer.send(record)
    }
  }

  def send(topic: String, events: List[Object]): Unit = {
    for (event <- events) {
      val record = new ProducerRecord[String, String](topic, null, JsonUtils.toJson(event))
      producer.send(record)
    }
  }

  def send(topic: String, event: Object): Unit = {
    val record = new ProducerRecord[String, String](topic, null, JsonUtils.toJson(event))
    producer.send(record)
  }

}
