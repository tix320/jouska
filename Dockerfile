FROM maven:3.6.3-jdk-11

COPY ./jouska-server/target/jouska-server.jar /usr/src/app/

EXPOSE 8888

CMD cd /usr/src/app/ && java -jar jouska-server.jar
