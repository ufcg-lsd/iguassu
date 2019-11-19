FROM openjdk:8

#Define working directory
WORKDIR /service

COPY ./bin/iguassu-1.0.0.jar app.jar
COPY ./bin/lib  lib/

ENTRYPOINT ["java", "-jar", "app.jar", "-c" , "./config/iguassu.conf"]