### To deploy your booster to a running single-node OpenShift cluster:[source,bash,options="nowrap",subs="attributes+"]
echo ---- Start deploy Narayana LRA
oc login -u developer -p developer
oc project summit-saga
oc create -f lra-coordinator.yaml
echo ---- End deploy Narayana LRA