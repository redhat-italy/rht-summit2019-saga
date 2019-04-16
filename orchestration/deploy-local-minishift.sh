### To deploy your booster to a running single-node OpenShift cluster:[source,bash,options="nowrap",subs="attributes+"]
echo ---- Start deploy ticket microservice

cd ticket
./deploy-local-minishift.sh

echo ---- End deploy ticket microservice

echo ---- Start deploy Narayana LRA

cd ../narayana
./deploy-local-minishift.sh

echo ---- End deploy Narayana LRA

echo ---- Start deploy insurance microservice

cd ../insurance
./deploy-local-minishift.sh

echo ---- End deploy insurance microservice

echo ---- Start deploy payment microservice

cd ../payment
./deploy-local-minishift.sh

echo ---- End deploy payment microservice

cd ../booking-client
./deploy-local-minishift.sh

echo ---- End deploy booking-client microservice

