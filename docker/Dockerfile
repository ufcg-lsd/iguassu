FROM openjdk:8

#Define working directory
WORKDIR /service

COPY ./bin/iguassu-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar", "-c" , "./config/iguassu.conf"]
