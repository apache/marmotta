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
#ifndef MARMOTTA_PERSISTENCE_H
#define MARMOTTA_PERSISTENCE_H

#include <memory>
#include <string>
#include <functional>

#include <leveldb/db.h>
#include <leveldb/cache.h>
#include <leveldb/comparator.h>

#include "model/rdf_model.h"
#include "persistence/base_persistence.h"
#include "service/sail.pb.h"
#include "util/iterator.h"
#include "util/threadpool.h"

namespace marmotta {
namespace persistence {

/**
 * A custom comparator treating the bytes in the key as unsigned char.
 */
class KeyComparator : public leveldb::Comparator {
 public:
    int Compare(const leveldb::Slice& a, const leveldb::Slice& b) const override ;

    const char* Name() const override { return "KeyComparator"; }
    void FindShortestSeparator(std::string*, const leveldb::Slice&) const override { }
    void FindShortSuccessor(std::string*) const override { }
};

/**
 * Persistence implementation based on the LevelDB high performance database.
 */
class LevelDBPersistence : public Persistence {
 public:
    /**
     * Initialise a new LevelDB database using the given path and cache size (bytes).
     */
    LevelDBPersistence(const std::string& path, int64_t cacheSize);

    /**
      * Add the namespaces in the iterator to the database.
      */
    service::proto::UpdateResponse AddNamespaces(NamespaceIterator& it) override;

    /**
     * Add the statements in the iterator to the database.
     */
    service::proto::UpdateResponse AddStatements(StatementIterator& it) override;

    /**
     * Get all statements matching the pattern (which may have some fields
     * unset to indicate wildcards). Call the callback function for each
     * result.
     */
    void GetStatements(const rdf::proto::Statement& pattern,
                       StatementHandler callback);

    /**
     * Get all statements matching the pattern (which may have some fields
     * unset to indicate wildcards). Call the callback function for each
     * result.
     */
    std::unique_ptr<StatementIterator>
    GetStatements(const rdf::proto::Statement& pattern);

    /**
     * Get all namespaces matching the pattern (which may have some of all
     * fields unset to indicate wildcards). Call the callback function for
     * each result.
     */
    void GetNamespaces(const rdf::proto::Namespace &pattern,
                       NamespaceHandler callback);

    /**
     * Get all namespaces matching the pattern (which may have some of all
     * fields unset to indicate wildcards). Call the callback function for
     * each result.
     */
    std::unique_ptr<NamespaceIterator>
    GetNamespaces(const rdf::proto::Namespace &pattern);

    /**
     * Remove all statements matching the pattern (which may have some fields
     * unset to indicate wildcards).
     */
    service::proto::UpdateResponse RemoveStatements(
            const rdf::proto::Statement& pattern) override;

    /**
     * Apply a batch of updates (mixed statement/namespace adds and removes).
     * The updates are collected in LevelDB batches and written atomically to
     * the database when iteration ends.
     */
    service::proto::UpdateResponse Update(
            UpdateIterator& it) override;

    /**
     * Return the size of this database.
     */
    int64_t Size() override;
 private:
    ctpl::thread_pool workers;

    std::unique_ptr<KeyComparator> comparator;
    std::shared_ptr<leveldb::Cache> cache;
    std::unique_ptr<leveldb::Options> options;

    // We currently support efficient lookups by subject, context and object.
    std::unique_ptr<leveldb::DB>
            // Statement databases, indexed for query performance
            db_spoc, db_cspo, db_opsc, db_pcos,
            // Namespace databases
            db_ns_prefix, db_ns_url,
            // Triple store metadata.
            db_meta;

    // Keep track of namespaces in memory for prefix compression.
    rdf::NsMap namespaces;

    /**
     * Add the namespace to the given database batch operations.
     */
    void AddNamespace(const rdf::proto::Namespace& ns,
                      leveldb::WriteBatch& ns_prefix, leveldb::WriteBatch& ns_url);

    /**
     * Add the namespace to the given database batch operations.
     */
    void RemoveNamespace(const rdf::proto::Namespace& ns,
                         leveldb::WriteBatch& ns_prefix, leveldb::WriteBatch& ns_url);

    /**
     * Add the statement to the given database batch operations.
     */
    void AddStatement(const rdf::proto::Statement& stmt,
                      leveldb::WriteBatch& spoc, leveldb::WriteBatch& cspo,
                      leveldb::WriteBatch& opsc, leveldb::WriteBatch&pcos);


    /**
     * Remove all statements matching the pattern (which may have some fields
     * unset to indicate wildcards) from the given database batch operations.
     */
    int64_t RemoveStatements(const rdf::proto::Statement& pattern,
                             leveldb::WriteBatch& spoc, leveldb::WriteBatch& cspo,
                             leveldb::WriteBatch& opsc, leveldb::WriteBatch&pcos);


};



}  // namespace persistence
}  // namespace marmotta

#endif //MARMOTTA_PERSISTENCE_H
