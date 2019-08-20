#!/bin/bash

DIRNAME=`dirname $0`
LOG4J=log4j.properties

cd $DIRNAME/..

if [ -f $LOG4J ]; then
	CONF_LOG=-Dlog4j.configuration=file:$LOG4J
else	
	CONF_LOG=
fi

nohup mvn spring-boot:run &
exit 0
