FROM ubuntu:latest
MAINTAINER nealhardesty@yahoo.com

WORKDIR /root

RUN apt-get update && apt-get upgrade -y

RUN apt-get install -y wget openjdk-8-jdk

ADD . /root

RUN ./sbt compile

CMD /root/sbt run
