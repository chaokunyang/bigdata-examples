# coding=utf-8
"""
监控Yarn、HDFS、HBase、SparkStreaming等应用
使用crontab定时调度：
*/5 * * * * cd /home/qjzh/7g_soft_ware/7g_abd_1.6/bin; python monitor.py >> monitor.out 2>&1
"""
import json
import logging
import urllib2
import subprocess
from subprocess import call
import os
from HTMLParser import HTMLParser
import re
import socket
import sys

slaves = ['slave1', 'slave2', 'slave3', 'slave4']


def get_logger():
    _logger = logging.getLogger("monitor tool")
    _logger.setLevel(logging.INFO)
    logger_handler = logging.FileHandler(os.getcwd() + '/monitor.log')
    formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")
    logger_handler.setFormatter(formatter)
    _logger.addHandler(logger_handler)
    return _logger


logger = get_logger()


def get_bin_path(cmd):
    """获取命令的绝对路径。不包含命令本身"""
    whole_cmd = "source /etc/profile; which {0}".format(cmd)
    out = subprocess.Popen(whole_cmd, stdout=subprocess.PIPE, shell=True).stdout.read()
    return out[: -len(cmd) - 1]


def get_env(env):
    """获取环境变量值"""
    try:
        return os.environ[env]
    except Exception:
        cmd = "source /etc/profile; echo ${0}".format(env)
        out = subprocess.Popen(cmd, stdout=subprocess.PIPE, shell=True).stdout.read()
        return out.strip("\n")


class YarnChecker:
    """Yarn 监控工具"""

    def __init__(self, yarn_conf):
        self.sbin_path = get_bin_path('start-yarn.sh')
        self.resourcemanager_webapp_address = yarn_conf['resourcemanager_webapp_address']

    def check(self):
        """check yarn state"""
        out = subprocess.Popen("source /etc/profile; jps | grep ResourceManager",
                               stdout=subprocess.PIPE, shell=True).stdout.read()
        if 'ResourceManager' not in out:
            logger.warn('ResourceManager is not running, start ResourceManager and NodeManagers now')
            cmd = '{0}/start-yarn.sh'.format(self.sbin_path)
            returncode = call(cmd)
            if returncode == 0:
                logger.info('started yarn successfully')
            else:
                logger.error('started yarn failed, start it again')
                call(cmd)
        else:
            url = '{0}/ws/v1/cluster/nodes'.format(self.resourcemanager_webapp_address)
            res = urllib2.urlopen(url).read()
            nodes = json.loads(res)['nodes']['node']
            not_running = []
            running = []
            for node in nodes:
                node_host = node['nodeHostName']
                if node['state'] != 'RUNNING':
                    not_running.append(node_host)
                else:
                    running.append(node_host)
            if not_running:
                logger.info('running nodemanagers: {0}'.format(running))
                logger.warn('nodemanagers on {0} are not running, start them respectively'.format(not_running))
                for node_host in not_running:
                    self.start_node_manager(node_host)

    def start_node_manager(self, node_host):
        """start nodemanager"""
        cmd = '{0}/yarn-daemon.sh start nodemanager'.format(self.sbin_path)
        call(['ssh', '-Tq', '-p', '22', node_host, cmd])
        logger.info('started nodemanager on {0}'.format(node_host))


class HDFSChecker:
    """HDFS 监控工具"""

    def __init__(self, hdfs_conf):
        self.sbin_path = get_bin_path('start-dfs.sh')
        self.namenode_http_address = hdfs_conf['namenode_http_address']

    def check(self):
        """check hdfs node"""
        out = subprocess.Popen("source /etc/profile; jps | grep NameNode",
                               stdout=subprocess.PIPE, shell=True).stdout.read()
        if 'NameNode' not in out:
            cmd = '{0}/start-dfs.sh'.format(self.sbin_path)
            logger.warn('NameNode is not running, start NameNode and DataNode now')
            returncode = call([cmd])
            if returncode == 0:
                logger.info('started dfs successfully')
            else:
                call([cmd])
        else:
            url = '{0}/jmx?qry=Hadoop:service=NameNode,name=NameNodeInfo'.format(self.namenode_http_address)
            res = urllib2.urlopen(url).read()
            live_nodes = json.loads(json.loads(res)['beans'][0]['LiveNodes'])
            dead_nodes = json.loads(json.loads(res)['beans'][0]['DeadNodes'])
            not_running = dead_nodes.keys()
            if not_running:
                logger.info('running datanodes: {0}'.format(live_nodes.keys()))
                logger.warn('datanodes on {0} are not running, start them respectively'.format(not_running))
                for host in not_running:
                    self.start_data_node(host)

    def start_data_node(self, host):
        """start datanode"""
        cmd = '{0}/hadoop-daemon.sh start datanode'.format(self.sbin_path)
        call(['ssh', '-Tq', '-p', '22', host, cmd])
        logger.info('started datanode on {0}'.format(host))


