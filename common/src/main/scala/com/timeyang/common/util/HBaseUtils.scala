package com.timeyang.common.util

import com.timeyang.common.config.BaseConf
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos
import org.apache.hadoop.hbase.util.Base64
import org.apache.hadoop.mapreduce.Job

/**
 * HBase工具类
 */
object HBaseUtils {

  val conf: Configuration = HBaseConfiguration.create()
  // should be set to a full list of ZooKeeper quorum servers.
  conf.set("hbase.zookeeper.quorum", BaseConf.zookeeperList)
  @transient
  lazy val connection: Connection = ConnectionFactory.createConnection(conf)

  def newConf(): Configuration = {
    val conf: Configuration = HBaseConfiguration.create()
    // should be set to a full list of ZooKeeper quorum servers.
    conf.set("hbase.zookeeper.quorum", BaseConf.zookeeperList)
    conf
  }

  def newConnection(): Connection = ConnectionFactory.createConnection(conf)

  /**
   * 如果指定表不存在，则创建相应的HBase表
   *
   * @param name        表名
   * @param familyNames 列簇名序列
   */
  def createHbaseTableIfAbsent(name: String, familyNames: String*): Unit = {
    val admin = HBaseUtils.connection.getAdmin
    try {
      val tableName = TableName.valueOf(name)
      if (!admin.tableExists(tableName)) {
        val htd = new HTableDescriptor(tableName)
        familyNames.foreach(familyName => htd.addFamily(new HColumnDescriptor(familyName)))
        admin.createTable(htd)
      }
    } finally {
      admin.close()
    }
  }

  /**
   * 转换scan为base64编码字符串
   *
   * @param scan scan对象
   * @return base64编码的字符串
   */
  def convertScanToString(scan: Scan): String = {
    val proto: ClientProtos.Scan = ProtobufUtil.toScan(scan)
    Base64.encodeBytes(proto.toByteArray)
  }

  /**
   * 创建一个批量写入hbase的job
   *
   * @param tableName 表名
   * @return job
   */
  def createHbaseOutputJob(tableName: String): Job = {
    val conf = HBaseUtils.newConf()
    conf.set(TableOutputFormat.OUTPUT_TABLE, tableName)
    val job = Job.getInstance(conf)
    job.setOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setOutputValueClass(classOf[Put])
    job.setOutputFormatClass(classOf[TableOutputFormat[ImmutableBytesWritable]])
    job
  }

}
