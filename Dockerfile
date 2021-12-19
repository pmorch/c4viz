# FROM openjdk:14-alpine
FROM ubuntu:20.04

RUN apt-get update && \
    apt-get install -y openjdk-17-jre-headless graphviz && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ARG version=0.0.0
ENV C4VIZ_VERSION=${version}
ENV C4VIZ_SOURCE_DIR=/sourceDir
ENV C4VIZ_CACHE=/var/cache/c4viz

RUN mkdir $C4VIZ_CACHE && \
    chown 1000:1000 $C4VIZ_CACHE

COPY backend/build/libs/c4viz-${version}.jar /c4viz-${version}.jar
COPY sourceDir /sourceDir

USER 1000:1000
RUN mkdir /tmp/cache

ENTRYPOINT [ \
    "bash", \
    "-c", \
    "java -jar /c4viz-${C4VIZ_VERSION}.jar \"$@\"", \
    "--" \
]