class ZookeeperChecker:
    """Zookeeper 监控工具"""

    def __init__(self, zk_conf):
        zk_servers = zk_conf['zk_servers'].split(',')
        self.hosts_ports = [(x.split(':')[0], int(x.split(':')[1])) for x in zk_servers]
        self.zk_home = get_env('ZOOKEEPER_HOME')

    def check(self):
        """check zookeeper nodes"""
        running_nodes = []
        not_running_nodes = []
        for host, port in self.hosts_ports:
            # cmd = "ssh -p 22 {0} 'source /etc/profile;{1}/bin/zkServer.sh status'".format(server_host, self.zk_home)
            # not_running = "Error" in subprocess.Popen(cmd, stdout=subprocess.PIPE, shell=True).stdout.read()
            try:
                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                s.settimeout(3)
                s.connect((host, port))
                running_nodes.append(host)
                # s.send('ruok')
                # if s.recv(1024) == 'imok':
                #     running_nodes.append(host)
                # else:
                #     not_running_nodes.append(host)
                s.close()
            except socket.error:
                not_running_nodes.append(host)
        if not_running_nodes:
            logger.info('zookeeper nodes are running on : {0}'.format(running_nodes))
            logger.warn('zookeeper nodes on {0} are not running, start them respectively'.format(not_running_nodes))
            for host in not_running_nodes:
                self.start_zk_node(host)

    def start_zk_node(self, host):
        """start zookeeper node"""
        # stop_cmd = "{0}/bin/zkServer.sh stop".format(self.zk_home)
        start_cmd = "source /etc/profile;ssh -p 22 {0} '{1}/bin/zkServer.sh start'".\
            format(host, self.zk_home)
        # call(['ssh', '-p', '22', host, stop_cmd])
        call(start_cmd, shell=True)
        logger.info('started zookeeper on {0}'.format(host))


class KafkaChecker:
    """Kafka 监控工具"""

    def __init__(self, kafka_conf):
        kafka_brokers = kafka_conf['kafka_brokers'].split(',')
        self.hosts_ports = [(x.split(':')[0], int(x.split(':')[1])) for x in kafka_brokers]
        self.kafka_home = get_env('KAFKA_HOME')

    def check(self):
        """check kafka brokers"""
        running_brokers = []
        not_running_brokers = []
        for host, port in self.hosts_ports:
            # cmd = "ssh {0} 'source /etc/profile; jps | grep Kafka'".format(broker_host)
            # running = 'Kafka' in subprocess.Popen(cmd, stdout=subprocess.PIPE, shell=True).stdout.read().strip()
            try:
                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                s.connect((host, port))
                running_brokers.append(host)
                s.close()
            except socket.error:
                not_running_brokers.append(host)
        if not_running_brokers:
            logger.info('running kafka brokers: {0}'.format(running_brokers))
            logger.warn('kafka brokers on {0} are not running, start them respectively'.format(not_running_brokers))
            for host in not_running_brokers:
                self.start_kafka_node(host)

    def start_kafka_node(self, host):
        """start kafka node"""
        cmd = "{0}/bin/kafka-server-start.sh -daemon {0}/config/server.properties".format(self.kafka_home)
        call(['ssh', '-p', '22', host, cmd])
        logger.info('started kafka broker on {0}'.format(host))


