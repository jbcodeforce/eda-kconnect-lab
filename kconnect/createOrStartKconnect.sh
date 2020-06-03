#!/bin/bash
source ../scripts/setenv.sh

alreadyBuild=$(docker images | grep kconlab)
if [[ "$alreadyBuild" != "" ]]
then
    echo start it
else
    echo build and start
    sed 's/APIKEY/'$KAFKA_APIKEY'/g' connect-distributed-TMPL.properties > output.properties
    sed 's/KAFKA_BROKERS/'$KAFKA_BROKERS'/g' output.properties > connect-distributed.properties
    docker build -t ibmcase/kconlab:1.0.0  .
fi
docker run -t -p 8083:8083 ibmcase/kconlab:1.0.0 
