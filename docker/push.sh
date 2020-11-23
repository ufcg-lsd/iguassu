#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <image name>"
    exit 1
fi

readonly IMAGE=ufcglsd/iguassu
TAG=$1

sudo docker push "$IMAGE":"$TAG"