class HBaseChecker:
    """Hbase 监控工具"""

    def __init__(self, hbase_conf):
        self.bin_path = get_bin_path('start-hbase.sh')
        self.hbase_web_address = hbase_conf['hbase_web_address']

    def check(self):
        # HMaster PID
        pid = subprocess.Popen("source /etc/profile; jps | grep HMaster | awk '{print $1}'",
                               stdout=subprocess.PIPE, shell=True).stdout.read().strip()
        if pid == '':
            cmd = '{0}/start-hbase.sh'.format(self.bin_path)
            logger.warn("HMaster is not running, start HBase now")
            returncode = call([cmd])
            if returncode == 0:
                logger.info('started hbase successfully')
            else:
                call([cmd])
        else:
            url = '{0}/jmx?qry=Hadoop:service=HBase,name=Master,sub=Server'.format(self.hbase_web_address)
            res = urllib2.urlopen(url).read()

            live_region_servers = list(
                x.split(',')[0] for x in json.loads(res)['beans'][0]['tag.liveRegionServers'].split(';'))
            if len(live_region_servers) > 0 and live_region_servers[-1].strip() == '':
                live_region_servers = live_region_servers[:-1]
            dead_region_servers = list(
                x.split(',')[0] for x in json.loads(res)['beans'][0]['tag.deadRegionServers'].split(';'))
            if len(dead_region_servers) > 0 and dead_region_servers[-1].strip() == '':
                dead_region_servers = dead_region_servers[:-1]

            if dead_region_servers:
                logger.info('running HRegionServers: {0}'.format(live_region_servers))
                logger.warn('HRegionServers on {0} are not running, start them respectively'.
                            format(dead_region_servers))
                for host in dead_region_servers:
                    self.start_region_servers(host)

    def start_region_servers(self, host):
        """start region servers"""
        cmd = '{0}/hbase-daemon.sh start regionserver'.format(self.bin_path)
        call(['ssh', '-Tq', '-p', '22', host, cmd])
        logger.info('started regionserver on {0}'.format(host))


class RedisChecker:
    """redis监控工具"""

    def __init__(self, redis_conf):
        self.redis_host = redis_conf['redis_host']
        self.redis_home = redis_conf['redis_home']
        self.bin_path = redis_conf['bin_path']
        self.conf_path = redis_conf['conf_path']

    def check(self):
        cmd = "ssh -p 22 {0} 'ps -ef | grep redis | grep -v grep'".format(self.redis_host)
        out = subprocess.Popen(cmd, stdout=subprocess.PIPE, shell=True).stdout.read().strip()
        if "redis" not in out:
            logger.warn("Redis is not running, start redis now")
            self.start_redis(self.redis_host)

    def start_redis(self, host):
        start_cmd = "ssh -p 22 {0} 'cd {1}; {2}/redis-server {3} &'".\
            format(host, self.redis_home, self.bin_path, self.conf_path)
        call(start_cmd, shell=True)
        logger.info('started redis on {0}'.format(host))


class YarnAppChecker:
    """Yarn App 监控工具"""

    def __init__(self, yarn_app_conf, yarn_conf):
        self.streaming_apps = yarn_app_conf['streaming_apps']
        self.resourcemanager_webapp_address = yarn_conf['resourcemanager_webapp_address']

    def check(self):
        running_app_classes = self.get_running_apps().values()
        not_running_app_classes = []
        for app_class in self.streaming_apps.keys():
            is_run = False
            for running_app_class in running_app_classes:
                if app_class in running_app_class:
                    is_run = True
                    break
            if not is_run:
                not_running_app_classes.append(app_class)

        if not_running_app_classes:
            logger.warn('Yarn App: {0} are not running, start them respectively'.
                        format(not_running_app_classes))
            for not_running_app_class in not_running_app_classes:
                bin_name = self.streaming_apps[not_running_app_class]
                self.start_app(not_running_app_class, bin_name)

    @staticmethod
    def start_app(app_class, bin_name):
        # running background
        cmd = 'nohup {0} > {1} 2>&1'.format(bin_name, app_class)
        subprocess.Popen(cmd, shell=True)
        logger.info('started yarn app: {0}'.format(app_class))

    def get_running_apps(self):
        # 该方法调用命令行太慢。app_class_list: [app_class]
        # out = subprocess.Popen("source /etc/profile; yarn application -list",
        #                        stdout=subprocess.PIPE).stdout.read()
        # app_class_list = list(line.split()[1] for line in out.split('\n')[2:] if line != '')

        url = '{0}/cluster/apps/RUNNING'.format(self.resourcemanager_webapp_address)
        res = urllib2.urlopen(url).read()
        parser = YarnRunningAppHTMLParser(res)
        parser.feed(res)
        return parser.apps


