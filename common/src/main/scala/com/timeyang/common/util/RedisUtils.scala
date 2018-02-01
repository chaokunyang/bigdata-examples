package com.timeyang.common.util

import com.timeyang.common.config.BaseConf
import com.redis.{RedisClient, RedisClientPool}
import com.redis.RedisClient.{ASC, DESC}

/**
 * Redis工具类
 */
object RedisUtils {

  private val redisClientPool: RedisClientPool =
    new RedisClientPool(BaseConf.redisHost, BaseConf.redisPort)

  def getRedisClient: RedisClientPool = redisClientPool

  /**
   * 使用redis client执行访问redis
   *
   * @param func function
   */
  def withClient[T](func: RedisClient => T): T = redisClientPool.withClient(func)


  def expire(key: Any, ttl: Int): Boolean = withClient(_.expire(key, ttl))

  def expireat(key: Any, timestamp: Long): Boolean = withClient(_.expireat(key, timestamp))

  /**
   * 从世界时开始，以duration划分时间，在最新时刻expire指定key
   * @param key redis key
   * @param duration 时间分段。单位：ms
   * @return return true if expireat call success
   */
  def expireAtNextDurationMoment(key: Any, duration: Long): Boolean = {
    val timestamp = System.currentTimeMillis()
    val expireat = ((timestamp / duration) + 1) * duration
    if (expireat != timestamp) {
      withClient(_.expireat(key, expireat))
    } else {
      false
    }
  }


  ///////////////////////////////////////////////////////////////////////////
  // string operations
  ///////////////////////////////////////////////////////////////////////////
  def set(key: Any, value: Any): Boolean = withClient(_.set(key, value))

  def setex(key: Any, expiry: Long, value: Any): Boolean = withClient(_.setex(key, expiry, value))


  // list operations
  def lpush(key: Any, value: Any, values: Any*): Option[Long] =
    withClient(_.lpush(key, value, values: _*))

  def rpush(key: Any, value: Any, values: Any*): Option[Long] =
    withClient(_.rpush(key, value, values: _*))

  def ltrim(key: Any, start: Int, end: Int): Boolean =
    withClient(_.ltrim(key, start, end))

  def lrange(key: Any, start: Int, end: Int): Option[List[Option[String]]] =
    withClient(_.lrange(key, start, end))


  // hash operations
  def hget(key: Any, field: Any): Option[String] = withClient(_.hget(key, field))

  def hset(key: Any, field: Any, value: Any): Boolean = withClient(_.hset(key, field, value))

  def hexists(key: Any, field: Any): Boolean = withClient(_.hexists(key, field))



  // set operations
  def scard(key: Any): Option[Long] = withClient(_.scard(key))


  // sorted set operations
  def zadd(key: Any, score: Double, member: Any, scoreValues: (Double, Any)*): Option[Long] =
    withClient(_.zadd(key, score, member, scoreValues: _*))

  def zremrangebyscore(
                        key: Any,
                        start: Double = Double.NegativeInfinity,
                        end: Double = Double.PositiveInfinity)
  : Option[Long] = withClient(_.zremrangebyscore(key, start, end))

  def zrange[A](
      key: Any,
      start: Int = 0,
      end: Int = -1,
      asc: Boolean = true): Option[List[String]] =
    withClient(_.zrange(key, start, end, if (asc) ASC else DESC))

  def zrangeWithScore[A](
      key: Any,
      start: Int = 0,
      end: Int = -1,
      asc: Boolean = true):
  Option[List[(String, Double)]] =
    withClient(_.zrangeWithScore(key, start, end, if (asc) ASC else DESC))

}
