FROM openjdk:8

ARG IGUASSU_BRANCH
ARG BLOWOUT_BRANCH=adapt-new-fogbow
ARG IGUASSU_DASHBOARD_BRANCH=support/without-auth

#install
RUN \
    apt-get update -y && \
    apt-get upgrade -y && \
    apt-get install -y maven curl git lsof && \
    curl -sL https://deb.nodesource.com/setup_11.x | bash - && \
    apt-get install -y nodejs

RUN \
    apt-get install -y ruby-full && \
    npm install -g grunt-cli bower yo generator-karma generator-angular

RUN \
    apt-get install -y ruby-compass && \
    gem install compass

#Set environment variables
ENV HOME /root

#Define working directory
WORKDIR /root

# Cloning and Setting
RUN \
    git clone -b $BLOWOUT_BRANCH https://github.com/ufcg-lsd/blowout.git --single-branch && \
    (cd blowout && mvn clean install -DskipTests)

WORKDIR /root

RUN \
    git clone -b $IGUASSU_BRANCH https://github.com/ufcg-lsd/iguassu.git && \
    (cd iguassu && git branch && mvn clean install -DskipTests)

WORKDIR /root

RUN \
    git clone -b $IGUASSU_DASHBOARD_BRANCH https://github.com/fogbow/iguassu-dashboard.git --single-branch && \
    (cd iguassu-dashboard && npm install && bower install --allow-root)
    
    
