oc delete project saga-playgrounds
oc new-project saga-playgrounds
oc create serviceaccount runasanyuid
oc new-app debezium/postgres
oc patch dc/postgres --patch '{"spec":{"template":{"spec":{"serviceAccountName": "runasanyuid"}}}}'


oc exec $(oc get pods | grep postgres | cut -d " " -f1) -- bash -c 'psql -h localhost -p 5432 -U postgres -c "CREATE DATABASE tickets;"'
oc exec $(oc get pods | grep postgres | cut -d " " -f1) -- bash -c 'psql -h localhost -p 5432 -U postgres -c "CREATE DATABASE payments;"'
oc exec $(oc get pods | grep postgres | cut -d " " -f1) -- bash -c 'psql -h localhost -p 5432 -U postgres -c "CREATE DATABASE insurances;"'


oc apply -f install/cluster-operator/020-RoleBinding-strimzi-cluster-operator.yaml -n saga-playgrounds
oc apply -f install/cluster-operator/031-RoleBinding-strimzi-cluster-operator-entity-operator-delegation.yaml -n saga-playgrounds
oc apply -f install/cluster-operator/032-RoleBinding-strimzi-cluster-operator-topic-operator-delegation.yaml -n saga-playgrounds
oc apply -f install/cluster-operator -n saga-playgrounds
oc apply -f examples/kafka/kafka-ephemeral.yaml
#oc apply -f examples/kafka-connect/kafka-connect-s2i.yaml
#oc delete dc/my-connect-cluster-connect

# Build a Debezium image
#cd debezium
#mvn clean install
#mkdir -p target/plugins && cd target/plugins
#export DEBEZIUM_VERSION=0.9.4.Final
#for PLUGIN in {postgres}; do curl http://central.maven.org/maven2/io/debezium/debezium-connector-$PLUGIN/$DEBEZIUM_VERSION/debezium-connector-$PLUGIN-$DEBEZIUM_VERSION-plugin.tar.gz | tar xz; done
#cp ../debezium-1.0.jar .
#oc start-build my-connect-cluster-connect --from-dir .
#oc expose service my-connect-cluster-connect-api


oc new-app quay.io/bridlos/outbox-debezium -e ES_DISABLED=true

cd ../../connector
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://my-connect-cluster-connect-api-saga-playgrounds.apps.nodisk.space/connectors/ -d @ticket-connector.json
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://my-connect-cluster-connect-api-saga-playgrounds.apps.nodisk.space/connectors/ -d @order-connector.json
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://my-connect-cluster-connect-api-saga-playgrounds.apps.nodisk.space/connectors/ -d @payment-connector.json


oc new-app quay.io/bridlos/ticket-service-quarkus
oc expose svc/ticket-service-quarkus
oc new-app quay.io/bridlos/insurance-service-quarkus
oc expose svc/insurance-service-quarkus
oc new-app quay.io/bridlos/payment-service-quarkus
oc expose svc/payment-service-quarkus