#!/bin/bash

if [[ $# -ne 1 ]]
then
    echo "Usage: createOrStartKconnect [build | start | both ]"
    exit 1
fi
source ../scripts/setenv.sh

function build {
    echo build 
    sed 's/APIKEY/'$KAFKA_APIKEY'/g' connect-distributed-TMPL.properties > output.properties
    sed 's/KAFKA_BROKERS/'$KAFKA_BROKERS'/g' output.properties > connect-distributed.properties
    docker build -t ibmcase/kconlab:1.0.0  .
}

function start {
    docker run -t -p 8083:8083 ibmcase/kconlab:1.0.0 
}



if [[ "$1" == "build" ]]
then
    build
fi


if [[ "$1" == "both" ]]
then
    build
    start
fi

if [[ "$1" == "start" ]]
then
    start
fi

