#!/bin/bash

DOCKER_HOST=$(ip -4 addr show docker0 | grep -Po 'inet \K[\d.]+')
printf "\nDocker host: ${DOCKER_HOST}"

############################ Docker prune
echo -e "\nDeleting Docker containers running for Debezium...."

docker stop $(docker ps -a | grep connect | cut -d ' ' -f 1)
docker stop $(docker ps -a | grep kafka | cut -d ' ' -f 1)
docker stop $(docker ps -a | grep zookeeper | cut -d ' ' -f 1)
docker stop $(docker ps -a | grep postgres | cut -d ' ' -f 1)
docker stop $(docker ps -a | grep ticket | cut -d ' ' -f 1)
docker stop $(docker ps -a | grep payment| cut -d ' ' -f 1)
docker rm $(docker ps -a | grep connect | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep kafka | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep zookeeper | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep postgres | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep ticket | cut -d ' ' -f 1)
docker rm $(docker ps -a | grep payment | cut -d ' ' -f 1)


echo -e "\nPruning done. Starting application..."

############################ Postgres

echo -e "\nStart Postgresql container...."
docker run -d --name postgres -p 5432:5432 debezium/postgres
sleep 5
echo -e "\nCREATE tickets database...."
docker exec -it postgres psql -h localhost -p 5432 -U postgres -c 'CREATE DATABASE tickets;'
sleep 5
echo -e "\nCREATE payments database...."
docker exec -it postgres psql -h localhost -p 5432 -U postgres -c 'CREATE DATABASE payments;'
echo -e "\nPostgresql started."

############################ Zookeeper
sleep 5
echo -e "\nStart Zookeeper container...."
docker run -d --name zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 debezium/zookeeper
echo -e "\nZookeeper started."

############################ Kafka
sleep 5
echo -e "\nStart Kafka container...."
docker run -d --name kafka -p 9092:9092 --link zookeeper:zookeeper debezium/kafka
echo -e "\nKafka started."

############################ Debezium - Kafka Connect with transformation
sleep 5
echo -e "\nStart Debezium Kafka connect container...."
docker run -d --name connect -p 8083:8083 -e BOOTSTRAP_SERVERS=kafka:9092 -e GROUP_ID=1 -e CONNECT_KEY_CONVERTER_SCHEMAS_ENABLE=false -e CONNECT_VALUE_CONVERTER_SCHEMAS_ENABLE=false -e CONFIG_STORAGE_TOPIC=my-connect-configs -e OFFSET_STORAGE_TOPIC=my-connect-offsets -e ADVERTISED_HOST_NAME=${DOCKER_HOST} --link zookeeper:zookeeper --link postgres:postgres --link kafka:kafka hifly81/debezium-connect
sleep 5
echo -e "\nCREATE kafka connector ticket-connector...."
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d @debezium/ticket-connector.json
sleep 5
echo -e "\nCREATE kafka connector payment-connector...."
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d @debezium/payment-connector.json
echo -e "\nKafka Connect started."

########################### Ticket Application
sleep 5
echo -e "\nStart Ticket Application container..."
docker run -d --name ticket -p 8080:8080 --link postgres:postgres --link zookeeper:zookeeper --link kafka:kafka hifly81/quarkus-ticket-service
echo -e "\nTicket Application started."

########################### Payment Application
sleep 5
echo -e "\nStart Payment Application container..."
docker run -d --name payment -p 8090:8080 --link postgres:postgres --link zookeeper:zookeeper --link kafka:kafka hifly81/quarkus-payment-service
echo -e "\nPayment Application started."

########################### Verify Environment
sleep 5
echo -e "\nAdd 1 ticket..."
echo -e "\nResponse:"
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8080/tickets -d @ticket/tickets.json
sleep 5
echo -e "\n\nVerify TicketEvent Table..."
docker exec -it postgres psql -h localhost -p 5432 -U postgres -d tickets -c 'select * from ticketevent;'
sleep 5
echo -e "\n\nVerify PaymentEvent Table..."
docker exec -it postgres psql -h localhost -p 5432 -U postgres -d payments -c 'select * from paymentevent;'

