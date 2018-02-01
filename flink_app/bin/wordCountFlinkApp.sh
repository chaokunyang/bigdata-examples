#!/bin/bash

master="yarn-cluster"
current=`cd $(dirname $0)/../;pwd`

function getExtraClasspath {
  result=""
  target_dir=$1
  for filename in ${target_dir}/*; do
    result="$result -C file://$filename"
  done
  echo ${result}
}

extraLib=`getExtraClasspath ${current}/lib`
conf=`getExtraClasspath ${current}/conf`
extra="${extraLib} ${conf}"
echo ${extra}

yarn application -kill  `yarn application -list | grep BoxLocationApp | awk '{print $1}'`

# set memory greater than 4G to avoid yarn killing container
# -yd can also be used for detaching YARN submission
#flink run -m ${master} -yn 3 -yjm 4096 -ytm 4096 -ys 8 -yt ${current}/lib/* -yt ${current}/conf/* -c com.timeyang.flink.streaming.WordCountApp ${current}/lib/flink_app-1.0-SNAPSHOT.jar

# -ylibjars can also be used for specifying jars
#flink run -m ${master} -yn 3 -yjm 4096 -ytm 4096 -ys 8 -c com.timeyang.flink.streaming.WordCountApp ${current}/lib/flink_app-1.0-SNAPSHOT.jar
flink run -m ${master} -yn 3 -yjm 4096 -ytm 4096 -ys 8 ${extra} -yd  -c com.timeyang.flink.streaming.WordCountApp ${current}/lib/flink_app-1.0-SNAPSHOT.jar
