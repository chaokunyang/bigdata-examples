package com.timeyang.common.util

import java.lang.reflect.Type

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.google.gson.Gson

/**
 * Json工具类
 */
object JsonUtils {

  private val GSON = new Gson()
  private val mapper = {
    val _mapper = new ObjectMapper()
    // to allow serialization of "empty" POJOs (no properties to serialize)
    // (without this setting, an exception is thrown in those cases)
    _mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    // to write java.util.Date, Calendar as number (timestamp):
    _mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    _mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    _mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
  }

  /**
   * 对象转换为json
   *
   * @param obj Bean
   * @return json
   */
  def toJson(obj: AnyRef): String = GSON.toJson(obj)

  /**
   * 把json解析为实体对象。自动忽略未知的字段
   *
   * @param json json字符串
   * @param clz  实体类对象
   * @tparam T 实体类型
   * @return 实体
   */
  // Gson will ignore the unknown fields and simply match the fields that it’s able to.
  // def fromJson[T : Manifest](json: String, clz: Class[T]): T = {
  //   implicit val formats: DefaultFormats.type = DefaultFormats
  //   parse(json).extract[T]
  // }
  def fromJson[T](json: String, clz: Class[T]): T = GSON.fromJson(json, clz)

  def fromJsonToBean[T](json: String, clz: Class[T]): T = mapper.readValue(json, clz)

  def fromJson[T](json: String, _type: Type): T = GSON.fromJson(json, _type)

}
