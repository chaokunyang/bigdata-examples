# coding=utf-8
# 由于操作系统默认都是python2，因此以下脚本使用python2编写
"""
重新运行指定时间范围内的指定日期内的任务。
示例：
    python rerun.py -start 2017/11/21 -end 2017/12/01 -task dayJob.sh
    python rerun.py -start 2017/11/21 -end 2017/12/01 -r -task dayJob.sh
"""
from subprocess import call
from datetime import datetime, timedelta
import os
import logging

logger = logging.getLogger("rerun tool")
logger.setLevel(logging.INFO)
logger_handler = logging.FileHandler('rerun.log')
formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")
logger_handler.setFormatter(formatter)
logger.addHandler(logger_handler)


def get_opts(argv):
    """Collect command-line options in a dictionary

    :rtype: dict
    """
    opts = {}
    argv = argv[1:]  # exclude rerun.py argument
    while argv:  # 当这里有参数需要解析
        if argv[0][0] == '-' and argv[1] and argv[1][0] != '-':  # 找到"-name value" pair
            opts[argv[0][1:]] = argv[1]
            argv = argv[2:]  # 前进两步
        elif argv[0][0] == '-' and argv[1] and argv[1][0] == '-':
            opts[argv[0][1:]] = True
            argv = argv[1:]  # 前进一步
        else:
            argv = argv[1:]  # 跳过该参数

    logger.info('input args : %s', opts)
    return opts


def date_range(start_date, end_date, reverse=False):
    """日期范围"""
    index_range = range(int((end_date - start_date).days))
    if reverse:
        index_range = range(int((end_date - start_date).days), 0, -1)
    for n in index_range:
        yield start_date + timedelta(n)


def run_task(task, day):
    """运行脚本任务"""
    work_dir = os.getcwd()
    logger.info('start run task: %s for %s', work_dir + "/" + task, day)
    call([work_dir + "/" + task, day])


def rerun(opts):
    """离线任务重跑"""
    start_day = datetime.strptime(opts['start'], '%Y/%m/%d')
    end_day = datetime.strptime(opts['end'], '%Y/%m/%d')
    task = opts['task']
    reverse = False
    if 'r' in opts:
        reverse = True
    for day in date_range(start_day, end_day, reverse):
        run_task(task, day.strftime('%Y/%m/%d'))


if __name__ == "__main__":
    from sys import argv

    args = get_opts(argv)
    rerun(args)
