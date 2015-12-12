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
#include "leveldb_sparql.h"

namespace marmotta {
namespace persistence {
namespace sparql {

using ::marmotta::sparql::StatementIterator;

class WrapProtoStatementIterator : public StatementIterator {

 public:
    WrapProtoStatementIterator(std::unique_ptr<persistence::LevelDBPersistence::StatementIterator> it)
            : it(std::move(it)) { }

    util::CloseableIterator<rdf::Statement> &operator++() override {
        ++(*it);
        parsed = false;
        return *this;
    };

    rdf::Statement &operator*() override {
        if (!parsed) {
            current = std::move(**it);
            parsed = true;
        }
        return current;
    };

    rdf::Statement *operator->() override {
        if (!parsed) {
            current = std::move(**it);
            parsed = true;
        }
        return &current;
    };

    bool hasNext() override {
        return it->hasNext();
    }

 private:
    std::unique_ptr<persistence::LevelDBPersistence::StatementIterator> it;
    rdf::Statement current;
    bool parsed;
};


bool LevelDBTripleSource::HasStatement(
        const rdf::Resource *s, const rdf::URI *p, const rdf::Value *o, const rdf::Resource *c) {
    rdf::proto::Statement pattern;

    if (s != nullptr) {
        *pattern.mutable_subject() = s->getMessage();
    }
    if (p != nullptr) {
        *pattern.mutable_predicate() = p->getMessage();
    }
    if (o != nullptr) {
        *pattern.mutable_object() = o->getMessage();
    }
    if (c != nullptr) {
        *pattern.mutable_context() = c->getMessage();
    }

    bool found = false;
    persistence->GetStatements(pattern, [&found](rdf::proto::Statement) -> bool {
        found = true;
        return false;
    });

    return found;
}

std::unique_ptr<sparql::StatementIterator> LevelDBTripleSource::GetStatements(
        const rdf::Resource *s, const rdf::URI *p, const rdf::Value *o, const rdf::Resource *c) {
    rdf::proto::Statement pattern;

    if (s != nullptr) {
        *pattern.mutable_subject() = s->getMessage();
    }
    if (p != nullptr) {
        *pattern.mutable_predicate() = p->getMessage();
    }
    if (o != nullptr) {
        *pattern.mutable_object() = o->getMessage();
    }
    if (c != nullptr) {
        *pattern.mutable_context() = c->getMessage();
    }

    return std::unique_ptr<sparql::StatementIterator>(
            new WrapProtoStatementIterator(persistence->GetStatements(pattern)));
}

}  // namespace sparql
}  // namespace persistence
}  // namespace marmotta