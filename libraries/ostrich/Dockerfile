# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Dockerfile for Apache Marmotta Ostrich Triplestore

FROM debian:stretch
MAINTAINER Sergio Fernández <wikier@apache.org>

ADD . /src
WORKDIR /src

# configuration
ENV DEBIAN_FRONTEND noninteractive
ENV OSTRICH_PATH /opt/ostrich
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
        libc++-dev \
        python-all-dev \
        python-virtualenv \
    && apt-get install -y \
        libraptor2-dev \
        librasqal3-dev \
        libgoogle-glog-dev \
        libgflags-dev \
        libleveldb-dev \
        librocksdb-dev \
        zlib1g-dev \
        libgflags-dev \
        libgtest-dev \
        libboost-all-dev \
        libgoogle-perftools-dev

RUN git clone https://github.com/grpc/grpc.git \
    && cd grpc \
    && git submodule update --init --recursive \
    && make \
    && cd third_party/protobuf \
    && make install \
    && cd ../.. \
    && make install \
    && cd

# build
RUN cd backend \
    && mkdir build \
    && cd build \
    && cmake -D CMAKE_BUILD_TYPE=Release .. \
    && make \
    && cd
RUN mkdir -p ${DB_PATH}
RUN mkdir ${OSTRICH_PATH}
RUN cp -r /src/backend/build/* ${OSTRICH_PATH}/

# clean
RUN apt-get -y clean -qq \
    && apt-get -y autoclean -qq \
    && apt-get -y autoremove -qq \
    && rm -rf /var/lib/apt/lists/* \
    && rm -rf /src

WORKDIR ${OSTRICH_PATH}
EXPOSE 10000

CMD ${OSTRICH_PATH}/persistence/marmotta_persistence -db ${DB_PATH} -port ${DB_PORT}

