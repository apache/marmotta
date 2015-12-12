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
#include <cstdlib>
#include <thread>
#include <unordered_set>
#include <glog/logging.h>

#include <grpc++/channel.h>
#include <grpc++/client_context.h>
#include <grpc++/create_channel.h>
#include <grpc++/security/credentials.h>
#include <grpc++/support/sync_stream.h>

#include "sharding/sharding.h"
#include "model/rdf_model.h"
#include "model/rdf_operators.h"

using grpc::Channel;
using grpc::ClientContext;
using grpc::ClientReader;
using grpc::ClientReaderWriter;
using grpc::ClientWriter;
using grpc::Status;
using grpc::ServerContext;
using grpc::ServerReader;
using grpc::ServerWriter;
using marmotta::rdf::proto::Namespace;
using marmotta::rdf::proto::Resource;
using marmotta::rdf::proto::Statement;
using marmotta::service::proto::ContextRequest;
using marmotta::service::proto::SailService;
using marmotta::service::proto::UpdateRequest;
using marmotta::service::proto::UpdateResponse;
using google::protobuf::Empty;
using google::protobuf::Int64Value;

namespace marmotta {
namespace sharding {

// A templated fanout function, forwarding the same request to all backends and collecting
// Int64Value responses by summing them up.
template<typename Request,
        Status (SailService::Stub::*ClientMethod)(ClientContext*, const Request&, Int64Value*)>
Status Fanout(const Request& request, ShardingService::ChannelList &backends, Int64Value *result) {
    auto start = std::chrono::steady_clock::now();
    std::vector<std::thread> threads;
    std::vector<Status> statuses(backends.size());

    int64_t r = 0;
    for (int i=0; i<backends.size(); i++) {
        threads.push_back(std::thread([i, &backends, &statuses, &request, &r]() {
            ClientContext localctx;
            Int64Value response;
            auto stub = svc::SailService::NewStub(backends[i]);
            statuses[i] = ((*stub).*ClientMethod)(&localctx, request, &response);
            r += response.value();
        }));
    }

    // need to wait until all are completed now.
    for (auto& t : threads) {
        t.join();
    }

    result->set_value(r);

    for (auto s : statuses) {
        if (!s.ok())
            return s;
    }

    DLOG(INFO) << "Fanout operation done (time="
               << std::chrono::duration <double, std::milli> (
            std::chrono::steady_clock::now() - start).count()
               << "ms).";

    return Status::OK;
};

ShardingService::ShardingService(std::vector<std::string> backends) : backends(backends) {
    for (const std::string& server : backends) {
        LOG(INFO) << "Establishing channel to " << server;
        channels.push_back(grpc::CreateChannel(server, grpc::InsecureChannelCredentials()));
    }
}

grpc::Status ShardingService::AddNamespaces(
        ServerContext *context, ServerReader<Namespace> *reader, Int64Value *result) {

    std::vector<ClientContext> contexts(backends.size());
    std::vector<Int64Value> stats(backends.size());

    StubList stubs;
    WriterList <Namespace> writers;

    for (int i=0; i<backends.size(); i++) {
        stubs.push_back(makeStub(i));
        writers.push_back(stubs.back()->AddNamespaces(&contexts[i], &stats[i]));
    }

    // Iterate over all namespaces and schedule a write task.
    Namespace ns;
    while (reader->Read(&ns)) {
        DLOG(INFO) << "Adding namespace " << ns.DebugString();
        for (auto& w : writers) {
            w->Write(ns);
        }
    }

    for (auto& w : writers) {
        w->WritesDone();
        w->Finish();
    }

    result->set_value(stats[0].value());

    return Status::OK;
}


Status ShardingService::GetNamespace(
        ServerContext *context, const Namespace *pattern, Namespace *result) {
    int bucket = rand() % backends.size();

    auto stub = makeStub(bucket);

    ClientContext ctx;
    return stub->GetNamespace(&ctx, *pattern, result);
}

Status ShardingService::GetNamespaces(
        ServerContext *context, const Empty *ignored, ServerWriter<Namespace> *result) {
    int bucket = rand() % backends.size();

    auto stub = makeStub(bucket);

    ClientContext ctx;
    auto reader = stub->GetNamespaces(&ctx, *ignored);

    Namespace ns;
    while (reader->Read(&ns)) {
        result->Write(ns);
    }
    return reader->Finish();
}

Status ShardingService::AddStatements(
        ServerContext *context, ServerReader<Statement> *reader, Int64Value *result) {
    std::vector<ClientContext> contexts(backends.size());
    std::vector<Int64Value> responses(backends.size());

    StubList stubs;
    WriterList<Statement> writers;
    for (int i=0; i<backends.size(); i++) {
        stubs.push_back(makeStub(i));
        writers.push_back(Writer<Statement>(
                stubs.back()->AddStatements(&contexts[i], &responses[i])));
    }

    std::hash<Statement> stmt_hash;

    Statement stmt;
    while (reader->Read(&stmt)) {
            size_t bucket = stmt_hash(stmt) % backends.size();

            DLOG(INFO) << "Shard " << bucket << ": Adding statement " << stmt.DebugString();
            writers[bucket]->Write(stmt);
    }
    for (auto& w : writers) {
        w->WritesDone();
        w->Finish();
    }

    for (auto& r : responses) {
        result->set_value(result->value() + r.value());
    }

    return Status::OK;
}

Status ShardingService::GetStatements(
        ServerContext *context, const Statement *pattern, ServerWriter<Statement> *result) {
    auto start = std::chrono::steady_clock::now();
    DLOG(INFO) << "Get statements matching pattern " << pattern->DebugString();

    std::vector<std::thread> threads;
    std::mutex mutex;

    for (int i=0; i<backends.size(); i++) {
        threads.push_back(std::thread([i, this, &mutex, result, pattern]() {
            DLOG(INFO) << "Shard " << i << ": Getting statements.";
            ClientContext localctx;
            auto stub = makeStub(i);
            auto reader = stub->GetStatements(&localctx, *pattern);

            int64_t count = 0;
            Statement stmt;
            bool run = true;
            while (run && reader->Read(&stmt)) {
                std::lock_guard<std::mutex> guard(mutex);
                run = result->Write(stmt);
                count++;
            }
            DLOG(INFO) << "Shard " << i << ": Getting statements finished (" << count << " results)";
        }));
    }

    for (auto& t : threads) {
        t.join();
    }

    DLOG(INFO) << "Get statements done (time="
               << std::chrono::duration <double, std::milli> (
            std::chrono::steady_clock::now() - start).count()
               << "ms).";

    return Status::OK;
}

Status ShardingService::RemoveStatements(
        ServerContext *context, const Statement *pattern, Int64Value *result) {
    DLOG(INFO) << "Fanout: Remove statements matching pattern " << pattern->DebugString();

    return Fanout<Statement, &SailService::Stub::RemoveStatements>(*pattern, channels, result);
}


Status ShardingService::Update(
        ServerContext *context, ServerReader<UpdateRequest> *reader, UpdateResponse *result) {
    std::vector<ClientContext> contexts(backends.size());
    std::vector<UpdateResponse> responses(backends.size());

    StubList stubs;
    WriterList <UpdateRequest> writers;

    for (int i=0; i<backends.size(); i++) {
        stubs.push_back(makeStub(i));
        writers.push_back(stubs.back()->Update(&contexts[i], &responses[i]));
    }

    std::hash<Statement> stmt_hash;
    std::hash<Namespace> ns_hash;

    UpdateRequest req;
    std::string buf;
    while (reader->Read(&req)) {
        if (req.has_stmt_added()) {
            size_t bucket = stmt_hash(req.stmt_added()) % backends.size();

            DLOG(INFO) << "Shard " << bucket << ": Add statement request " << req.DebugString();
            writers[bucket]->Write(req);
        } else {
            DLOG(INFO) << "Fanout update request " << req.DebugString();
            for (auto& w : writers) {
                w->Write(req);
            }
        }
    }
    for (auto& w : writers) {
        w->WritesDone();
        w->Finish();
    }

    for (auto& r : responses) {
        result->set_added_namespaces(result->added_namespaces() + r.added_namespaces());
        result->set_removed_namespaces(result->removed_namespaces() + r.removed_namespaces());
        result->set_added_statements(result->added_statements() + r.added_statements());
        result->set_removed_statements(result->removed_statements() + r.removed_statements());
    }


    return Status::OK;
}

Status ShardingService::Clear(
        ServerContext *context, const ContextRequest *contexts, Int64Value *result) {
    DLOG(INFO) << "Fanout: Clear contexts matching pattern " << contexts->DebugString();

    return Fanout<ContextRequest, &SailService::Stub::Clear>(*contexts, channels, result);
}

Status ShardingService::Size(
        ServerContext *context, const ContextRequest *contexts, Int64Value *result) {
    DLOG(INFO) << "Fanout: Computing size of contexts matching pattern " << contexts->DebugString();

    return Fanout<ContextRequest, &SailService::Stub::Size>(*contexts, channels, result);
}

std::unique_ptr<SailService::Stub> ShardingService::makeStub(int i) {
    return SailService::NewStub(channels[i]);
}

Status ShardingService::GetContexts(
        ServerContext *context, const Empty *ignored, ServerWriter<Resource> *result) {
    std::unordered_set<Resource> contexts;
    std::vector<std::thread> threads;
    std::mutex mutex;

    for (int i=0; i<backends.size(); i++) {
        threads.push_back(std::thread([i, &mutex, &contexts, this](){
            ClientContext ctx;
            auto stub = makeStub(i);
            auto reader = stub->GetContexts(&ctx, Empty());

            Resource r;
            while (reader->Read(&r)) {
                std::lock_guard<std::mutex> guard(mutex);
                contexts.insert(r);
            }
            reader->Finish();
        }));
    }

    for (auto c : contexts) {
        result->Write(c);
    }
    return Status::OK;
}
}
}