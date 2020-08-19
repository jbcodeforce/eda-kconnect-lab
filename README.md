# Kafka Connect with Event Streams on Cloud

This repository includes a set of components to demonstrate some of the Kafka Connect connectors developed by the IBM Event messaging extended team.

We are using a classical, yet simple implementation, of good items sold in store and how to use kafka stream to get a centralized view of the goods inventory. The business goal is to get a real time inventory view instead of relaying on batch jobs run daily. A lot of distribution industry companies are moving to this approach.

The system context looks like:

![](./design/out/system-ctx/system-ctx.png)

And the component view looks like:

![](./design/out/solution-view/solution-view.png)

See the [lab instructions](https://ibm-cloud-architecture.github.io/refarch-eda/technology/event-streams/kconnect/) in the EDA main repository for a step by step tutorial covering the different connectors used in the solution.

## Pre-requisites

The ultimate goal is to run all those components on an OpenShift cluster and use Cloud Pak for Integration with Event Streams and MQ instances. So you need:

* an OpenShift cluster with at least version 4.4
* Cloud Pak for Integration 2020.2
* Event Streams v10 instance 


## Build and run locally

We are providing docker compose files and maven poms to build and run each component locally for development purpose.

### Build

Do the following within this folder, to build each of the different modules

```shell
mvn build
```

### Build and configure connector

Under the `kconnect` folder we have a simple script to get the 3 connector to use, compile and package them, then copy jars for the connectors to the `kconnect/libs` folder. Once done the 3 configurations for each connector assumes to connect to a remote Event Streams cluster on cloud, and use RabbitMQ, Db2 and MQ locally.

The file `connect-distributed-TMPL.properties` is a template for the connection definition. Change the APIKEY and BROKERS end points, then rename the file as `connect-distributed.properties`.

Finally as we are using custom adaptor, we need to build a Kafka connector image:

```shell
docker build -t ibmcase/kconlab:1.0.0 .
```

### Run locally

Under the `infrastructure` folder we have different docker compose files to support different test scenarios:

* `MQ-Kconnect-compose.yaml` for IBM MQ and the kafka connect.
* `RabbitMQ-Kconnect-compose.yaml` for Rabbit MQ with Kafka Connect and the Store item sales generator app.

### Rabbit MQ

Quick set of things to do:

```shell
# Start Rabbit mq and kafka connect and the store item sale generator: under infrastructure

docker-compose -f  RabbitMQ-Kconnect-compose.yaml up
# Upload the connector configuration
curl -X POST -H "Content-Type: application/json" http://localhost:8083/connectors   --data "@./rabbitmq-source.json"
```

# Once done delete the connector

```shell
curl -X DELETE http://localhost:8083/connectors/RabbitMQSourceConnector
```

## IBM MQ Sink connector

Deploy the connector with a POST to the connectors url

```shell
curl -X POST -H "Content-Type: application/json" http://localhost:8083/connectors   --data "@./mq-sink.json"
# To delete the connector
curl -X DELETE  http://localhost:8083/connectors/mq-sink
```

```
docker exec -ti ibmmq bash
bash-4.4$ /opt/mqm/samp/bin/amqsget INVENTORY QM1
Sample AMQSGET0 start
message <{"storeName": "NYC02", "itemCode": "IT02", "quantity": 11, "price": 113, "id": 0, "timestamp": "15-Jun-2020 23:56:30"}>
no more messages
```

### Some trouble shooting

* Be sure to have set the consumer and producer credential in the kafka connector, if not you will get a broker disconnection.
* Be sure to specify the good hostname for the MQ server: `ibmmq` when running locally
* Entity 'admin' has insufficient authority to access object QM1 [qmgr].
* User ID 'app' authentication failed: add authentication info on DEV.AUTHINFO for the app user:

![](../docs/images/mq-authentication-info.png)

![](../docs/images/mq-auth-app-user.png)
