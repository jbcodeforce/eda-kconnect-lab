# Store sale producer service

This component is a simulator app to create events to kafka about stores activities around selling items.

It uses [Quarkus](https://quarkus.io) with the AMQP qpid-jms extension to send messages to RabbitMQ. This code is used to demonstrate RabbitMQ to Kafka with the IBM Kafka Connect [RabbitMQ source connector](https://github.com/ibm-messaging/kafka-connect-rabbitmq-source).

Tested 08/28/2020 Quarkus 1.7 and Kafka 2.5

## Implementation approach

The application is using one REST resource for defining two simple API: 

* `GET /sales` to get the last item sold.
* `POST /sales/start/{records}` to start sending {records} number of message to MQ. 

The messages sent are defined in the [domain/ItemSaleMessage.java](https://github.com/jbcodeforce/eda-kconnect-lab/blob/master/store-sale-producer/src/main/java/ibm/gse/eda/stores/domain/ItemSaleMessage.java) class.

The items sold are part of a simple predefined list of item with SKU from 'IT01 to IT09'. The content is generated randomly. 

Below is an example as json object:

```json
{"id":9,"price":3.9131141142105355,"quantity":5,"sku":"IT05","storeName":"PT01"}
```

The [simple generator code](https://github.com/jbcodeforce/eda-kconnect-lab/blob/18c4fed416d92bb3cadce733e6d5352afafd1243/store-sale-producer/src/main/java/ibm/gse/eda/stores/infrastructure/ItemSaleGenerator.java#L76) is sending message to RabbitMQ using the AMQP API.

The following extensions were added to add metrics, health end points, and OpenShift deployment manifests creation:

```shell
./mvnw quarkus:add-extension -Dextensions="quarkus-smallrye-openapi"
./mvnw quarkus:add-extension -Dextensions="smallrye-health"
./mvnw quarkus:add-extension -Dextensions="smallrye-metrics"
./mvnw quarkus:add-extension -Dextensions="openshift"
```

## Build and run locally

### Running the application in dev mode

You could use the quarkus in dev mode with `./mvnw quarkus:dev`, but we have also done a simple docker compose with maven to continuously run quarks:dev with dependant rabbitmq container.

```shell
docker-compose up
```

Access the API via: [http://localhost:8080/swagger-ui/#/](http://localhost:8080/swagger-ui/#/)

Access Rabbit MQ Console: [http://localhost:15672/#/](http://localhost:15672/#/) user rabbitmq

In the Queues page, see the content of the `items` queue, and use the 'get messages' to see the queue content.

The application is working, next is to package and deploy it to OpenShift.

### Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `store-sale-producer-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/store-sale-producer-1.0.0-SNAPSHOT-runner.jar`.

### Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/store-sale-producer-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.

## Deploy and run on OpenShift

We have defined a ConfigMap to deploy to the OpenShift project so environment variables can be loaded from the config map at runtime.

```
oc apply -f src/main/kubernetes/store-sale-cm.yaml
```

To package the app as docker images with a build on OpenShift, using the source to image approach run the following command:

```shell
./mvnw clean package -Dquarkus.container-image.build=true -Dquarkus.container-image.group=ibmcase -Dquarkus.container-image.tag=1.0.0
```
