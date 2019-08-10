#!/bin/bash

## Installation environment and services script
## Run with root privileges. Only tested in Ubuntu 16.04 & 18.04 

echo '---> Installing OpenJDK-8 & Maven...'

apt-get install -y openjdk-8-jdk

apt-get install -y maven

echo '--->  Installing NodeJs...'
sudo apt-get install curl
curl -sL https://deb.nodesource.com/setup_11.x | sudo -E bash -
sudo apt-get install -y nodejs

install-iguassu() {
	echo '--->  Installing Iguassu'
	mvn clean install -DskipTests && cd ../
}

install-arrebol() {
	echo '--->  Installing Arrebol'
	wget https://github.com/ufcg-lsd/arrebol/archive/feature/remote-worker.zip
	unzip remote-worker && rm remote-worker.zip
	cd arrebol-remote-worker && mvn clean install -DskipTests && cd ../
}

install-web-ui() {
  echo '---> Installing Web UI'
	wget https://github.com/emanueljoivo/iwi/archive/master.zip
	unzip master.zip && rm master.zip
	cd iwi-master && npm install && ../
}

install-iguassu
install-arrebol
install-web-ui
