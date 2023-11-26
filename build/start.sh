#!/bin/bash

current_dir=$(cd $(dirname $0);pwd)

java -DrootDir=${current_dir} -Dlog4j.configuration=file:${current_dir}/log4j.properties -jar myproxy-1.0.0-RELEASE-jar-with-dependencies.jar
