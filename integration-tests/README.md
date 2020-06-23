# Integration tests

This folder has multiple integration tests to validate the solution component per component.

## Setup

1. Be sure to have an Event Streams intance up and running and the credentials with TLS certificate when running on-premise with Cloud Pak for integration.
1. If not done before create an `inventory` topic with one partition using the ibmcloud CLI or User interface.
1. Set the `scripts/setenv.sh` with the KAFKA_BROKERS and KAFKA_APIKEY values as defined in the Event Streams service credentials.
1. Starts the Kafka Connector see readme in `kconnect` folder.

## Monitor kafka messages with Kafdrop

If you want to review [Kafdrop tool](https://github.com/obsidiandynamics/kafdrop) in [this note](https://ibm-cloud-architecture.github.io/refarch-eda/technology/event-streams/schema-registry/#kafdrop).

We have a script in the `infrastructure` folder to start a Kafdrop docker instance locally but connected to a remote event streams.

```shell
./startKafdrop.sh
```

To stop it

```shell
./stopKafdrop.sh
```

## Produce inventory events to inventory topic

The goal is to validate event streams connection and sending event to be processed by down stream logic, like JDBC Sink to database.

### Prepare and run

1. Under the integration-tests folder start a python environment: `./startPython.sh`
1. In the bash shell, inside the python container, execute the following command to send two events based on a payload that looks like: 

```json
{"storeName": "LA02", "itemCode": "IT08", "quantity": 3, "price": 20, "id": 1, "timestamp": 1591142867.188271}
```

```shell
 python ProduceInventoryEvent.py --size 2
```

The Item code and store name are selected randomly from a possible set of values, that work with the data in the database. (See inventory-app)

To get out of the container use `exit` or Ctrl-C.

In the Kafdrop you should see the messages in the inventory topic.

![]()