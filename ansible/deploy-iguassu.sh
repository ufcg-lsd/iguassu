#!/bin/bash

DIR_PATH=$(pwd)
CONF_FILES_DIR="conf-files"

IMAGE_NAME="wesleymonte/iguassu"
CONTAINER_NAME="iguassu"

MANAGER_PORT="8080"
CONTAINER_PORT="9000"


sudo docker stop $CONTAINER_NAME
sudo docker rm $CONTAINER_NAME
sudo docker pull $IMAGE_NAME

sudo docker run -idt \
    --name $CONTAINER_NAME \
    -p $MANAGER_PORT:$CONTAINER_PORT \
    $IMAGE_NAME

sudo docker cp $DIR_PATH/$CONF_FILES_DIR/backend-confs/. $CONTAINER_NAME:/root/iguassu
sudo docker cp $DIR_PATH/$CONF_FILES_DIR/frontend-confs/. $CONTAINER_NAME:/root/iguassu-dashboard/app

sudo docker exec -d $CONTAINER_NAME /bin/bash iguassu/bin/start-iguassu-service.sh
sudo docker exec -d $CONTAINER_NAME /bin/bash -c 'cd iguassu-dashboard && grunt serve'


