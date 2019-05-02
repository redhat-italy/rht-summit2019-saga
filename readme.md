Saga microservices Playground
=============================

## Saga Choreography with Kafka, Debezium and Quarkus

### Launch on OpenShift

An already running OCP cluster is available at:<br>
https://ocp.nodisk.space:8443/console/project/saga-playgrounds/overview

Ticket service:<br>
http://ticket-service-quarkus-saga-playgrounds.apps.nodisk.space

Insurance service:<br>
http://insurance-service-quarkus-saga-playgrounds.apps.nodisk.space

Outbox connect service:<br>
http://outbox-connect-service-quarkus-saga-playgrounds.apps.nodisk.space

Images are downloaded from docker hub and from https://quay.io

Images:
 - Postgres (image debezium/postgres) on port 5432
 - AMQ Streams (Zookeeper on port 2181 and Kafka on port 9092)
 - Kafka Connect + Debezium (image quay.io/bridlos/outbox-connect) on port 8083
 - Ticket Service (image quay.io/bridlos/ticket-service-quarkus) on port 8080
 - Insurance Service (image quay.io/bridlos/insurance-service-quarkus) on port 8080
 - Payment Service (image quay.io/bridlos/payment-service-quarkus) on port 8080

 Elastic Search and Kibana are not installed in Openshift (available only for local installation).

 Run a simulation:

```bash
cd simulation/
./test-ocp-saga.sh
./test-ocp-saga-failed.sh
```

