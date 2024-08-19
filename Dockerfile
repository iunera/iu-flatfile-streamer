FROM --platform=linux/amd64 maven:3.8.1-openjdk-17-slim as DEPS
WORKDIR /opt/app

COPY iu-transport-datatypes iu-transport-datatypes
COPY iu-flink-extensions iu-flink-extensions

RUN mvn clean install -f iu-transport-datatypes
# RUN mvn clean install -f iu-flink-extensions


FROM --platform=linux/amd64 maven:3.8.1-openjdk-17-slim as BUILDER
COPY --from=DEPS /root/.m2 /root/.m2

# copy the main repo 
COPY ./iu-flatfile-streamer .

# build for release
RUN mvn clean package -pl :iu-flatfile-streamer -DskipTests

FROM --platform=linux/amd64 openjdk:17-jdk-slim

RUN \
  apt-get update && \
  apt-get upgrade -y && \
  apt-get dist-upgrade -y && \
  apt-get autoclean && \
  apt-get autoremove && \
  rm -rf /var/lib/apt/lists/*

RUN addgroup spring && adduser --disabled-password  --gecos ''  spring --ingroup spring
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY --from=BUILDER ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
