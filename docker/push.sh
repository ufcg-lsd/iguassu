 #!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <docker image name>"
  exit 1
fi

sudo docker push $1
