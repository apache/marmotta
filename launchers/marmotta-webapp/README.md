# Apache Marmotta Webapp Launcher

This folder contains a JavaEE Web Application for launching Marmotta in any Servlet (>=3.0) container.

## WAR

For building the WAR just execute:

    mvn package

and you'll find the WAR file at `target/marmotta.war`.

## Debian

The build also provides supprt for Debian packages, just append the profile to the regular build command:

    mvn package -Pdebian

and you'll find the `.deb` file `target/`.

## Docker

It also comes witth support for creating a Docker images that you can user for developing or testing
Apache Marmotta.

* Build image: `docker build -t marmotta .`
* Run the image: `docker run -p 8080:8080 marmotta`
* Access Marmotta at [localhost:8080/marmotta](http://localhost:8080/marmotta) (IP address may be different, 
  use `docker inspect CONTAINER_ID` for details).

@@TODO@@: push it to asf or docker hub

