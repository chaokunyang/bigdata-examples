#!/bin/bash

script_path=`readlink -f $0`
script_dir=`dirname ${script_path}`

path=`pwd`
if [ "$1" != "" ]; then
  path=$1
fi


host_list="${script_dir}/hosts"
if [ ! -e ${host_list} ]; then
  echo "$host_list not exist"
  exit 0
fi


while read host
do
  if [ -n "$host" ]; then
     echo "Starting on $host"

     ssh ${host} "rm -rf ${path}/"

     if [ $? -eq 0 ]; then
        echo "$host DONE"
     else
       echo "error: " $?
     fi
  fi
done < ${host_list}
