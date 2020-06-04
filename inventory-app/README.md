# inventory-app project

This project uses [Quarkus](https://quarkus.io/), Hibernate with Panache and DB2 JDBC driver and support the CRUD operations for the following integrated entity:

* Inventory
* Stores
* Items

The code is a strong reuse of [the SIMPLIFIED HIBERNATE ORM WITH PANACHE guide](https://quarkus.io/guides/hibernate-orm-panache).

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell
./mvnw quarkus:dev
```

## Access to the API

[http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)


## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `inventory-app-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/inventory-app-1.0.0-SNAPSHOT-runner.jar`.

## Build and run with docker

Under the root folder of this project do:

```shell
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t ibmcase/eda-inventory-app:1.0.0 .
docker run -i --rm -p 8080:8080 ibmcase/eda-inventory-app:1.0.0
```

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/inventory-app-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.