# Tuning

## DStream
CMS Garbage Collector: Use of the concurrent mark-and-sweep GC is strongly recommended for keeping GC-related pauses consistently low. Even though concurrent GC is known to reduce the overall processing throughput of the system, its use is still recommended to achieve more consistent batch processing times. Make sure you set the CMS GC on both the driver (using --driver-java-options in spark-submit) and the executors (using Spark configuration spark.executor.extraJavaOptions).