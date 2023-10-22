#!/bin bash

cd ..
dir=`pwd`

nohup java -jar ${dir}/sweb-server-1.0.jar > /dev/null &

