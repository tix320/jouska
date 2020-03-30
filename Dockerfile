FROM maven:3.6.3-jdk-11

WORKDIR /usr/src/app

COPY ./jouska-server/target/jouska-server.jar .
COPY ./jouska-server/target/classes/config.properties .

EXPOSE 8888

CMD cd /usr/src/app/ && java -jar jouska-server.jar
