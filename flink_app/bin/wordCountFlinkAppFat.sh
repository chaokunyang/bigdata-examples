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

conf=`getExtraClasspath ${current}/conf`
extra="${conf}"
echo ${extra}

yarn application -kill  `yarn application -list | grep BoxLocationApp | awk '{print $1}'`

flink run -m ${master} -yn 3 -yjm 4096 -ytm 4096 -ys 8 -yd ${extra}  -c com.timeyang.flink.streaming.WordCountApp ${current}/flink_app-1.0-SNAPSHOT.jar
