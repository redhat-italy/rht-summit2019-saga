Saga microservices Playground
=============================

## Saga Choreography with Quarkus

### Launch on local env - linux and mac

Launch the bootstrap script to create the docker containers.<br>
Images are downloaded from docker hub.

Containers:
 - Postgres (image debezium/postgres) on local port 5432
 - Zookeeper (image debezium/zookeeper) on local port 2181
 - Kafka (image debezium/kafka) on local port 9092
 - Kafka Connect + Debezium (image hifly81/debezium-connect) on local port 8083
 - Ticket Service (image hifly81/quarkus-ticket-service) on local port 8080
 - Insurance Service (image hifly81/quarkus-insurance-service) on local port 8090
 - Payment Service (image hifly81/quarkus-payment-service) on local port 8100

```bash
cd quarkus/
./deploy-docker.sh
./test-saga.sh
```

1 ticket will be created (payload in file ticket/tickets.json).

1 insurance will be created (payload in file insurance/insurances.json).


### Create Native Image

Launch the script to create the native images:

```bash
cd quarkus/
./build-image.sh
```

### Deploy on OpenShift

Launch the bootstrap script to create your namespace.<br>
Images are downloaded from docker hub.

Containers:
 - Postgres (image debezium/postgres)
 - Zookeeper (image debezium/zookeeper)
 - Kafka (image debezium/kafka)
 - Kafka Connect + Debezium (image hifly81/debezium-connect)
 - Ticket Service (image hifly81/quarkus-ticket-service)
 - Insurance Service (image hifly81/quarkus-insurance-service)
 - Payment Service (image hifly81/quarkus-payment-service)

```bash
cd quarkus/
./deploy-ocp.sh
```
