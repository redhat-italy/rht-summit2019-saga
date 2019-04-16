Saga microservices Playground
=============================

## Saga Choreography with Quarkus

### Create Native Image

Launch the script to create the native images:

```bash
cd quarkus/
./build-image.sh
```

### Launch on local env - linux and mac

Launch the bootstrap script to create the docker containers.<br>
Images are downloaded from docker hub.

Containers:
 - Postgres (image debezium/postgres) on local port 5432
 - Elastic Search + Kibana (image nshou/elasticsearch-kibana) on local port 9200 and 5601 (kibana)
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
./test-saga-failed.sh
```

2 tickets will be created.

2 insurances will be created.

### Deploy on OpenShift

Launch the bootstrap script to create your namespace.<br>
Images are downloaded from docker hub.

Containers:
 - Postgres (image debezium/postgres)
 - Zookeeper (image debezium/zookeeper)
 - Elastic + Kibana (image nshou/elasticsearch-kibana)
 - Kafka (image debezium/kafka)
 - Kafka Connect + Debezium (image hifly81/debezium-connect)
 - Ticket Service (image hifly81/quarkus-ticket-service)
 - Insurance Service (image hifly81/quarkus-insurance-service)
 - Payment Service (image hifly81/quarkus-payment-service)

```bash
cd quarkus/
./deploy-ocp.sh
```

### Verification

This is the final state inside the microservices databases at the end of the 2 sagas:

![ScreenShot 1](quarkus/images/ticketevent.png)

![ScreenShot 2](quarkus/images/orderevent.png)

![ScreenShot 3](quarkus/images/paymentevent.png)

![ScreenShot 4](quarkus/images/tickettable.png)

![ScreenShot 5](quarkus/images/insurancetable.png)

![ScreenShot 6](quarkus/images/accounttable.png)

Events as stored in Elastic Search (Kibana view):

![ScreenShot 7](quarkus/images/kibana.png)

# Saga Orchestration with Eclipse Microprofile LRA and Openshift
This project is an example of Saga orchestration implementation using Eclipse Microprofile LRA and Openshift.

It created five different components:

* Three microservices:
  * Ticket
  * Insurance
  * Payment
* Eclipse Microprofile LRA coordinator:
  * Narayana
* An API Gateway that orchestrates the invocation of the microservices and interacts with the LRA coordinator:
  * Booking client

# Requirements

In order to build and run it you need:

- OpenJDK 1.8 or above;
- Apache Maven 3.5.4 or above;
- Minishift v1.32.0 or above;

# Running the demo

This project uses the Fabric8 Maven Plugin to deploy itself automatically to Openshift or Kubernetes.
To run the demo you need to start minishift:

```sh
$ minishift start --cpus 4 --memory 6144
```

Then you install and configure all components with the script:
```sh
$ ./deploy-local-minishift.sh
```

# Test the SAGA
The project gives two built in examples to test examples:

- Test of SUCCESS SAGA:
```sh
$ ./test-saga.sh
```

You should expect a result lile this
```sh
./test-saga.sh 

Start Test Saga

Buying Ticket
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   121  100     3  100   118      1     67  0:00:03  0:00:01  0:00:02    67
Order Id:  + 131

Ticket bought

Get Ticket
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   191  100   191    0     0  13785      0 --:--:-- --:--:-- --:--:-- 14692
Ticket:  + {"id":4,"orderId":"131","state":"TICKET_BOOKED","accountId":"AA2","name":"Lady Gaga - NYC 18 june 2019","numberOfPersons":"1","totalCost":60.0,"lraId":"0_ffffac110006_-6ddc8291_5cb48169_142"}

Get Insurance
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   168  100   168    0     0  13296      0 --:--:-- --:--:-- --:--:-- 14000
Insurance:  + {"id":4,"orderId":"131","state":"INSURANCE_BOOKED","accountId":"AA2","ticketId":4,"name":"PROTECT_ALL","totalCost":30.0,"lraId":"0_ffffac110006_-6ddc8291_5cb48169_142"}

Get Payment
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   134  100   134    0     0   5150      0 --:--:-- --:--:-- --:--:--  5360
Payment:  + {"id":4,"orderId":"131","orderCost":90.0,"state":"PAYMENT_ACCEPTED","accountId":"AA2","lraId":"0_ffffac110006_-6ddc8291_5cb48169_142"}

End Test Saga
```

All the state of the entity should be `BOOKED` or `ACCEPTED`


- Test of FAIL SAGA:
```sh
$ ./test-saga-failed.sh
```

You should expect a result lile this
```sh
./test-saga-failed.sh 

Start Test Saga

Buying Ticket
Order Id:  + 135

Ticket Refused

Get Ticket
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   202  100   202    0     0  21335      0 --:--:-- --:--:-- --:--:-- 22444
Ticket:  + {"id":5,"orderId":"135","state":"TICKET_AVAILABLE","accountId":"AA2","name":"Eminem - Atlanta 20th September 2019","numberOfPersons":"1","totalCost":90.0,"lraId":"0_ffffac110006_-6ddc8291_5cb48169_153"}

Get Insurance
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   177  100   177    0     0  17569      0 --:--:-- --:--:-- --:--:-- 17700
Insurance Refused:  + {"id":5,"orderId":"135","state":"INSURANCE_PAYMENT_REFUSED","accountId":"AA2","ticketId":5,"name":"PROTECT_ALL","totalCost":45.0,"lraId":"0_ffffac110006_-6ddc8291_5cb48169_153"}

Get Payment
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   134  100   134    0     0  13815      0 --:--:-- --:--:-- --:--:-- 14888
Payment Refused:  + {"id":5,"orderId":"135","orderCost":135.0,"state":"PAYMENT_REFUSED","accountId":"AA2","lraId":"0_ffffac110006_-6ddc8291_5cb48169_153"}

End Test Saga
```

All the state of the entity should be `AVAILABLE` or `REFUSED`

## WARNING
Pay attention that the two built in examples are not idempotent: use them as scaffold for other test or change at least the orderId value


