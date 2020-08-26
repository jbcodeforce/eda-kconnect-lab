# Item aggregator component

The goals of this project are:

* create a quarkus app using reactive messaging to consume items sold in store events
* aggregate store id -> item id -> item sold count
* aggregate item id -> total sold so far
* generate events on inventory topic used item id -> total sold

The kafka elements used:

* in-topic items
* out-topic inventory
* ktable item, count  with store
* ktable store-item, count with store
* Interactive query to get data from partitioned topic

## How asset is created

We are using our [custom appsody quarkus, kafka stack](https://github.com/ibm-cloud-architecture/appsody-stacks).
Then the code is using kafka streams API so the following extension was added:

```shell
./mvnw quarkus:add-extension -Dextensions="smallrye-health,quarkus-smallrye-openapi"
./mvnw quarkus:add-extension -Dextensions="jsonb"
./mvnw quarkus:add-extension -Dextensions="kafka,smallrye-reactive-messaging-kafka,kafka-streams"
```

Then we use the TopologyTestDriver to test the topology. We need one stream processing to process the items and compute the remaining item stock, and two stores to keep total item count and store - item stock.

Some explanations on the topology can be found in the class [StoreInventoryAgent](). It builds a store, inventory ktable by processing the item sold, and get new key from item.storeName. The inventory is a hashmap <itemID, currentStockAmount>. So the logic to compute the current stock amount is done with aggregate: 

```java
.aggregate(
      () ->  new Inventory(), // initializer
      (k , newItem, currentInventory) 
            -> currentInventory.updateStockQuantity(k,newItem), 
      Materialized.<String,Inventory,KeyValueStore<Bytes,byte[]>>as(StoreInventoryAgent.STOCKS_STORE_NAME)
            .withKeySerde(Serdes.String())
            .withValueSerde(inventorySerde));
```
First row is to initialize new key, record with an empty Inventory object. 
The second row is executed when a key is found (first key too), and update the currentInventory with the new quantity from the item. 
The content is materialized in a store. This store can be query to answer to an API like `/inventory/store/{storeid}/{itemid}`.

As items topic can be partioned, a REST call may not reach the good end points, as the local store may not have the expected queried key. So the code is using interactive query to get access to the local state stores or return a URL of a remote store where the records for the given key are.

## How to run it

Start Kafka locally with `docker-compose up`, then start a builder docker container connected to the same local docker network and running `quarkus:dev`

```shell
docker build -f Dockerfile-dev -t tmp-builder .
docker run --rm -p 8080:8080 -ti --network kafkanet -v ~/.m2:/root/.m2 tmp-builder
```

# End to end testing

The integration tests use Python scripts. We have a custom python docker images (ibmcase/python37) with the necessary Kafka and pandas libraries to execute the tests.

* Start the Kafka cluster locally

`docker-compose -f docker-compose-dev.yaml up`

* If not done before configure the topics for test: under the `e2e` folder do `./createTopics.sh`

* Start the app in development mode: `./mvnw quarkus:dev`

* Start the python environment to send 2 items. Under `e2e` folder, execute following command to start the python environment connected to the docker network where kafka is running:

```shell
docker run -v $(pwd):/home -e KAFKA_BROKERS=kafka:9092 \
     --network kafkanet \
      -ti ibmcase/python37 bash -c "python /home/ItemProducer.py"
```

In the shell run the item producer: `python ItemProducer.py`

```shell
root@docker-desktop:/home# python ItemProducer.py
Start Item Sold Event Producer
INFO:root:--- This is the configuration for the producer: ---
INFO:root:[KafkaProducer] - {'bootstrap.servers': 'kafka:9092', 'group.id': 'ItemSoldProducer-1', 'delivery.timeout.ms': 15000, 'request.timeout.ms': 15000}
INFO:root:---------------------------------------------------
INFO:root:Send {"storeName": "Store-1", "itemCode": "Item-2", "type": "RESTOCK", "quantity": 5} with key itemCode to items
INFO.. - Message delivered to items [0]
INFO:root:Send {"storeName": "Store-1", "itemCode": "Item-2", "type": "SALE", "quantity": 2, "price": 10.0} with key itemCode to items
INFO.. - Message delivered to items [0]
```

* In a terminal, under `e2e` start the consumer of the inventory topic: ` ./monitorInventory.sh`. The trace should get those records:

```json
Store-1	{"stock":{"Item-2":-2},"storeName":"Store-1"}
Store-1	{"stock":{"Item-2":1},"storeName":"Store-1"}
Store-1	{"stock":{"Item-2":4},"storeName":"Store-1"}
Store-1	{"stock":{"Item-2":7},"storeName":"Store-1"}
```

* The last statement is accessible from the Kafka Stream KTable, via the REST api: 

```shell
curl http://localhost:8002/inventory/store/Store-1/Item-2

# should get a result like:
{
  "stock": {
    "Item-2": 7
  },
  "storeName": "Store-1"
}
```

The API is visible via the swagger-ui: `http://localhost:8080/swagger-ui/`

## Deploy on OpenShift cluster with Event Streams

