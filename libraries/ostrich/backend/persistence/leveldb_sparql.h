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
#ifndef MARMOTTA_SPARQL_H
#define MARMOTTA_SPARQL_H

#include "sparql/rasqal_adapter.h"
#include "leveldb_persistence.h"

namespace marmotta {
namespace persistence {
namespace sparql {

/**
 * A SPARQL triple source using a LevelDBPersistence to access data.
 */
class LevelDBTripleSource : public ::marmotta::sparql::TripleSource {
 public:

    LevelDBTripleSource(LevelDBPersistence *persistence) : persistence(persistence) { }


    bool HasStatement(const rdf::Resource *s, const rdf::URI *p, const rdf::Value *o, const rdf::Resource *c) override;

    std::unique_ptr<::marmotta::sparql::StatementIterator>
            GetStatements(const rdf::Resource *s, const rdf::URI *p, const rdf::Value *o, const rdf::Resource *c) override;

 private:
    // A pointer to the persistence instance wrapped by this triple source.
    LevelDBPersistence* persistence;
};


}  // namespace sparql
}  // namespace persistence
}  // namespace marmotta

#endif //MARMOTTA_SPARQL_H
