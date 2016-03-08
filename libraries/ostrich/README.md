# Apache Marmotta Ostrich Backend

This repository implements an experimental high-performance backend for Apache Marmotta
using LevelDB as storage and gRPC as communication channel between the Java frontend
and the C++ backend. 

If it proves to be useful, the repository will eventually be merged into the main 
development branch of Apache Marmotta

## Dependencies (C++)

To compile the C++ backend, you need to have the following dependencies installed:

  * libraptor (used for parsing/serializing in C++)
  * librasqal (used for server-side SPARQL evaluation)
  * libglog (logging)
  * libgflags (command line arguments)
  * libleveldb (database backend)
  * libgrpc (gRPC runtime)
  * libprotobuf (messaging, data model)

With the exception of libgrpc and libprotobuf, all libraries are available in Linux repositories.
Debian:

    apt-get install libraptor2-dev librasqal3-dev libgoogle-glog-dev libgflags-dev libleveldb-dev
    
The backend uses the new Proto 3 format and the gRPC SDK. These need to be installed separately;
please follow the instructions at [https://github.com/grpc/grpc](https://github.com/grpc/grpc/blob/master/INSTALL).


## Compilation (C++)

The backend uses cmake to compile the modules. Create a new directory `build`, run cmake, and run make:

    cd backend
    mkdir build && cd build
    cmake ..
    make
    cd ..

## Compilation (Java)

The frontend is compiled with Maven and depends on many Apache Marmotta modules to work. Build it with

    mvn clean install
    
## Running C++ Backend

Start the backend from the cmake build directory as follows:

    ./backend/build/persistence/marmotta_persistence -db /path/to/database -port 10000
    
The binary accepts many different options. Please see `--help` for details.

## Running Docker

The C++ backend can be ran in the provided Docker image. You can build it:

    docker build -t apachemarmotta/ostrich .

Or fetch it [from Docker Hub](https://hub.docker.com/r/apachemarmotta/ostrich/):

    docker pull apachemarmotta/ostrich

Then you can run Ostrich as a container:

    docker run -t -d -p 10000:10000 apachemarmotta/ostrich

connecting normally to `localhost:10000`.

## Running Sharding

The repository contains an experimental implementation of a sharding server that proxies and 
distributes requests based on a hash calculation over statements. In heavy load environments,
this is potentially much faster than running a single persistence backend. The setup requires
several persistence backends (shards) and a sharding proxy. To experiment, you can start these
on the same machine as follows:

    ./backend/build/persistence/marmotta_persistence -db /path/to/shard1 -port 10001
    ./backend/build/persistence/marmotta_persistence -db /path/to/shard2 -port 10002
    ./backend/build/sharding/marmotta_sharding --port 10000 --backends localhost:10001,localhost:10002

You can then access the sharding server through Marmotta like the persistence server. Running all instances
on the same host is only useful for testing. In production environments, you would of course run all three
(or more) instances on different hosts. Note that the number and order of backends should not change once
data has been imported, because otherwise the hashing algorithm will do the wrong thing.

## Running Apache Marmotta 

There is a `ostrich` Maven profile to run the webapp launcher:

    cd launchers/marmotta-webapp
    mvn tomcat7:run -Postrich
    
Afterwards, point your browser to [localhost:8080](http://localhost:8080/).

## Command Line Client

A C++ command line client is available for very fast bulk imports and simple queries. To import
a large turtle file, run:

    ./client/marmotta_client --format=turtle import file.ttl

The client connects by default to `localhost:10000` (change with `--host` and `--port` flags).

