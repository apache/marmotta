# Dockerfile for Apache Marmotta Ostrich Triplestore

FROM debian:jessie
MAINTAINER Sergio Fernández <wikier@apache.org>

ADD . /src
WORKDIR /src

# configuration
ENV DEBIAN_FRONTEND noninteractive
ENV DB_PATH /data/ostrich
ENV DB_PORT 10000

# base environment
RUN apt-get update -qq \
    && apt-get install -qq -y \
        locales \
        apt-utils \
        git \
    && localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8
ENV LANG en_US.utf8

RUN apt-get update -qq \
    && apt-get install -y \
       build-essential \
        autoconf \
        libtool \
        cmake \
        clang \
        libc++-dev \
        python-all-dev \
        python-virtualenv \
    && apt-get install -y \
        libraptor2-dev \
        librasqal3-dev \
        libgoogle-glog-dev \
        libgflags-dev \
        libleveldb-dev \
        zlib1g-dev \
        libgflags-dev \
        libgtest-dev \
        libboost-all-dev \
        libgoogle-perftools-dev

RUN git clone https://github.com/grpc/grpc.git \
    && cd grpc \
    && git submodule update --init --recursive \
    && make \
    && make install \
    && cd third_party/protobuf \
    && ./configure \
    && make \
    && make install \
    && cd ../../..

# build
RUN mkdir build \
    && cd build \
    && cmake cmake -D CMAKE_BUILD_TYPE=Release .... \
    && make \
    && cd ..

# clean
RUN apt-get -y clean -qq \
    && apt-get -y autoclean -qq \
    && apt-get -y autoremove -qq \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p ${DB_PATH}

EXPOSE 10000

CMD ["/src/build/service/marmotta_persistence", "-db", "${DB_PATH}", "-port" "${DB_PORT}"]

