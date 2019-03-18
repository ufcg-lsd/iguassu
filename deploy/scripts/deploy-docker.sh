#!/bin/bash

DIR_PATH=$(pwd)
CONF_FILES_DIR="conf-files"

IMAGE_NAME="ufcglsd/iguassu:latest"
CONTAINER_NAME="iguassu"

MANAGER_FRONT_PORT="9000"
MANAGER_BACK_PORT="8080"
FRONTEND_PORT="9000"
BACKEND_PORT="8080"

PRIVATE_KEY_FILE="fogbow_priv"

sudo docker stop $CONTAINER_NAME
sudo docker rm $CONTAINER_NAME
sudo docker pull $IMAGE_NAME

sudo docker run -idt \
    --name $CONTAINER_NAME \
    -p $MANAGER_FRONT_PORT:$FRONTEND_PORT \
    -p $MANAGER_BACK_PORT:$BACKEND_PORT \
    $IMAGE_NAME

sudo docker cp $DIR_PATH/$CONF_FILES_DIR/backend-confs/. $CONTAINER_NAME:/root/iguassu
sudo docker cp $DIR_PATH/$CONF_FILES_DIR/frontend-confs/. $CONTAINER_NAME:/root/iguassu-dashboard/app

sudo docker exec -d $CONTAINER_NAME /bin/bash iguassu/bin/start-iguassu-service.sh
sudo docker exec -d $CONTAINER_NAME /bin/bash -c 'cd iguassu-dashboard && grunt serve'
