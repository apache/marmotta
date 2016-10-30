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

#include <rocksdb/db.h>
#include <rocksdb/cache.h>
#include <rocksdb/comparator.h>

#include "persistence/base_persistence.h"

namespace marmotta {
namespace persistence {

/**
 * A custom comparator treating the bytes in the key as unsigned char.
 */
class KeyComparator : public rocksdb::Comparator {
 public:
    int Compare(const rocksdb::Slice& a, const rocksdb::Slice& b) const override ;

    const char* Name() const override { return "KeyComparator"; }
    void FindShortestSeparator(std::string*, const rocksdb::Slice&) const override { }
    void FindShortSuccessor(std::string*) const override { }
};


// Symbolic handle indices,
enum Handles {
    ISPOC = 0, ICSPO = 1, IOPSC = 2, IPCOS = 3, NSPREFIX = 4, NSURI = 5, META = 6
};

/**
 * Persistence implementation based on the RocksDB high performance database.
 */
class RocksDBPersistence : public Persistence {
 public:
    /**
     * Initialise a new LevelDB database using the given path and cache size (bytes).
     */
    RocksDBPersistence(const std::string& path, int64_t cacheSize);

    ~RocksDBPersistence();

    /**
     * Add the namespaces in the iterator to the database.
     */
    service::proto::UpdateResponse AddNamespaces(NamespaceIterator& it) override;

    /**
     * Add the statements in the iterator to the database.
     */
    service::proto::UpdateResponse  AddStatements(StatementIterator& it) override;

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
    service::proto::UpdateResponse  RemoveStatements(
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
    KeyComparator comparator_;
    std::unique_ptr<rocksdb::DB> database_;

    // Column Families for the different index access types.
    std::vector<rocksdb::ColumnFamilyHandle*> handles_;

    /**
     * Add the namespace to the given database batch operations.
     */
    void AddNamespace(const rdf::proto::Namespace& ns, rocksdb::WriteBatch& batch);

    /**
     * Add the namespace to the given database batch operations.
     */
    void RemoveNamespace(const rdf::proto::Namespace& ns, rocksdb::WriteBatch& batch);

    /**
     * Add the statement to the given database batch operations.
     */
    void AddStatement(const rdf::proto::Statement& stmt, rocksdb::WriteBatch& batch);


    /**
     * Remove all statements matching the pattern (which may have some fields
     * unset to indicate wildcards) from the given database batch operations.
     */
    int64_t RemoveStatements(const rdf::proto::Statement& pattern, rocksdb::WriteBatch& batch);
};



}  // namespace persistence
}  // namespace marmotta

#endif //MARMOTTA_PERSISTENCE_H
