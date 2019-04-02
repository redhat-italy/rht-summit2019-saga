Saga microservices Playground
=============================

## Saga Choreography with Quarkus

### Launch on local env - linux and mac

Docker Containers:
 - Postgres (image debezium/postgres) on local port 5432
 - Zookeeper (image debezium/zookeeper) on local port 2181
 - Kafka (image debezium/kafka) on local port 9092
 - Kafka Connect + Debezium (image hifly81/debezium-connect) on local port 8083
 - Ticket Service (image hifly81/quarkus-ticket-service) on local port 8080

For Linux users:

```bash
cd quarkus/
./deploy-local-linux.sh
```

For Mac users:

```bash
cd quarkus/
./deploy-local-mac.sh
```

1 ticket will be created (payload in file ticket/tickets.json)


### Create Native Image

Launch the script to create the native images:

```bash
cd quarkus/
./build-image.sh
```

### Deploy on OpenShift

Launch the bootstrap script to create your namespace.<br>
Images are downloaded from docker hub.

```bash
cd quarkus/
./deploy-ocp.sh
```
