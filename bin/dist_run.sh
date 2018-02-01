#!/usr/bin/env bash
#/bin/sh
hosts="$(pwd)/hosts"

if [ ! -e ${hosts} ]; then
        echo "$hosts not exist"
        exit 0
fi

while read host
do
        if [ -n "$host" ]; then
                echo "Starting on $host"
                if [ ${host} = "localhost" ]; then
                        $1 < /dev/null
                else
                        ssh -Tq -p 22 ${host} $1 < /dev/null
                fi
                if [ $? -eq 0 ]; then
                        echo "$host DONE"
                else
                        echo "error: " $?
                fi
        fi
done < ${hosts}