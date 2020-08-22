KAFKA_INTERNAL_PATH="/opt/kafka"
KAFKA_DOCKER_NAME=$(docker ps | grep kafka_1 | awk '{print $NF}')

if [ -z "$KAFKA_DOCKER_NAME" ]
then
    echo "\e[31m [ERROR] - Kafka docker is not running.\e[0m"
    exit 1
fi

docker exec -ti $KAFKA_DOCKER_NAME bash -c "$KAFKA_INTERNAL_PATH/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
   --topic inventory --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true "
