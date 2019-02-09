#!/usr/bin/env bash

echo "This script installs all dependencies needed to the Iguassu."
echo "This script was tested only in Ubuntu 18.04"

echo "Starting..."
echo "Installing Oracle JDK8"
sudo apt-get purge openjdk*
sudo add-apt-repository -y ppa:webupd8team/java
sudo apt-get update
sudo apt-get install -y oracle-java8-installer
sudo apt-get install oracle-java8-set-default

echo "Installing Maven"
sudo apt install -y maven

echo "Installing Curl"
sudo apt install -y curl

echo "Installing Node Js"
curl -sL https://deb.nodesource.com/setup_11.x | sudo -E bash -
sudo apt-get install -y nodejs

echo "Installing dependencies from Node"
sudo npm install -g grunt-cli bower yo generator-karma generator-angular

echo "Installing Ruby"
sudo apt install -y ruby-full
sudo gem install compass

echo "Done."
