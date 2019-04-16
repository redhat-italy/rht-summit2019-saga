### To deploy your booster to a running single-node OpenShift cluster:[source,bash,options="nowrap",subs="attributes+"]
echo ---- Start deploy
oc login -u developer -p developer

MY_PROJECT_NAME="summit-saga"

oc project $MY_PROJECT_NAME

# Ensure that you use the following values for the user name, password and database name when creating your database application.
# The pre-configured values are used in the `credentials-secret.yml` and `deployment.yml` files in the `src/main/fabric8` directory of your booster application project.

oc new-app -e POSTGRESQL_USER=mauro -ePOSTGRESQL_PASSWORD=secret -ePOSTGRESQL_DATABASE=insurance openshift/postgresql-92-centos7 --name=insurance-database

# Wait for `insurance-database` application to be running.

mvn clean fabric8:deploy -Popenshift
echo ---- End deploy