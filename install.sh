#!/bin/bash

## Installation environment and services script
## Run with root privileges. Only tested in Ubuntu 16.04 & 18.04 

echo 'Verifying Docker installation'

apt update -y

CHECK_DOCKER_INSTALLATION=$(dpkg -l | grep -c docker-ce)

if ! [ $CHECK_DOCKER_INSTALLATION -ne 0 ]
then
    echo 'Installing Docker'
    apt-get install -y \
        apt-transport-https \
        ca-certificates \
        curl \
        software-properties-common

    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -

    apt-key fingerprint 0EBFCD88

    add-apt-repository \
    "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
    $(lsb_release -cs) \
    stable"

    apt-get update -y

    apt-get install -y docker-ce
fi

echo 'Configuring docker-compose'


if ! [ -f "/usr/local/bin/docker-compose" ]; then
    curl -L "https://github.com/docker/compose/releases/download/1.23.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
fi

echo 'Installing OpenJDK-8 & Maven...'

apt-get install -y openjdk-8-jdk

apt-get install -y maven

echo 'Installing NodeJs...'
sudo apt-get install curl
curl -sL https://deb.nodesource.com/setup_11.x | sudo -E bash -
sudo apt-get install -y nodejs

install-iguassu() {
	echo 'Installing Iguassu'
	wget https://github.com/ufcg-lsd/iguassu/archive/feature/database-upgrade.zip
	unzip database-upgrade.zip && rm database-updagrade.zip
	cd iguassu-database-updagrade && mvn clean install -DskipTests && cd ../
}

install-arrebol() {
	echo 'Installing Arrebol'
	wget https://github.com/ufcg-lsd/arrebol/archive/feature/remote-worker.zip
	unzip remote-worker && rm remote-worker.zip
	cd arrebol-remote-worker && mvn clean install -DskipTests && cd ../
}

install-web-ui() {
	wget https://github.com/emanueljoivo/iwi/archive/master.zip
	unzip master.zip && rm master.zip
	cd iwi-master && npm install && ../
}

install-iguassu
install-arrebol
install-web-ui


