#!/bin/bash

echo $PJM_HOME
# stop
# kill the project that has report.jar
pid=`ps aux | grep report | grep -v grep | awk '{print $2}'`
kill $pid
