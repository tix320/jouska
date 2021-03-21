# NOTE: maven build must be executed before docker image build
FROM openjdk:16-jdk-oraclelinux7

WORKDIR /usr/src/app

COPY ./jouska-server/target/jouska-server.jar .
COPY ./client-applications ./client-applications/

EXPOSE 8888

CMD cd /usr/src/app/ && java -jar jouska-server.jar
