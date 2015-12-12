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
#include "service/sail.pb.h"
#include "util/iterator.h"

namespace marmotta {
namespace persistence {

/**
 * A custom comparator treating the bytes in the key as unsigned char.
 */
class KeyComparator : public leveldb::Comparator {
 public:
    int Compare(const leveldb::Slice& a, const leveldb::Slice& b) const;

    const char* Name() const { return "KeyComparator"; }
    void FindShortestSeparator(std::string*, const leveldb::Slice&) const { }
    void FindShortSuccessor(std::string*) const { }
};


// Statistical data about updates.
struct UpdateStatistics {
    UpdateStatistics()
            : added_stmts(0), removed_stmts(0), added_ns(0), removed_ns(0) {}

    int64_t added_stmts, removed_stmts, added_ns, removed_ns;
};

/**
 * Persistence implementation based on the LevelDB high performance database.
 */
class LevelDBPersistence {
 public:
    typedef util::CloseableIterator<rdf::proto::Statement> StatementIterator;
    typedef util::CloseableIterator<rdf::proto::Namespace> NamespaceIterator;
    typedef util::CloseableIterator<service::proto::UpdateRequest> UpdateIterator;

    typedef std::function<bool(const rdf::proto::Statement&)> StatementHandler;
    typedef std::function<bool(const rdf::proto::Namespace&)> NamespaceHandler;


    /**
     * Initialise a new LevelDB database using the given path and cache size (bytes).
     */
    LevelDBPersistence(const std::string& path, int64_t cacheSize);

    /**
     * Add the namespaces in the iterator to the database.
     */
    int64_t AddNamespaces(NamespaceIterator& it);

    /**
     * Add the statements in the iterator to the database.
     */
    int64_t AddStatements(StatementIterator& it);

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
    int64_t RemoveStatements(const rdf::proto::Statement& pattern);

    /**
     * Apply a batch of updates (mixed statement/namespace adds and removes).
     * The updates are collected in LevelDB batches and written atomically to
     * the database when iteration ends.
     */
    UpdateStatistics Update(UpdateIterator& it);

    /**
     * Return the size of this database.
     */
    int64_t Size();
 private:

    std::unique_ptr<KeyComparator> comparator;
    std::unique_ptr<leveldb::Cache> cache;
    std::unique_ptr<leveldb::Options> options;

    // We currently support efficient lookups by subject, context and object.
    std::unique_ptr<leveldb::DB>
            // Statement databases, indexed for query performance
            db_spoc, db_cspo, db_opsc, db_pcos,
            // Namespace databases
            db_ns_prefix, db_ns_url,
            // Triple store metadata.
            db_meta;

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
