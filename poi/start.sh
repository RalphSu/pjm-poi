#!/bin/bash

source ~/.bashrc
export PJM_HOME=/home/ralph/dev/pjm2;
nohup java -Xmx512m -jar ~/report.jar >> ~/report_generation.log &

