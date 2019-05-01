FROM openjdk:8

ARG IGUASSU_BRANCH

#install
RUN \
    apt-get update -y && \
    apt-get upgrade -y && \
    apt-get install -y maven curl git lsof && \
    curl -sL https://deb.nodesource.com/setup_11.x | bash - && \
    apt-get install -y nodejs

#Set environment variables
ENV HOME /root

#Define working directory
WORKDIR /root

RUN \
    git clone -b $IGUASSU_BRANCH https://github.com/ufcg-lsd/iguassu.git && \
    (cd iguassu && git branch && mvn clean install -DskipTests)
