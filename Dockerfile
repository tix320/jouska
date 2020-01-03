FROM maven:3.6.3-jdk-11

COPY . /usr/src/app/

RUN cd /usr/src/app/ && mvn clean install

EXPOSE 8888

CMD cd /usr/src/app/jouska-server/target && java -jar jouska-server.jar
