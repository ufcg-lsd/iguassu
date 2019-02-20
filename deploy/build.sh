#!/usr/bin/env bash

##
# Helper functions.
##

fetch_remote_branchs(){
  for branch in `git branch -a | grep remotes | grep -v HEAD | grep -v master `; do
     git branch --track ${branch#remotes/origin/} $branch
  done
}

print_spaces(){
  echo "-"
  echo "-"
  echo "-"
  echo "-"
}

##
# Script main body.
##

echo "This script build a Iguassu instance."
print_spaces

echo "Cloning Blowout"
print_spaces
git clone -b adapt-new-fogbow git@github.com:ufcg-lsd/blowout.git --single-branch
echo "Installing Blowout packages"
print_spaces
cd blowout && mvn clean install -DskipTests

echo "Cloning Iguassu"
print_spaces
cd ../ && git clone git@github.com:ufcg-lsd/iguassu.git && cd iguassu/
fetch_remote_branchs
echo "Installing Iguassu packages"
git checkout master && mvn clean install -DskipTests

echo "Cloning Iguassu Dashboard"
print_spaces
cd ../ && git clone -b support/without-auth https://github.com/fogbow/iguassu-dashboard.git --single-branch && cd iguassu-dashboard/
echo "Installing Iguassu Dashboard packages"
npm install
sudo bower install --allow-root

print_spaces
echo "Done."
