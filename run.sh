#!/bin/bash

../

./iguass/bin/start-iguassu-service.sh &
./arrebol/bin/start-service.sh &

yarn run serve --port 8082