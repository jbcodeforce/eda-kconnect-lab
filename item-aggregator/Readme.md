# Item aggregator component

This project is an example to illustrate combining kafka streams with reactive programming, reactive messaging with Quarkus.

* create a quarkus app using microprofile reactive messaging to consume items sold in stores
* aggregate store id -> item id -> item sold count
* aggregate item id -> total sold so far
* generate events on inventory topic using storeID -> [items] in stock

The kafka elements used:

* in-topic: items
* out-topic: inventory
* ktable <itemID, count> with store. To keep total stock, cross stores per item
* ktable <storeID, <itemID, count> with store. To keep store inventory
* Interactive query to get data from store and exposed as reactive REST resource.

## How asset was created

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

As items topic can be partitioned, a REST call may not reach the good end points, as the local store may not have the expected queried key. So the code is using interactive query to get access to the local state stores or return a URL of a remote store where the records for the given key are.

## How to run it

We assume an existing deployed Event Streams on OpenShift. If not you can deploy a Kafka cluster using [Strimzi](http://strimzi.io).

Be sure to have `items` topic with 3 partitions created and `inventory` topic with one partition.

To be able to remote connect to event streams we need a set of parameters that will be exposed via environment variables as defined in a `.venv` file. Here are a summary of the steps:

* Connect CLI to Event Streams:

```shell
oc login
cloudctl es init

Select an instance:
1. minimal-prod ( Namespace:eventstreams )
2. sandbox-rp ( Namespace:eventstreams )
Enter a number> 1
```

Get the `Event Streams bootstrap external address` and update the KAFKA_BROKERS variable in `.env` file.

* Select one of the kafka users ( with scram-sha-512 authentication) defined in the project where event streams is deployed.

```shell
oc get kafkausers -n eventstreams
```

* Define the KAFKA_USER variable in `.env` file with one of the user and KAFKA_PASSWORD with the user's password extracted from his secret:

```
oc get secret <username> --namespace eventstreams -o jsonpath='{.data.password}' | base64 --decode
```

* Then get the TLS certificate with the command:

```shell
cloudctl es certificates --format p12
# get the truststore password and the .p12 file
# mv the certificate
mv es-cert.p12 certs
```

The cluster public certificate is required for all external connections and is available to download from the Cluster connection panel under the Certificates heading. Upon downloading the PKCS12 certificate, the certificate password will also be displayed.

Modify KAFKA_CERT_PWD in the `.env` file.

Start the app in dev mode after doing the `source .env` command to set environment variables.

```shell
./mvnw quarkus:dev
```

The application should be connected to kafka and get visibility to the items and inventory topics. Next step is to send some items.

If we want to run the application using the *-runner.jar, the code will run in %prod profile, this means the configuration will use TLS certificate to do the authentication, as defined in the `application.properties`.

```properties
%prod.kafka.ssl.keystore.location=${USER_CERT_PATH}
%prod.kafka.ssl.keystore.password=${USER_CERT_PWD}
```

In this configuration we need to pick a user with TLS authentication and download the java key store for that user. Here are the commands:

```shell
# get user with tls
 oc get kafkausers -n eventstreams
# We use a user with TLS authentication named: kconnect-secret-internal
# Get the keystore for his public certificate and key
oc get secret kconnect-secret-internal -n eventstreams -o jsonpath='{.data.user\.p12}' | base64 --decode > certs/user.p12
# get password
oc get secret kconnect-secret-internal -n eventstreams -o jsonpath='{.data.user\.password}' | base64 --decode
# set env variables
export KAFKA_BROKERS=minimal-prod-kafka-bootstrap-eventstreams.....:443
export USER_CERT_PATH=${PWD}/certs/user.p12
export USER_CERT_PWD=super-secret-password-decoded-in-base64
# Finally start the app
java -jar ./target/item-aggregator-1.0-SNAPSHOT-runner.jar
```

# End to end testing

The integration tests use Python scripts. We have a custom python docker images (ibmcase/python37) with the necessary Kafka and pandas libraries to execute the tests.

* under e2e folder get the Event Streams certificate in pem format:

```shell
cloudctl es certificates --format pem
```

* Start the python environment to send 2 items. Under `e2e` folder, execute following command to start the python environment connected to the docker network where kafka is running:

```shell
# if not done set the env variables
source .env
docker run -v $(pwd)/e2e:/home -e KAFKA_BROKERS=$KAFKA_BROKERS \
   -e KAFKA_USER=$KAFKA_USER -e KAFKA_PASSWORD=$KAFKA_PASSWORD \
   -e KAFKA_CERT_PATH=/home/es-cert.pem \
      -ti ibmcase/python37 bash
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

* After these two records published we can validate the Event Streams console:
  * The consumer groups has 3 active members as there are three kafka stream tasks running.
  * One of the task has processed the partition 1 where e2 records were sent.  
  * The inventory topic has 2 records published.
* Using the REST api we can see the current stock for the store `Store-1` and the item

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

* Select one of the kafka users with TLS authentication defined or create a new one with the produce, consume messages and create topic and schemas authorizations, on all topics or topic with a specific prefix, on all consumer groups or again with a specific prefix, all transaction IDs.

 ```shell
 # if not logged yes to your openshift cluster where the docker private registry resides do:
oc login --token=... --server=https://c...
 oc get kafkausers -n eventstreams
 ```

We use a user with TLS authentication named: ` kconnect-secret-internal`

* Copy user's secret to the current project where the application will run

```shell
oc get secret  kconnect-secret-internal -n eventstreams --export -o yaml | oc apply -f -
```

* Define config map for Kafka broker URL and user name

```
oc apply -f src/main/kubernetes/configmap.yaml
```

* Build and push the image to public registry

```shell
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t ibmcase/item-aggregator:0.0.2 .
docker push ibmcase/item-aggregator:0.0.2
# build with s2i and push the image to private registry
./mvnw clean package -Dquarkus.kubernetes.deploy=true
```
