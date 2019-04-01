#!/bin/bash

DOCKER_HOST=$(ip -4 addr show docker0 | grep -Po 'inet \K[\d.]+')
printf "\nDocker host: ${DOCKER_HOST}"

############################ Docker prune
echo -e "\nDeleting Docker containers running for Debezium...."

docker stop $(docker ps -a | grep debezium/connect | cut -d ' ' -f 1)
docker stop $(docker ps -a | grep debezium/kafka | cut -d ' ' -f 1)
docker stop $(docker ps -a | grep debezium/zookeeper | cut -d ' ' -f 1)
docker stop $(docker ps -a | grep debezium/postgres | cut -d ' ' -f 1)
docker stop $(docker ps -a | grep hifly81/quarkus-ticket-service | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep debezium/connect | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep debezium/kafka | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep debezium/zookeeper | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep debezium/postgres | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep hifly81/quarkus-ticket-service | cut -d ' ' -f 1)


echo -e "\nPruning done. Starting application..."

############################ Postgres

echo -e "\nStart Postgresql container...."
docker run -d --name postgres -p 5432:5432 debezium/postgres
sleep 5
echo -e "\nCREATE tickets database...."
psql -h localhost -p 5432 -U postgres -c 'CREATE DATABASE tickets;'
echo -e "\nPostgresql started."

############################ Zookeeper

echo -e "\nStart Zookeeper container...."
docker run -d --name zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 debezium/zookeeper
echo -e "\nZookeeper started."

############################ Kafka

echo -e "\nStart Kafka container...."
docker run -d --name kafka -p 9092:9092 --link zookeeper:zookeeper debezium/kafka
sleep 5
echo -e "\nCREATE kafka topic schema-changes.tickets...."
docker run -it --rm --link zookeeper:zookeeper debezium/kafka create-topic -r 1 schema-changes.tickets
echo -e "\nKafka started."

############################ Debezium - Kafka Connect

echo -e "\nStart Debezium Kafka connect container...."
docker run -d --name connect -p 8083:8083 -e GROUP_ID=1 -e CONFIG_STORAGE_TOPIC=my-connect-configs -e OFFSET_STORAGE_TOPIC=my-connect-offsets -e ADVERTISED_HOST_NAME=${DOCKER_HOST} --link zookeeper:zookeeper --link postgres:postgres --link kafka:kafka debezium/connect
sleep 5
echo -e "\nCREATE kafka connector ticket-connector...."
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d @debezium/connect-pgsql-kafka.json
echo -e "\nKafka Connect started."

############################ Debezium - Kafka Connect with transformation


########################### Ticket Application
echo -e "\nStart Ticket Application container..."
docker run -d --name ticket -p 8080:8080 --link postgres:postgres --link zookeeper:zookeeper --link kafka:kafka hifly81/quarkus-ticket-service
echo -e "\nTicket Application started."
sleep 5
echo -e "\nAdd 1 ticket..."
cat ticket/tickets.json
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8080/tickets -d @ticket/tickets.json
sleep 5
echo -e "\nVerify Change Data Capture..."
docker exec -it kafka /bin/bash -c "cat data/1/dbserver1.public.ticketevent-0/00000000000000000000.log"

