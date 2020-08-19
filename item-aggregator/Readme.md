# Item aggregator component

The goals of this project are:

* create a quarkus app using reactive messaging to consume items sold in store events
* aggregate store id -> item id -> item sold count
* aggregate item id -> total sold so far
* generate events on inventory topic used item id -> total sold

The kafka elements used:

* in-topic items
* out-topic inventory
* ktable item, count
* ktable store-item, count

## How asset is created

We are using our [custom appsody quarkus, kafka stack](https://github.com/ibm-cloud-architecture/appsody-stacks).
Then the code is using kafka streams API so the following extension was added:

```shell
./mvnw quarkus:add-extension -Dextensions="smallrye-health,quarkus-smallrye-openapi"
./mvnw quarkus:add-extension -Dextensions="jsonb"
./mvnw quarkus:add-extension -Dextensions="kafka,"
```