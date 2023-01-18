FROM maven:3.6.3-jdk-11-slim

RUN mkdir /jar/

WORKDIR /jar/

COPY application/target/a.jar /jar/a.jar

ENV JAR_FILE=a LOG_PATH=/var/log/a

CMD ["java","-jar","/jar/a.jar"]
