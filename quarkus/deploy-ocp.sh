#!/bin/bash

ocp_admin_url=
ocp_docker_registry=
ocp_user=
ocp_user_token=$(oc whoami -t)
ocp_namespace=saga-playground
image_ticket_name=ticket
image_ticket_version=latest

#create ocp project
oc login ${ocp_admin_url} --token ${ocp_user_token}
oc delete project ${ocp_namespace}
oc new-project ${ocp_namespace}

############################ Ticket Service

#create application
oc new-app ${ocp_namespace}/${image_ticket_name}:${image_ticket_version}

#create route for business central application
oc expose service ${image_ticket_name} --port=8080 --hostname=ticket.example.com