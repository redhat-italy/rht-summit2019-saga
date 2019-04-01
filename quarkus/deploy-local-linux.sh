#!/bin/bash

DOCKER_HOST=$(ip -4 addr show docker0 | grep -Po 'inet \K[\d.]+')
echo "Docker host: ${DOCKER_HOST}"

############################ Docker prune
echo "Deleting Docker containers running for Debezium...."
docker stop $(docker ps -a | grep debezium/connect | cut -d ' ' -f 1)
docker stop $(docker ps -a | grep debezium/kafka | cut -d ' ' -f 1)
docker stop $(docker ps -a | grep debezium/zookeeper | cut -d ' ' -f 1)
docker stop $(docker ps -a | grep debezium/postgres | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep debezium/connect | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep debezium/kafka | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep debezium/zookeeper | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep debezium/postgres | cut -d ' ' -f 1)


echo "Pruning done. Starting application..."

############################ Postgres

echo "Start Postgresql container...."
docker run -d --name postgres -p 5000:5432 debezium/postgres
sleep 5
echo "CREATE tickets database...."
psql -h localhost -p 5000 -U postgres -c 'CREATE DATABASE tickets;'
echo "Postgresql started."

############################ Zookeeper

echo "Start Zookeeper container...."
docker run -d --name zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 debezium/zookeeper
echo "Zookeeper started."

############################ Kafka

echo "Start Kafka container...."
docker run -d --name kafka -p 9092:9092 --link zookeeper:zookeeper debezium/kafka
sleep 5
echo "CREATE kafka topic schema-changes.tickets...."
docker run -it --rm --link zookeeper:zookeeper debezium/kafka create-topic -r 1 schema-changes.tickets
echo "Kafka started."

############################ Debezium - Kafka Connect

echo "Start Debezium Kafka connect container...."

docker run -d --name connect -p 8083:8083 -e GROUP_ID=1 -e CONFIG_STORAGE_TOPIC=my-connect-configs -e OFFSET_STORAGE_TOPIC=my-connect-offsets -e ADVERTISED_HOST_NAME=${DOCKER_HOST} --link zookeeper:zookeeper --link postgres:postgres --link kafka:kafka debezium/connect
sleep 5
echo "CREATE kafka connector ticket-connector...."
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d @debezium/connect-pgsql-kafka.json
#sleep 5
#curl -X GET -H "Accept:application/json" localhost:8083/connectors/ticket-connector
echo "Kafka Connect started."

############################ Debezium - Kafka Connect with transformation


########################### Ticket Application
sleep 5
echo "Running Ticket Application..."
cd ticket && ./mvnw compile quarkus:dev

#docker exec -it kafka /bin/bash -c "cat data/1/dbserver1.public.ticketevent-0/00000000000000000000.log"

