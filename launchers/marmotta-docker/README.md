# Apache Marmotta Docker launcher

This launcher creates a Docker images that you can user for developing or testing
Apache Marmotta.

## Build image

    sudo docker build -t marmotta .

## Get image

@@TODO@@: push it to asf or docker hub

## Run

    docker run -p 8080:8080

## Accesss

Access Marmotta runing inside the container, at [172.17.42.1:8080/marmotta](http://172.17.42.1:8080/marmotta) 
(IP address may be different, use `docker inspect CONTAINER_ID` for details).
