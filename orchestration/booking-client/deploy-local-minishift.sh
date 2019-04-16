### To deploy your booster to a running single-node OpenShift cluster:[source,bash,options="nowrap",subs="attributes+"]
echo ---- Start deploy
oc login -u developer -p developer

MY_PROJECT_NAME="summit-saga"

oc project $MY_PROJECT_NAME

mvn clean fabric8:deploy -Popenshift
echo ---- End deploy