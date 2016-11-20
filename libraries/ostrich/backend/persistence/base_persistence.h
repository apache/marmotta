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
#ifndef MARMOTTA_BASE_PERSISTENCE_H
#define MARMOTTA_BASE_PERSISTENCE_H

#include <string>

#include "model/rdf_model.h"
#include <model/rdf_namespaces.h>
#include "service/sail.pb.h"
#include "util/iterator.h"

namespace marmotta {
namespace persistence {


// Length of key in bytes per field S, P, O and C.
constexpr int kKeyLength = 16;

enum IndexTypes {
    SPOC, CSPO, OPSC, PCOS
};

enum BoundTypes {
    LOWER, UPPER
};

class Key {
 public:
    // Create key for the given string quadruple using a Murmer3 hash for each
    // component.
    Key(const std::string* s, const std::string* p,
        const std::string* o, const std::string* c);

    // Create key for the given statement. Some fields may be unset.
    Key(const rdf::proto::Statement& stmt);

    // Create the key for the given index type. Returns a newly allocated char
    // array that needs to be deleted by the caller using free().
    char* Create(IndexTypes type, BoundTypes bound = LOWER) const;

 private:
    bool sEnabled, pEnabled, oEnabled, cEnabled;
    char sHash[kKeyLength], pHash[kKeyLength], oHash[kKeyLength], cHash[kKeyLength];
};


/**
 * A pattern for querying the index of a key-value store supporting range queries
 * like LevelDB or RocksDB.
 */
class Pattern {
 public:
    Pattern(const rdf::proto::Statement& pattern);

    /**
     * Return the lower key for querying the index (range [MinKey,MaxKey) ).
     */
    char* MinKey() const;

    /**
     * Return the upper key for querying the index (range [MinKey,MaxKey) ).
     */
    char* MaxKey() const;

    IndexTypes Type() const {
        return type_;
    }

    Pattern& Type(IndexTypes t) {
        type_ = t;
        return *this;
    }

    // Returns true in case this query pattern cannot be answered by the index alone.
    bool NeedsFilter() const {
        return needsFilter_;
    }

 private:
    Key key_;
    IndexTypes type_;
    bool needsFilter_;
};

class Persistence {
 public:
    typedef util::CloseableIterator<rdf::proto::Statement> StatementIterator;
    typedef util::CloseableIterator<rdf::proto::Namespace> NamespaceIterator;
    typedef util::CloseableIterator<service::proto::UpdateRequest> UpdateIterator;

    typedef std::function<bool(const rdf::proto::Statement&)> StatementHandler;
    typedef std::function<bool(const rdf::proto::Namespace&)> NamespaceHandler;


    /**
     * Add the namespaces in the iterator to the database.
     */
    virtual service::proto::UpdateResponse AddNamespaces(
            NamespaceIterator& it) = 0;

    /**
     * Add the statements in the iterator to the database.
     */
    virtual service::proto::UpdateResponse AddStatements(
            StatementIterator& it) = 0;

    /**
     * Get all statements matching the pattern (which may have some fields
     * unset to indicate wildcards). Call the callback function for each
     * result.
     */
    virtual void GetStatements(
            const rdf::proto::Statement& pattern, StatementHandler callback) = 0;

    /**
     * Get all statements matching the pattern (which may have some fields
     * unset to indicate wildcards). Call the callback function for each
     * result.
     */
    virtual std::unique_ptr<StatementIterator> GetStatements(
            const rdf::proto::Statement& pattern) = 0;

    /**
     * Get all namespaces matching the pattern (which may have some of all
     * fields unset to indicate wildcards). Call the callback function for
     * each result.
     */
    virtual void GetNamespaces(
            const rdf::proto::Namespace &pattern, NamespaceHandler callback) = 0;

    /**
     * Get all namespaces matching the pattern (which may have some of all
     * fields unset to indicate wildcards). Call the callback function for
     * each result.
     */
    virtual std::unique_ptr<NamespaceIterator> GetNamespaces(
            const rdf::proto::Namespace &pattern) = 0;

    /**
     * Remove all statements matching the pattern (which may have some fields
     * unset to indicate wildcards).
     */
    virtual service::proto::UpdateResponse RemoveStatements(
            const rdf::proto::Statement& pattern) = 0;

    /**
     * Apply a batch of updates (mixed statement/namespace adds and removes).
     * The updates are collected in LevelDB batches and written atomically to
     * the database when iteration ends.
     */
    virtual service::proto::UpdateResponse Update(
            UpdateIterator& it) = 0;

    /**
     * Return the size of this database.
     */
    virtual int64_t Size() = 0;
};


// Base iterator for wrapping a LevelDB-style database iterators.
template<typename T, typename Iterator>
class DBIterator : public util::CloseableIterator<T> {
 public:

    DBIterator(Iterator *it)
            : it(it) {
        it->SeekToFirst();
    }

    virtual ~DBIterator() override {
        delete it;
    };

    const T& next() override {
        // Parse current position, then iterate to next position for next call.
        proto.ParseFromString(it->value().ToString());
        rdf::DecodeWellknownURI(&proto);
        it->Next();
        return proto;
    };

    const T& current() const override {
        return proto;
    };

    virtual bool hasNext() override {
        return it->Valid();
    }

 protected:
    Iterator* it;
    T proto;
};


// Return true if the statement matches the pattern. Wildcards (empty fields)
// in the pattern are ignored.
bool Matches(const rdf::proto::Statement& pattern,
             const rdf::proto::Statement& stmt);

}  // namespace persistence
}  // namespace marmotta

#endif //MARMOTTA_BASE_PERSISTENCE_H
