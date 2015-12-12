/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Binary to start a persistence server implementing the sail.proto API.
#include <gflags/gflags.h>
#include <glog/logging.h>
#include <sys/stat.h>
#include <signal.h>

#include "leveldb_service.h"

using grpc::Status;
using grpc::Server;
using grpc::ServerBuilder;


DEFINE_string(host, "0.0.0.0", "Address/name of server to access.");
DEFINE_string(port, "10000", "Port of server to access.");
DEFINE_string(db, "/tmp/testdb", "Path to database. Will be created if non-existant.");
DEFINE_int64(cache_size, 100 * 1048576, "Cache size used by the database (in bytes).");

std::unique_ptr<Server> server;

void stopServer(int signal) {
    if (server.get() != nullptr) {
        LOG(INFO) << "Persistence Server shutting down";
        server->Shutdown();
    }
}

int main(int argc, char** argv) {
    // Initialize Google's logging library.
    google::InitGoogleLogging(argv[0]);
    google::ParseCommandLineFlags(&argc, &argv, true);

    mkdir(FLAGS_db.c_str(), 0700);
    marmotta::persistence::LevelDBPersistence persistence(FLAGS_db, FLAGS_cache_size);

    marmotta::service::LevelDBService sailService(&persistence);
    marmotta::service::LevelDBSparqlService sparqlService(&persistence);

    ServerBuilder builder;
    builder.AddListeningPort(FLAGS_host + ":" + FLAGS_port, grpc::InsecureServerCredentials());
    builder.RegisterService(&sailService);
    builder.RegisterService(&sparqlService);

    server = builder.BuildAndStart();
    std::cout << "Persistence Server listening on " << FLAGS_host << ":" << FLAGS_port << std::endl;

    LOG(INFO) << "Persistence Server listening on " << FLAGS_host << ":" << FLAGS_port;

    signal(SIGINT, stopServer);
    signal(SIGTERM, stopServer);

    server->Wait();

    return 0;
}