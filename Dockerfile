FROM openjdk:alpine
MAINTAINER USGS LCMAP http://eros.usgs.gov

ARG version
ENV jarfile lcmap-nemo-$version-standalone.jar
ENV HTTP_PORT 5759
EXPOSE 5759

RUN mkdir /app
WORKDIR /app
COPY target/$jarfile $jarfile
COPY resources/log4j.properties log4j.properties

ENTRYPOINT java -server -XX:+UseG1GC -jar $jarfile