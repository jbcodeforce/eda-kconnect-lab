# Store sale producer service

This component is a mockup of stores activities around selling items of the inventory. 

It uses [Quarkus](https://quarkus.io) with the AMQP qpid-jms extension to send messages to RabbitMQ. This code is used to demonstrate RabbitMQ to Kafka with Kafka Connect [IBM RabbitMQ source connector](https://github.com/ibm-messaging/kafka-connect-rabbitmq-source). The application is also a microservice to expose a get messages using paging.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell
./mvnw quarkus:dev
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `store-sale-producer-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/store-sale-producer-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/store-sale-producer-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.