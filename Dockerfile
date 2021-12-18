# FROM openjdk:14-alpine
FROM ubuntu:20.04

RUN apt-get update && \
    apt-get install -y openjdk-17-jre-headless graphviz && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ARG version=0.0.0
ENV C4VIZ_VERSION=${version}

COPY backend/build/libs/c4viz-${version}.jar /c4viz-${version}.jar

ENTRYPOINT [ "bash", "-c", "java -jar /c4viz-${C4VIZ_VERSION}.jar \"$@\"", "--" ]
