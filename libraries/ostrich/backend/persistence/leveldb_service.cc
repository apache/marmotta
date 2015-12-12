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
#include "leveldb_service.h"
#include "leveldb_sparql.h"

#include <unordered_set>
#include <model/rdf_operators.h>
#include <util/iterator.h>

using grpc::Status;
using grpc::StatusCode;
using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::ServerReader;
using grpc::ServerWriter;
using google::protobuf::Int64Value;
using google::protobuf::Message;
using google::protobuf::Empty;
using marmotta::rdf::proto::Statement;
using marmotta::rdf::proto::Namespace;
using marmotta::rdf::proto::Resource;
using marmotta::service::proto::ContextRequest;
using marmotta::persistence::sparql::LevelDBTripleSource;
using marmotta::sparql::SparqlService;
using marmotta::sparql::TripleSource;

namespace marmotta {
namespace service {

// A STL iterator wrapper around a client reader.
template <class Proto>
class ReaderIterator : public util::CloseableIterator<Proto> {
 public:

    ReaderIterator(grpc::ServerReader<Proto>* r) : reader(r), finished(false) {
        // Immediately move to first element.
        operator++();
    }

    util::CloseableIterator<Proto>& operator++() override {
        if (!finished) {
            finished = !reader->Read(&buffer);
        }
        return *this;
    }

    Proto& operator*() override {
        return buffer;
    }

    Proto* operator->() override {
        return &buffer;
    }

    bool hasNext() override {
        return !finished;
    }

 private:
    grpc::ServerReader<Proto>* reader;
    Proto buffer;
    bool finished;
};

typedef ReaderIterator<rdf::proto::Statement> StatementIterator;
typedef ReaderIterator<rdf::proto::Namespace> NamespaceIterator;
typedef ReaderIterator<service::proto::UpdateRequest> UpdateIterator;


Status LevelDBService::AddNamespaces(
        ServerContext* context, ServerReader<Namespace>* reader, Int64Value* result) {

    auto it = NamespaceIterator(reader);
    int64_t count = persistence->AddNamespaces(it);
    result->set_value(count);

    return Status::OK;
}

grpc::Status LevelDBService::GetNamespace(
        ServerContext *context, const rdf::proto::Namespace *pattern, Namespace *result) {

    Status status(StatusCode::NOT_FOUND, "Namespace not found");
    persistence->GetNamespaces(*pattern, [&result, &status](const Namespace &r) -> bool {
        *result = r;
        status = Status::OK;
        return true;
    });

    return status;
}

grpc::Status LevelDBService::GetNamespaces(
        ServerContext *context, const Empty *ignored, ServerWriter<Namespace> *result) {

    Namespace pattern; // empty pattern
    persistence->GetNamespaces(pattern, [&result](const Namespace &r) -> bool {
        return result->Write(r);
    });

    return Status::OK;
}


Status LevelDBService::AddStatements(
        ServerContext* context, ServerReader<Statement>* reader, Int64Value* result) {

    auto it = StatementIterator(reader);
    int64_t count = persistence->AddStatements(it);
    result->set_value(count);

    return Status::OK;
}


Status LevelDBService::GetStatements(
        ServerContext* context, const Statement* pattern, ServerWriter<Statement>* result) {

    persistence->GetStatements(*pattern, [&result](const Statement& stmt) -> bool {
        return result->Write(stmt);
    });

    return Status::OK;
}

Status LevelDBService::RemoveStatements(
        ServerContext* context, const Statement* pattern, Int64Value* result) {

    int64_t count = persistence->RemoveStatements(*pattern);
    result->set_value(count);

    return Status::OK;
}

Status LevelDBService::Clear(
        ServerContext* context, const ContextRequest* contexts, Int64Value* result) {


    int64_t count = 0;

    Statement pattern;
    if (contexts->context_size() > 0) {
        for (const Resource &r : contexts->context()) {
            pattern.mutable_context()->CopyFrom(r);
            count += persistence->RemoveStatements(pattern);
        }
    } else {
        count += persistence->RemoveStatements(pattern);
    }
    result->set_value(count);

    return Status::OK;
}

Status LevelDBService::Size(
        ServerContext* context, const ContextRequest* contexts, Int64Value* result) {

    int64_t count = 0;

    if (contexts->context_size() > 0) {
        Statement pattern;
        for (const Resource &r : contexts->context()) {
            pattern.mutable_context()->CopyFrom(r);

            persistence->GetStatements(pattern, [&count](const Statement& stmt) -> bool {
                count++;
                return true;
            });
        }
    } else {
        count = persistence->Size();
    }
    result->set_value(count);

    return Status::OK;

}


grpc::Status LevelDBService::GetContexts(
        ServerContext *context, const Empty *ignored, ServerWriter<Resource> *result) {
    // Currently we need to iterate over all statements and collect the results.
    Statement pattern;
    std::unordered_set<Resource> contexts;

    persistence->GetStatements(pattern, [&contexts](const Statement& stmt) -> bool {
        if (stmt.has_context()) {
            contexts.insert(stmt.context());
        }
        return true;
    });

    for (auto c : contexts) {
        result->Write(c);
    }
    return Status::OK;
}

grpc::Status LevelDBService::Update(grpc::ServerContext *context,
                                    grpc::ServerReader<service::proto::UpdateRequest> *reader,
                                    service::proto::UpdateResponse *result) {

    auto it = UpdateIterator(reader);
    persistence::UpdateStatistics stats = persistence->Update(it);

    result->set_added_namespaces(stats.added_ns);
    result->set_removed_namespaces(stats.removed_ns);
    result->set_added_statements(stats.added_stmts);
    result->set_removed_statements(stats.removed_stmts);

    return Status::OK;
}


grpc::Status LevelDBSparqlService::TupleQuery(
        grpc::ServerContext* context, const spq::SparqlRequest* query,
        grpc::ServerWriter<spq::SparqlResponse>* result) {

    SparqlService svc(
        std::unique_ptr<TripleSource>(
                new LevelDBTripleSource(persistence)));

    svc.TupleQuery(query->query(), [&result](const SparqlService::RowType& row) {
        spq::SparqlResponse response;
        for (auto it = row.cbegin(); it != row.cend(); it++) {
            auto b = response.add_binding();
            b->set_variable(it->first);
            *b->mutable_value() = it->second.getMessage();
        }
        result->Write(response);
        return true;
    });

    return Status::OK;
}

}  // namespace service
}  // namespace marmotta