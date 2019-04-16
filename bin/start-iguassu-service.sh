#!/bin/bash
DIRNAME=`dirname $0`
LOG4J=log4j.properties
cd $DIRNAME/..
if [ -f $LOG4J ]; then
CONF_LOG=-Dlog4j.configuration=file:$LOG4J
else
CONF_LOG=
fi

if [ -d "datastores/" ]; then
	if [ "$(ls -A datastores/)" ]; then
		rm datastores/*
	fi
else
	mkdir datastores
fi

if [ -f "/tmp/blowoutdb" ]; then
    rm /tmp/blowoutdb
fi

mvn spring-boot:run
