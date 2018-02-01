package com.timeyang.spark

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

import org.apache.spark.{HashPartitioner, Partitioner}

import org.apache.spark.rdd.RDD


/**
 * A general implementation of Secondary Sort
 */
object SecondarySort {

  // tag::sortByTwoKeys[]
  def sortByTwoKeys[K: Ordering : ClassTag,
  S: Ordering : ClassTag,
  V: ClassTag](
                pairRDD: RDD[((K, S), V)], partitions: Int): RDD[((K, S), V)] = {
    val colValuePartitioner = new PrimaryKeyPartitioner[K, S](partitions)

    // tag::implicitOrdering[]

    implicit val ordering: Ordering[(K, S)] = Ordering.Tuple2
    // end::implicitOrdering[]
    val sortedWithinParts = pairRDD.repartitionAndSortWithinPartitions(
      colValuePartitioner)
    sortedWithinParts
  }
  // end::sortByTwoKeys[]

  // tag::sortAndGroup[]
  def groupByKeyAndSortBySecondaryKey[K: Ordering : ClassTag,
  S: Ordering : ClassTag,
  V: ClassTag]
  (pairRDD: RDD[((K, S), V)], partitions: Int):
  RDD[(K, List[(S, V)])] = {
    // Create an instance of our custom partitioner
    val colValuePartitioner = new PrimaryKeyPartitioner[Double, Int](partitions)

    // define an implicit ordering, to order by the second key the ordering will
    // be used even though not explicitly called
    implicit val ordering: Ordering[(K, S)] = Ordering.Tuple2

    // use repartitionAndSortWithinPartitions
    val sortedWithinParts =
      pairRDD.repartitionAndSortWithinPartitions(colValuePartitioner)

    sortedWithinParts.mapPartitions(iter => groupSorted[K, S, V](iter))
  }

  def groupSorted[K, S, V](
                            it: Iterator[((K, S), V)]): Iterator[(K, List[(S, V)])] = {
    val res = List[(K, ArrayBuffer[(S, V)])]()
    it.foldLeft(res)((list, next) => list match {
      case Nil =>
        val ((firstKey, secondKey), value) = next
        List((firstKey, ArrayBuffer((secondKey, value))))

      case head :: _ =>
        val (curKey, valueBuf) = head
        val ((firstKey, secondKey), value) = next
        if (!firstKey.equals(curKey)) {
          (firstKey, ArrayBuffer((secondKey, value))) :: list
        } else {
          valueBuf.append((secondKey, value))
          list
        }

    }).map { case (key, buf) => (key, buf.toList) }.iterator
  }
  // end::sortAndGroup[]

}


class PrimaryKeyPartitioner[K, S](partitions: Int) extends Partitioner {
  /**
   * We create a hash partitioner and use it with the first set of keys.
   */
  val delegatePartitioner = new HashPartitioner(partitions)

  override def numPartitions: Int = delegatePartitioner.numPartitions

  /**
   * Partition according to the hash value of the first key
   */
  override def getPartition(key: Any): Int = {
    val k = key.asInstanceOf[(K, S)]
    delegatePartitioner.getPartition(k._1)
  }
}