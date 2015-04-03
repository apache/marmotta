# Dockerfile for Apache Marmotta

FROM debian:jessie

MAINTAINER Sergio Fernández <wikier@apache.org>

# general configuration
ENV DEBIAN_FRONTEND noninteractive
ENV baseurl http://localhost:8080/marmotta
ENV dbname marmotta
ENV dbuser marmotta
ENV dbpass s3cr3t
WORKDIR /code

# make sure the package repository is up to date
RUN apt-get update

# install some required tools
RUN apt-get install -y git maven openjdk-7-jdk postgresql curl gdebi
RUN apt-get clean

# get the source code
RUN git clone -b develop https://git-wip-us.apache.org/repos/asf/marmotta.git
RUN cd marmotta && git pull

# package from source code
# RUN cd marmotta && mvn install -DskipTests -DskipITs -Djdeb.signing=false
RUN cd marmotta/launchers/marmotta-webapp && mvn package -Djdeb.signing=false

# install webapp
RUN gdebi marmotta/launchers/marmotta-webapp/target/marmotta*.deb

# setup database
RUN sudo -u postgres psql -c "CREATE USER $dbuser WITH PASSWORD '$dbpass'"
RUN sudo -u postgres psql -c "CREATE DATABASE $dbname WITH OWNER $dbuser"

# wait until it actually boots
until [ "`curl --silent --show-error --connect-timeout 1 -I $baseurl/ | grep 'Coyote'`" != "" ]; do echo "marmotta not yet ready, sleeping for 10 seconds..." && sleep 10 done

# for development purposes disable security
RUN curl -X POST -H "Content-Type: application/json" -d '["false"]' $baseurl/config/data/security.enabled

# configure postgres as database
RUN curl -X POST -H "Content-Type: application/json" -d '["postgres"]' $baseurl/config/data/database.type
RUN curl -X POST -H "Content-Type: application/json" -d '["jdbc:postgresql://localhost:5432/$dbname?prepareThreshold=3"]' $baseurl/config/data/database.url
RUN curl -X POST -H "Content-Type: application/json" -d '["$dbuser"]' $baseurl/config/data/database.user
RUN curl -X POST -H "Content-Type: application/json" -d '["$dbpass"]' $baseurl/config/data/database.password
RUN curl -X POST $baseurl/system/database/reinit

EXPOSE 8080

