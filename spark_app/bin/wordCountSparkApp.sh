#!/bin/bash

master="yarn-cluster"
current=`cd $(dirname $0)/../;pwd`

yarn application -kill  `yarn application -list | grep BoxStreamingApp | awk '{print $1}'`

# set executor-memory greater than 4G. If less than 4G，memory overhead will be 384M, will be wasteful relate to total memory。if memory to big, GC overload will be large
# executor-cores 5. to much is wasteful
# num-executors  <= CPU cores / 5
${SPARK_HOME}/spark-submit --master ${master} --conf spark.speculation=true --executor-memory 4G --executor-cores 5 --driver-memory 2G --class com.timeyang.spark.streaming.WordCountApp --conf spark.executor.extraClassPath=${current}/lib/*  --files ${current}/conf/base.properties#base.properties  --conf spark.driver.extraClassPath=${current}/lib/*  spark.executorEnv.JAVA_HOME=${JAVA_HOME}  ${current}/lib/spark_app-1.0-SNAPSHOT.jar
