#!/bin/bash

current=`cd $(dirname $0)/../;pwd`
target_file=${current}/lib/$1
host_list="$(pwd)/hosts"
if [ ! -e ${host_list} ]; then
    echo "${host_list} not exist"
    exit 0
fi

rm -r ${target_file}

while read host
do
    if [ -n "$host" ]; then
       echo "Starting on $host"

           ssh -Tq -p 22 ${host} "rm -f $target_file" < /dev/null > /dev/null

       if [ $? -eq 0 ]; then
            echo "$host DONE"
       else
           echo "error: " $?
       fi
    fi
done < ${host_list}