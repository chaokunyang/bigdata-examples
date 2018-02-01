#!/bin/bash

current=`cd $(dirname $0)/../;pwd`

host_list="${current}/bin/hosts"
if [ ! -e ${host_list} ]; then
  echo "$host_list not exist"
  exit 0
fi

# 参数1 host，参数2 dir
function mkdirIfAbsent {
  target_host=$1
  target_dir=$2
  if ssh $target_host "[ ! -d ${target_dir} ]" < /dev/null; then
    ssh -Tq -p 22 ${target_host} "mkdir -p $target_dir" < /dev/null
    if [$? -eq 0]; then
      echo "$target_dir created"
    else
      echo "error: " $?
    fi
  else
    echo "$target_dir exists"
  fi
}


while read host
do
  if [ -n "$host" ]; then
     echo "Starting on $host"

     mkdirIfAbsent ${host} ${current}/lib
     mkdirIfAbsent ${host} ${current}/resources
     scp ../lib/* ${host}:${current}/lib
     scp ../resources/*.properties ${host}:${current}/resources/

     if [ $? -eq 0 ]; then
        echo "$host DONE"
     else
       echo "error: " $?
     fi
  fi
done < ${host_list}