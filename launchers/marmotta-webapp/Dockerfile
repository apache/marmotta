# Dockerfile for Apache Marmotta

FROM debian:sid
MAINTAINER Sergio Fernández <wikier@apache.org>
WORKDIR /src
EXPOSE 8080
EXPOSE 5432

# general configuration
ENV DEBIAN_FRONTEND noninteractive
ENV baseurl http://localhost:8080/marmotta
ENV dbname marmotta
ENV dbuser marmotta
ENV dbpass s3cr3t
ENV conf /var/lib/marmotta/system-config.properties

# install base system
RUN sed -i.bak s/"exit 101"/"exit 0"/g /usr/sbin/policy-rc.d
RUN apt-get update
RUN apt-get install -y apt-utils
RUN apt-get upgrade -y
RUN apt-get install -y sudo
RUN apt-get install -y locales
RUN locale-gen en_US.UTF-8 en_us && dpkg-reconfigure locales && dpkg-reconfigure locales && locale-gen C.UTF-8 && /usr/sbin/update-locale LANG=C.UTF-8
RUN apt-get install -y git ssmtp
RUN apt-get install -y openjdk-7-jre-headless openjdk-7-jdk maven
RUN apt-get install -y tomcat7
RUN apt-get install -y postgresql postgresql-common postgresql-contrib

# pre-setup
USER postgres
RUN psql -c "CREATE USER $dbuser WITH PASSWORD '$dbpass'"
RUN psql -c "CREATE DATABASE $dbname WITH OWNER $dbuser"
USER root
RUN mkdir -p "$(dirname $conf)"
RUN echo "" > $conf
RUN echo "security.enabled = false" >> $conf
RUN echo "database.type = postgres" >> $conf
RUN echo "database.url = jdbc:postgresql://localhost:5432/$dbname?prepareThreshold=3" >> $conf
RUN echo "database.user = $dbuser" >> $conf
RUN echo "database.password = $dbpas" >> $conf
RUN chmod -R tomcat7:tomcat7 "$(dirname $conf)"

# package from source code and install the webapp
RUN git clone -b develop https://git-wip-us.apache.org/repos/asf/marmotta.git
RUN cd marmotta/launchers/marmotta-webapp/ && mvn clean package -Djdeb.signing=false
RUN dpkg -i marmotta/launchers/marmotta-webapp/target/marmotta*.deb

# cleanup
#RUN cd marmotta/ && mvn clean
RUN apt-get clean -y && apt-get autoclean -y && apt-get autoremove -y
RUN mv /usr/sbin/policy-rc.d.bak /usr/sbin/policy-rc.d

