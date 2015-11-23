# Apache Marmotta Webapp Launcher

This folder contains a JavaEE Web Application for launching Marmotta in any Servlet (>=3.0) container.

Further details at the `src/main/doc/README.txt` file.

## WAR

For building the WAR just execute:

    mvn package

and you'll find the WAR file at `target/marmotta.war`.

## Debian

The build also provides supprt for Debian packages, just append the profile to the regular build command:

    mvn package -Pdebian

and you can find the `.deb` file under `target/`.

## Docker

It also comes with support for creating a Docker images that you can user for developing or testing:

* Build the launcher: `mvn package`
* Build image: `docker build -t marmotta .`
* Run the container: `docker run -p 8080:8080 marmotta`
* Access Marmotta at [localhost:8080/marmotta](http://localhost:8080/marmotta) (IP address may be different, 
  see point bellow).

If you want to further work with the container, here some basic instructions:

* List running containers: `docker ps` (appending `--filter "ancestor=marmotta` shows only the `marmotta` images, `-a` lists all)
* Get details about the container: `docker inspect CONTAINER_ID`
* Get basic statistics about the container: `docker stats [CONTAINERID]`
* Commit the container changes to a new image: `docker commit [CONTAINERID] my-marmotta`
* Stop the container: `docker stop [CONTAINERID]`
* Start again the container: `docker start [CONTAINERID]`
* Remove a container: `docker rm CONTAINER_ID`
* Remove all containers `docker rm $(docker ps -a -q)`
* List all images: `docker images`
* Remove an image: `docker rmi IMAGE_ID`
* Remove all images: `docker rmi $(docker images -q)`

For further instructions, please take a look to the [Docker User Guide](https://docs.docker.com/userguide/).

@@TODO@@: push it to asf or docker hub

