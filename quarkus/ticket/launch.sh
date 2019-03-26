#!/bin/bash

ocp_admin_url=localhost:8443
ocp_docker_registry=172.30.1.1:5000
ocp_user=developer
ocp_user_token=$(oc whoami -t)
ocp_namespace=saga-playground
docker_tag_name=ticket
image_version=latest

#create ocp project
oc login ${ocp_admin_url} --token ${ocp_user_token}
oc delete project ${ocp_namespace}
oc new-project ${ocp_namespace}

############################ Ticket Service

#create image
mvn package -Pnative -Dnative-image.docker-build=true
docker build -f Dockerfile.native -t ${docker_tag_name}:${image_version} .

#push image in ocp namespace registry
docker tag ${docker_tag_name}:${image_version} ${ocp_docker_registry}/${ocp_namespace}/${docker_tag_name}:${image_version}
docker login -u ${ocp_user} -p ${ocp_user_token} ${ocp_docker_registry}
docker push ${ocp_docker_registry}/${ocp_namespace}/${docker_tag_name}:${image_version}

#create application
oc new-app ${ocp_namespace}/${docker_tag_name}:${image_version}

#create route for business central application
oc expose service ${docker_tag_name} --port=8080 --hostname=business-central.example.com