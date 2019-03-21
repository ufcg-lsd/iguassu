#!/bin/bash

MY_PATH="`dirname \"$0\"`"              # relative
MY_PATH="`( cd \"$MY_PATH\" && pwd )`"  # absolutized and normalized
if [ -z "$MY_PATH" ] ; then
  # error; for some reason, the path is not accessible
  # to the script (e.g. permissions re-evaled after suid)
  exit 1  # fail
fi

echo "Dir path: "$MY_PATH

DEPLOY_CONF_FILE=$MY_PATH"/deploy.conf"
echo "deploy.conf file path:" $DEPLOY_CONF_FILE

IMAGE_NAME_PATTERN="image_name"
IMAGE_NAME=$(grep $IMAGE_NAME_PATTERN $DEPLOY_CONF_FILE | awk -F "=" '{print $2}')
echo "Image Name: " $IMAGE_NAME

CONTAINER_NAME_PATTERN="container_name"
CONTAINER_NAME=$(grep $CONTAINER_NAME_PATTERN $DEPLOY_CONF_FILE | awk -F "=" '{print $2}')
echo "Container Name: " $CONTAINER_NAME

MANAGER_BACK_PORT_PATTERN="manager_back_port"
MANAGER_BACK_PORT=$(grep $MANAGER_BACK_PORT_PATTERN $DEPLOY_CONF_FILE | awk -F "=" '{print $2}')
echo "Manager Back Port: " $MANAGER_BACK_PORT

MANAGER_FRONT_PORT_PATTERN="manager_front_port"
MANAGER_FRONT_PORT=$(grep $MANAGER_FRONT_PORT_PATTERN $DEPLOY_CONF_FILE | awk -F "=" '{print $2}')
echo "Manager front port: " $MANAGER_FRONT_PORT

readonly CONF_FILES_DIR="conf-files"
readonly FRONTEND_PORT="9000"
readonly BACKEND_PORT="8080"

PRIVATE_KEY_FILE="fogbow_priv"

sudo docker stop $CONTAINER_NAME
sudo docker rm $CONTAINER_NAME
sudo docker pull $IMAGE_NAME

sudo docker run -idt \
    --name $CONTAINER_NAME \
    -p $MANAGER_FRONT_PORT:$FRONTEND_PORT \
    -p $MANAGER_BACK_PORT:$BACKEND_PORT \
    $IMAGE_NAME

sudo docker exec -d $CONTAINER_NAME /bin/bash -c 'mkdir .ssh'

sudo docker cp $DIR_PATH/$CONF_FILES_DIR/secret/. $CONTAINER_NAME:/root/.ssh
sudo docker cp $DIR_PATH/$CONF_FILES_DIR/backend-confs/. $CONTAINER_NAME:/root/iguassu
sudo docker cp $DIR_PATH/$CONF_FILES_DIR/frontend-confs/. $CONTAINER_NAME:/root/iguassu-dashboard/app

sudo docker exec -d $CONTAINER_NAME /bin/bash iguassu/bin/start-iguassu-service.sh
sudo docker exec -d $CONTAINER_NAME /bin/bash -c 'cd iguassu-dashboard && grunt serve'
