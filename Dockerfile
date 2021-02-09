# NOTE: maven build must be executed before docker image build
FROM maven:3.6.3-jdk-11

WORKDIR /usr/src/app

COPY ./jouska-server/target/jouska-server.jar .
ADD ./client-app ./client-app/

EXPOSE 8888

CMD cd /usr/src/app/ && java -jar jouska-server.jar
