#!/bin/bash
hosts="$(pwd)/hosts"

echo 'You are executing' $1
ZOOKEEPER_HOME="$(pwd)"
case $1 in
  start) remote_cmd="$ZOOKEEPER_HOME/bin/zkServer.sh start"
  ;;
  stop) remote_cmd="$ZOOKEEPER_HOME/bin/zkServer.sh stop"
  ;;
  *) echo 'wrong cmd args, please input start/stop'
  exit 0
  ;;
esac

if [ ! -e ${hosts} ]; then
    echo "$hosts not exist"
    exit 0
fi

while read host
do
    if [ -n "$host" ]; then
        echo "Starting on $host"
        if [ ${host} = "localhost" ]; then
            "$remote_cmd" < /dev/null > /dev/null
        else
            ssh -Tq -p 22 ${host} "$remote_cmd" < /dev/null > /dev/null
        fi
        if [ $? -eq 0 ]; then
            echo "DONE"
        else
            echo "error: " $?
        fi
    fi
done < ${hosts}

