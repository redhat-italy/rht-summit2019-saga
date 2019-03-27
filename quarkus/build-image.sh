#!/bin/bash

image_ticket_name=ticket
image_ticket_version=latest

############################ Ticket Service

#create image
cd ticket/
mvn package -Pnative -Dnative-image.docker-build=true
docker build -f Dockerfile.native -t ${image_ticket_name}:${image_ticket_version} .