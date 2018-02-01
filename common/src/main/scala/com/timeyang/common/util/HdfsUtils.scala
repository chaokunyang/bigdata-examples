package com.timeyang.common.util

import com.timeyang.common.config.BaseConf
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

/**
 * HDFS 工具，仅在driver中使用
 * <p>note: fs被复制到worker后无法使用</p>
 */
object HdfsUtils {

  val HDFS_URL: String = BaseConf.hdfsUrl // hdfs://master:9000

  private val conf = new Configuration
  conf.set("fs.defaultFS", HDFS_URL)
  private val fs: FileSystem = FileSystem.get(conf)


  def delete(pathStr: String): Unit = {
    val p = if (pathStr.startsWith(HDFS_URL)) pathStr.substring(HDFS_URL.length) else pathStr
    val path = new Path(p)
    if (fs.exists(path)) {
      fs.delete(path, true)
    }
  }

  def isExists(testPath: String): Boolean = {
    val p = if (testPath.startsWith(HDFS_URL)) testPath.substring(HDFS_URL.length) else testPath
    val path = new Path(p)
    if (fs.exists(path)) true else false
  }

}
