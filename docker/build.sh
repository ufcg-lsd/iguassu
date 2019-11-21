#!/bin/bash

if [[ "$#" -ne 1 ]]; then
  echo "Usage: $0 <docker tag>"
  exit 1
fi

readonly IMAGE=ufcglsd/iguassu
TAG=$1

sudo docker build -t "$IMAGE":"$TAG" -f docker/Dockerfile .