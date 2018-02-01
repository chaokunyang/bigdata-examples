# Awesome Bigdata Samples

A curated list of awesome bigdata  applications, deploying, operations and monitoring.
## Environment
- Java: 1.8
- Scala: 2.11
- Python: 2.7
- Zookeeper: 3.4.6
- Hbase: 1.0.3
- Kafka: 0.10.0.1
- Redis: 3.2.6
- Hadoop: 2.6.5
- Spark: 2.2.1
- Flink: 1.4.0

## applications
- Spark Application
- Flink Application 

## deploying
Operate a server cluster is not easy. Write some scripts can help us ease  operations significantly. Here's some simple tools for this:
- `sync.sh`: recursively synchronize the files of current directory or specified directory and sub directory to same directory of all servers specified in hosts file.
- `del.sh`: delete current directory or specified directory of all servers specified in hosts file
- `dist_run.sh`: run a cmd on all servers specified in hosts

## operations
The scripts in awesome-bigdata-samples/bin provides some useful small operations tools to manage small and medium-sized server clusters. The details is as follows:
- `zk_admin.sh`: start or stop zookeeper cluster.
    - start zookeeper cluster: ```./zk_admin.sh start```
    - stop zookeeper cluster: ```./zk_admin.sh stop```
- `kafka_admin.sh`: start or stop kafka broker cluster.
    - start kafka broker cluster: ```./kafka_admin.sh start```
    - stop kafka broker cluster: ```./kafka_admin.sh stop```
- `rerun.py`: sometimes we may need to rerun some offline compute tasks for a couples of days. It would be tedious to rerun it one by one. `rerun.py` can be used to resolve scene like this. For example: ```python rerun.py -start 2017/11/21 -end 2017/12/01 -task dayJob.sh```

## monitoring
`monitor.py` in awesome-bigdata-samples/bin provides monitoring, auto recovery and alerting. The details is as follows:
- YarnChecker: monitor ResourceManager and NodeManagers
- HDFSChecker: monitor NameNode and DataNodes 
- ZookeeperChecker: monitor zookeeper nodes
- KafkaChecker: monitor kafka brokers
- HBaseChecker: monitor HMaster and HRegionServer
- RedisChecker: monitor redis server
- YarnAppChecker: monitor yarn application. useful for monitor spark streaming application and flink streaming application

## Style
- Scala: The scala code use programing style from [databricks](https://github.com/databricks/scala-style-guide), and is integrated in to maven build lifestyle using [scalastyle-maven-plugin](http://www.scalastyle.org/)
- Java: The scala code use programing style from [Apache Beam](https://github.com/apache/beam/blob/master/sdks/java/build-tools/src/main/resources/beam/checkstyle.xml)and is integrated in to maven build lifestyle using maven-checkstyle-plugin

##Run
Flink jobs containing Java 8 lambdas with generics cannot be compiled with IntelliJ IDEA at the moment. What you have to do is to build the project on the cli using `mvn compile` with **Eclipse JDT compiler**. Once the program has been built via maven, you can also run it from within IntelliJ.


##Build
```shell
mvn clean package -DskipTest -Pbuild-jar
```

##Contribute
- Source Code: https://github.com/chaokunyang/awesome-bigdata-samples
- Issue Tracker: https://github.com/chaokunyang/awesome-bigdata-samples/issues

## LICENSE
This project is licensed under Apache License 2.0.