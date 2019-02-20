 #!/bin/bash

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <git branch> <docker tag>"
  exit 1
fi

branch=$1
tag=$2
sudo docker build --build-arg IGUASSU_BRANCH=$branch --no-cache -t ufcglsd/iguassu:$tag .