class YarnRunningAppHTMLParser(HTMLParser):
    """parse running yarn application"""

    def __init__(self, html):
        HTMLParser.__init__(self)
        self.html = html

    # running yarn application list: [(appid, app_class_name)]
    apps = {}

    def handle_starttag(self, tag, attrs):
        pass

    def handle_endtag(self, tag):
        # python2.6.6会把双引号里面的内容也当做HTML进行解析。to workaround this, use regex to find match text
        if tag == 'html' and self.apps == {}:
            line_regex = r'^\[(.*)\],?$'  # running applications info
            lines = re.findall(line_regex, self.html, re.MULTILINE)
            for line in lines:
                splits = line.split(',')
                appid_regex = r'application[_\d]+'
                appid = re.search(appid_regex, splits[0]).group(0)
                app_class_name = splits[2][1:-1]
                self.apps[appid] = app_class_name

    def handle_data(self, data):
        if 'appsTableData=[' in data:
            line_regex = r'^\[(.*)\],?$'  # running applications info
            lines = re.findall(line_regex, data, re.MULTILINE)
            for line in lines:
                splits = line.split(',')
                appid_regex = r'application[_\d]+'
                appid = re.search(appid_regex, splits[0]).group(0)
                app_class_name = splits[2][1:-1]
                self.apps[appid] = app_class_name


class Checker:
    def __init__(self, _conf):
        yarn_checker = YarnChecker(_conf['yarn'])
        hdfs_checker = HDFSChecker(_conf['hdfs'])
        zk_checker = ZookeeperChecker(_conf['zookeeper'])
        kafka_checker = KafkaChecker(_conf['kafka'])
        hbase_checker = HBaseChecker(_conf['hbase'])
        redis_checker = RedisChecker(_conf['redis'])
        spark_streaming_checker = YarnAppChecker(_conf['yarn_app'], _conf['yarn'])
        self.checkers = [
            yarn_checker,
            hdfs_checker,
            zk_checker,
            kafka_checker,
            hbase_checker,
            redis_checker,
            spark_streaming_checker
        ]

    def check(self):
        for checker in self.checkers:
            checker.check()


def get_conf():
    """获取配置"""
    # sbin_path = get_bin_path('start-dfs.sh')
    # # default 50070
    # namenode_http_address = subprocess.Popen(
    #     '{0}/bin/hdfs getconf -confKey dfs.namenode.http-address'.format(sbin_path[:-6]),
    #     stdout=subprocess.PIPE, shell=True).stdout.read()

    # p1 = subprocess.Popen(['ss', '-l', '-p', '-n'], stdout=subprocess.PIPE)  # all sockets
    # p2 = subprocess.Popen(['grep', pid], stdin=p1.stdout, stdout=subprocess.PIPE,
    #                       stderr=subprocess.PIPE)
    # out, err = p2.communicate()
    # # hbase web port. default 16010
    # hbase_web_port = list(x for x in out.split('\n') if 'ffff' not in x)[0].split()[3][3:]
    _conf = {
        'yarn': {
            'resourcemanager_webapp_address': 'http://master:8088'
        },
        'hdfs': {
            'namenode_http_address': 'http://master:50070',
        },
        'zookeeper': {
            'zk_servers': 'slave1:2181,slave2:2181,slave3:2181'
        },

        'kafka': {
            'kafka_brokers': 'slave1:9092,slave2:9092,slave3:9092'
        },
        'hbase': {
            'hbase_web_address': 'http://master:16010'
        },
        'redis': {
            'redis_host': 'redis',
            'redis_home': os.environ['REDIS_HOME'],
            'bin_path': os.environ['REDIS_HOME'] + '/src',
            'conf_path': os.environ['REDIS_HOME'] + '/redis.conf'
        },
        'yarn_app': {
            'streaming_apps': {
                'com.timeyang.spark.streaming.WordCountApp': os.getcwd()+'/wordCountSpark.sh',
                'com.timeyang.flink.streaming.WordCountApp': os.getcwd()+'/wordCountFlink.sh'
            }
        }

    }
    return _conf


if __name__ == '__main__':
    checkers = Checker(get_conf())
    checkers.check()