Watch a video of a simulation at (set video to quality 1080):<br>
https://www.youtube.com/watch?v=7cLbRIc3TWU
[![Alt text](http://www.myiconfinder.com/uploads/iconsets/32-32-3a1eef40f04875d93dd6545f2f1b727e-youtube.png)](https://www.youtube.com/watch?v=7cLbRIc3TWU)

In order to create the demo on your openshift environment, you need:
 - ocp user with cluster-admin role
 - oc client installed on your machine (tested with 3.11.x)
 - AMQ Streams 1.1 for ocp downloaded from Red Hat<br>
 https://access.redhat.com/jbossnetwork/restricted/listSoftware.html?downloadType=distributions&product=jboss.amq.streams

Follow these instructions to create the demo:

Login to OCP, create a new project, create e new service account runasanyuid (postgres must run as root):
```bash
oc login <ocp_master_url> --token=<ocp_user_token>
oc new-project saga-playgrounds
oc create serviceaccount runasanyuid
```

Create postgres, then create tickets, insurances and payments database:
```bash
oc new-app debezium/postgres
oc patch dc/postgres --patch '{"spec":{"template":{"spec":{"serviceAccountName": "runasanyuid"}}}}'


oc exec $(oc get pods | grep postgres | cut -d " " -f1) -- bash -c 'psql -h localhost -p 5432 -U postgres -c "CREATE DATABASE tickets;"'
oc exec $(oc get pods | grep postgres | cut -d " " -f1) -- bash -c 'psql -h localhost -p 5432 -U postgres -c "CREATE DATABASE payments;"'
oc exec $(oc get pods | grep postgres | cut -d " " -f1) -- bash -c 'psql -h localhost -p 5432 -U postgres -c "CREATE DATABASE insurances;"'
```

Install AMQ Streams cluster operator and a kafka cluster with 3 brokers (ephemeral and with prometheus metrics).<br>
This step requires that you've downloaded and unpacked the AMQ Streams zip archive for OCP <br>(for more info about the installation, https://access.redhat.com/documentation/en-us/red_hat_amq/7.2/html-single/using_amq_streams_on_openshift_container_platform/index)


```bash
sed -i 's/namespace: .*/namespace: saga-playgrounds/' install/cluster-operator/*RoleBinding*.yaml
oc apply -f install/cluster-operator/020-RoleBinding-strimzi-cluster-operator.yaml -n saga-playgrounds
oc apply -f install/cluster-operator/031-RoleBinding-strimzi-cluster-operator-entity-operator-delegation.yaml -n saga-playgrounds
oc apply -f install/cluster-operator/032-RoleBinding-strimzi-cluster-operator-topic-operator-delegation.yaml -n saga-playgrounds
oc apply -f install/cluster-operator -n saga-playgrounds
oc apply -f examples/metrics/kafka-metrics.yaml
```

Create the outbox-connect application:
```bash
oc new-app quay.io/bridlos/outbox-connect -e ES_DISABLED=true -e BOOTSTRAP_SERVERS=my-cluster-kafka-bootstrap:9092 -e GROUP_ID=1 -e CONNECT_KEY_CONVERTER_SCHEMAS_ENABLE=false -e CONNECT_VALUE_CONVERTER_SCHEMAS_ENABLE=false -e CONFIG_STORAGE_TOPIC=my-connect-configs -e OFFSET_STORAGE_TOPIC=my-connect-offsets
oc expose svc/outbox-connect
```

Install the debezium connectors:
```bash
cd debezium/connector/

curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://<outbox-connect-url>/connectors/ -d @ticket-connector.json
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://<outbox-connect-url>/connectors/ -d @order-connector.json
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://<outbox-connect-url>/connectors/ -d @payment-connector.json
```

Install ticket, insurance and payment microservice:
```bash
oc new-app quay.io/bridlos/ticket-service-quarkus
oc expose svc/ticket-service-quarkus
oc new-app quay.io/bridlos/insurance-service-quarkus
oc expose svc/insurance-service-quarkus
oc new-app quay.io/bridlos/payment-service-quarkus
```

Install prometheus and grafana:
```bash
wget https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/0.10.0/metrics/examples/prometheus/kubernetes.yaml
mv kubernetes.yaml prometheus.yaml
oc apply -f prometheus.yaml -n saga-playgrounds
oc adm policy add-cluster-role-to-user prometheus -z prometheus-server
wget https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/0.10.0/metrics/examples/grafana/kubernetes.yaml
mv kubernetes.yaml grafana.yaml
oc apply -f grafana.yaml -n saga-playgrounds
oc expose svc/grafana
```

Download and import grafana dashboard for kafka and zookeeper, dashboard can be downloaded at:<br>
wget https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/master/metrics/examples/grafana/strimzi-kafka.json<br>
wget https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/master/metrics/examples/grafana/strimzi-zookeeper.json

Follow the instruction to import the kafka and zookeeper grafana dashboards:<br>
https://strimzi.io/docs/latest/#grafana_dashboard

Grafana dashboards:

![ScreenShot 1](choreography/images/kafka.png)

![ScreenShot 2](choreography/images/zookeeper.png)


### Launch on local env - linux and mac

Launch the bootstrap script to create the docker containers.<br>
Images are downloaded from docker hub and from quay.io.

Images:
 - Postgres (image debezium/postgres) on local port 5432
 - Elastic Search + Kibana (image nshou/elasticsearch-kibana) on local port 9200 and 5601 (kibana)
 - Zookeeper (image debezium/zookeeper) on local port 2181
 - Kafka (image debezium/kafka) on local port 9092
 - Kafka Connect + Debezium (image quay.io/bridlos/outbox-connect) on local port 8083
 - Ticket Service (image quay.io/bridlos/ticket-service-quarkus) on local port 8080
 - Insurance Service (image quay.io/bridlos/insurance-service-quarkus) on local port 8090
 - Payment Service (image quay.io/bridlos/payment-service-quarkus) on local port 8100

```bash
cd choreography/
./deploy-docker.sh
```

Run a simulation:

```bash
cd simulation/
./test-saga.sh
./test-saga-failed.sh
```

2 tickets will be created.

2 insurances will be created.

### Verification

This is the final state inside the microservices databases at the end of the 2 sagas:

![ScreenShot 1](choreography/images/ticketevent.png)

![ScreenShot 2](choreography/images/orderevent.png)

![ScreenShot 3](choreography/images/paymentevent.png)

![ScreenShot 4](choreography/images/tickettable.png)

![ScreenShot 5](choreography/images/insurancetable.png)

![ScreenShot 6](choreography/images/accounttable.png)

Events as stored in Elastic Search (No Openshift) (Kibana view):

![ScreenShot 7](choreography/images/kibana.png)

### Compile and Create Images

Launch the script to compile and create the images:

```bash
cd choreography/
./build-image.sh
```

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
