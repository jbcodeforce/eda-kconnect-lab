source ../scripts/setenv.sh
sed 's/APIKEY/'$KAFKA_APIKEY'/g' kafka.properties > output.properties
docker run -d --rm -p 9000:9000 \
    -e KAFKA_BROKERCONNECT=$KAFKA_BROKERS \
    -e KAFKA_PROPERTIES=$(cat output.properties | base64) \
    -e JVM_OPTS="-Xms32M -Xmx64M" \
    -e SERVER_SERVLET_CONTEXTPATH="/" \
    obsidiandynamics/kafdrop