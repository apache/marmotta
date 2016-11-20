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
#define KEY_LENGTH 16

#include <chrono>
#include <memory>

#include <gflags/gflags.h>
#include <glog/logging.h>
#include <leveldb/filter_policy.h>
#include <leveldb/write_batch.h>
#include <google/protobuf/wrappers.pb.h>
#include <thread>

#include "leveldb_persistence.h"
#include "model/rdf_operators.h"
#include "model/rdf_namespaces.h"

#define CHECK_STATUS(s) CHECK(s.ok()) << "Writing to database failed: " << s.ToString()

DEFINE_int64(write_batch_size, 1000000,
             "Maximum number of statements to write in a single batch to the database");

using leveldb::WriteBatch;
using leveldb::Slice;
using marmotta::rdf::proto::Statement;
using marmotta::rdf::proto::Namespace;
using marmotta::rdf::proto::Resource;

namespace marmotta {
namespace persistence {
namespace {

template<typename T>
using LevelDBIterator = DBIterator<T, leveldb::Iterator>;


// Iterator wrapping a LevelDB Statement iterator over a given key range.
class StatementRangeIterator : public LevelDBIterator<Statement> {
 public:

    StatementRangeIterator(leveldb::Iterator *it, char *loKey, char *hiKey)
            : DBIterator(it), loKey(loKey), hiKey(hiKey) {
        it->Seek(leveldb::Slice(loKey, 4 * KEY_LENGTH));
    }

    ~StatementRangeIterator() override {
        delete[] loKey;
        delete[] hiKey;
    };

    bool hasNext() override {
        return it->Valid() && it->key().compare(leveldb::Slice(hiKey, 4 * KEY_LENGTH)) <= 0;
    }

 private:
    char *loKey;
    char *hiKey;
};

}  // namespace


/**
 * Build database with default options.
 */
leveldb::DB* buildDB(const std::string& path, const std::string& suffix, const leveldb::Options& options) {
    leveldb::DB* db;
    leveldb::Status status = leveldb::DB::Open(options, path + "/" + suffix + ".db", &db);
    CHECK_STATUS(status);
    return db;
}

leveldb::Options* buildOptions(KeyComparator* cmp, leveldb::Cache* cache) {
    leveldb::Options *options = new leveldb::Options();
    options->create_if_missing = true;

    // Custom comparator for our keys.
    options->comparator = cmp;

    // Cache reads in memory.
    options->block_cache = cache;

    // Write buffer size 16MB (fast bulk imports)
    options->write_buffer_size = 16384 * 1024;

    // Set a bloom filter of 10 bits.
    options->filter_policy = leveldb::NewBloomFilterPolicy(10);

    return options;
}

leveldb::Options buildNsOptions() {
    leveldb::Options options;
    options.create_if_missing = true;
    return options;
}

LevelDBPersistence::LevelDBPersistence(const std::string &path, int64_t cacheSize)
        : workers(8), comparator(new KeyComparator())
        , cache(leveldb::NewLRUCache(cacheSize))
        , options(buildOptions(comparator.get(), cache.get()))
        , db_ns_prefix(buildDB(path, "ns_prefix", buildNsOptions()))
        , db_ns_url(buildDB(path, "ns_url", buildNsOptions()))
        , db_meta(buildDB(path, "metadata", buildNsOptions())) {

    // Open databases in separate threads as LevelDB does a lot of computation on open.
    std::vector<std::future<void>> openers;
    openers.push_back(workers.push([&](int id) {
        db_spoc.reset(buildDB(path, "spoc", *options));
    }));
    openers.push_back(workers.push([&](int id) {
        db_cspo.reset(buildDB(path, "cspo", *options));
    }));
    openers.push_back(workers.push([&](int id) {
        db_opsc.reset(buildDB(path, "opsc", *options));
    }));
    openers.push_back(workers.push([&](int id) {
        db_pcos.reset(buildDB(path, "pcos", *options));
    }));


    LOG(INFO) << "Opening LevelDB database ...";
    for (auto& t : openers) {
        t.wait();
    }

    CHECK_NOTNULL(db_spoc.get());
    CHECK_NOTNULL(db_cspo.get());
    CHECK_NOTNULL(db_opsc.get());
    CHECK_NOTNULL(db_pcos.get());

    LOG(INFO) << "LevelDB Database initialised.";
}


service::proto::UpdateResponse LevelDBPersistence::AddNamespaces(NamespaceIterator& it) {
    DLOG(INFO) << "Starting batch namespace import operation.";
    int64_t count = 0;

    leveldb::WriteBatch batch_prefix, batch_url;

    while (it.hasNext()) {
        AddNamespace(it.next(), batch_prefix, batch_url);
        count++;
    }
    CHECK_STATUS(db_ns_prefix->Write(leveldb::WriteOptions(), &batch_prefix));
    CHECK_STATUS(db_ns_url->Write(leveldb::WriteOptions(), &batch_url));

    DLOG(INFO) << "Imported " << count << " namespaces";

    service::proto::UpdateResponse result;
    result.set_added_namespaces(count);
    return result;
}

std::unique_ptr<LevelDBPersistence::NamespaceIterator> LevelDBPersistence::GetNamespaces(
        const rdf::proto::Namespace &pattern) {
    DLOG(INFO) << "Get namespaces matching pattern " << pattern.DebugString();

    Namespace ns;

    leveldb::DB *db = nullptr;
    std::string key, value;
    if (pattern.prefix() != "") {
        key = pattern.prefix();
        db = db_ns_prefix.get();
    } else if(pattern.uri() != "") {
        key = pattern.uri();
        db = db_ns_url.get();
    }
    if (db != nullptr) {
        // Either prefix or uri given, report the correct namespace value.
        leveldb::Status s = db->Get(leveldb::ReadOptions(), key, &value);
        if (s.ok()) {
            ns.ParseFromString(value);
            return std::make_unique<util::SingletonIterator<Namespace>>(std::move(ns));
        } else {
            return std::make_unique<util::EmptyIterator<Namespace>>();
        }
    } else {
        // Pattern was empty, iterate over all namespaces and report them.
        return std::make_unique<LevelDBIterator<Namespace>>(
                db_ns_prefix->NewIterator(leveldb::ReadOptions()));
    }
}


void LevelDBPersistence::GetNamespaces(
        const Namespace &pattern, LevelDBPersistence::NamespaceHandler callback) {
    int64_t count = 0;

    bool cbsuccess = true;
    for(auto it = GetNamespaces(pattern); cbsuccess && it->hasNext();) {
        cbsuccess = callback(it->next());
        count++;
    }

    DLOG(INFO) << "Get namespaces done (count=" << count <<")";
}


service::proto::UpdateResponse LevelDBPersistence::AddStatements(StatementIterator& it) {
    auto start = std::chrono::steady_clock::now();
    LOG(INFO) << "Starting batch statement import operation.";
    int64_t count = 0;

    leveldb::WriteBatch batch_spoc, batch_cspo, batch_opsc, batch_pcos;
    auto writeBatches = [&]{
        std::vector<std::future<void>> writers;
        writers.push_back(workers.push([&](int id) {
            CHECK_STATUS(db_pcos->Write(leveldb::WriteOptions(), &batch_pcos));
            batch_pcos.Clear();
        }));
        writers.push_back(workers.push([&](int id) {
            CHECK_STATUS(db_opsc->Write(leveldb::WriteOptions(), &batch_opsc));
            batch_opsc.Clear();
        }));
        writers.push_back(workers.push([&](int id) {
            CHECK_STATUS(db_cspo->Write(leveldb::WriteOptions(), &batch_cspo));
            batch_cspo.Clear();
        }));
        writers.push_back(workers.push([&](int id) {
            CHECK_STATUS(db_spoc->Write(leveldb::WriteOptions(), &batch_spoc));
            batch_spoc.Clear();
        }));

        for (auto& t : writers) {
            t.wait();
        }
    };

    while (it.hasNext()) {
        AddStatement(it.next(), batch_spoc, batch_cspo, batch_opsc, batch_pcos);
        count++;

        if (count % FLAGS_write_batch_size == 0) {
            writeBatches();
        }
    }

    writeBatches();

    LOG(INFO) << "Imported " << count << " statements (time="
              << std::chrono::duration <double, std::milli> (
                   std::chrono::steady_clock::now() - start).count()
              << "ms).";

    service::proto::UpdateResponse result;
    result.set_added_statements(count);
    return result;
}


std::unique_ptr<LevelDBPersistence::StatementIterator> LevelDBPersistence::GetStatements(
        const rdf::proto::Statement &pattern) {
    DLOG(INFO) << "Get statements matching pattern " << pattern.DebugString();

    Pattern query(pattern);

    leveldb::DB* db;
    switch (query.Type()) {
        case IndexTypes::SPOC:
            db = db_spoc.get();
            DLOG(INFO) << "Query: Using index type SPOC";
            break;
        case IndexTypes::CSPO:
            db = db_cspo.get();
            DLOG(INFO) << "Query: Using index type CSPO";
            break;
        case IndexTypes::OPSC:
            db = db_opsc.get();
            DLOG(INFO) << "Query: Using index type OPSC";
            break;
        case IndexTypes::PCOS:
            db = db_pcos.get();
            DLOG(INFO) << "Query: Using index type PCOS";
            break;
    };

    if (query.NeedsFilter()) {
        DLOG(INFO) << "Retrieving statements with filter.";
        return std::make_unique<util::FilteringIterator<Statement>>(
                new StatementRangeIterator(
                        db->NewIterator(leveldb::ReadOptions()), query.MinKey(), query.MaxKey()),
                [&pattern](const Statement& stmt) -> bool { return Matches(pattern, stmt); });
    } else {
        DLOG(INFO) << "Retrieving statements without filter.";
        return std::make_unique<StatementRangeIterator>(
                db->NewIterator(leveldb::ReadOptions()), query.MinKey(), query.MaxKey());
    }
}


void LevelDBPersistence::GetStatements(
        const Statement& pattern, std::function<bool(const Statement&)> callback) {
    auto start = std::chrono::steady_clock::now();
    int64_t count = 0;

    bool cbsuccess = true;
    for(auto it = GetStatements(pattern); cbsuccess && it->hasNext(); ) {
        cbsuccess = callback(it->next());
        count++;
    }

    DLOG(INFO) << "Get statements done (count=" << count << ", time="
               << std::chrono::duration <double, std::milli> (
                    std::chrono::steady_clock::now() - start).count()
               << "ms).";
}


service::proto::UpdateResponse LevelDBPersistence::RemoveStatements(
        const rdf::proto::Statement& pattern) {
    auto start = std::chrono::steady_clock::now();
    DLOG(INFO) << "Remove statements matching pattern " << pattern.DebugString();

    leveldb::WriteBatch batch_spoc, batch_cspo, batch_opsc, batch_pcos;

    int64_t count =
            RemoveStatements(pattern, batch_spoc, batch_cspo, batch_opsc, batch_pcos);

    std::vector<std::future<void>> writers;
    writers.push_back(workers.push([&](int id) {
        CHECK_STATUS(db_pcos->Write(leveldb::WriteOptions(), &batch_pcos));
    }));
    writers.push_back(workers.push([&](int id) {
        CHECK_STATUS(db_opsc->Write(leveldb::WriteOptions(), &batch_opsc));
    }));
    writers.push_back(workers.push([&](int id) {
        CHECK_STATUS(db_cspo->Write(leveldb::WriteOptions(), &batch_cspo));
    }));
    writers.push_back(workers.push([&](int id) {
        CHECK_STATUS(db_spoc->Write(leveldb::WriteOptions(), &batch_spoc));
    }));

    for (auto& t : writers) {
        t.wait();
    }

    DLOG(INFO) << "Removed " << count << " statements (time=" <<
               std::chrono::duration <double, std::milli> (
                       std::chrono::steady_clock::now() - start).count()
               << "ms).";

    service::proto::UpdateResponse result;
    result.set_removed_statements(count);
    return result;
}

service::proto::UpdateResponse LevelDBPersistence::Update(LevelDBPersistence::UpdateIterator &it) {
    auto start = std::chrono::steady_clock::now();
    LOG(INFO) << "Starting batch update operation.";
    int64_t added_stmts = 0, removed_stmts = 0, added_ns = 0, removed_ns = 0;

    WriteBatch b_spoc, b_cspo, b_opsc, b_pcos, b_prefix, b_url;
    auto writeBatches = [&]{
        std::vector<std::future<void>> writers;
        writers.push_back(workers.push([&](int id) {
            CHECK_STATUS(db_pcos->Write(leveldb::WriteOptions(), &b_pcos));
            b_pcos.Clear();
        }));
        writers.push_back(workers.push([&](int id) {
            CHECK_STATUS(db_opsc->Write(leveldb::WriteOptions(), &b_opsc));
            b_opsc.Clear();
        }));
        writers.push_back(workers.push([&](int id) {
            CHECK_STATUS(db_cspo->Write(leveldb::WriteOptions(), &b_cspo));
            b_cspo.Clear();
        }));
        writers.push_back(workers.push([&](int id) {
            CHECK_STATUS(db_spoc->Write(leveldb::WriteOptions(), &b_spoc));
            b_spoc.Clear();
        }));
        writers.push_back(workers.push([&](int id) {
            CHECK_STATUS(db_ns_prefix->Write(leveldb::WriteOptions(), &b_prefix));
            b_prefix.Clear();
        }));
        writers.push_back(workers.push([&](int id) {
            CHECK_STATUS(db_ns_url->Write(leveldb::WriteOptions(), &b_url));
            b_url.Clear();
        }));

        for (auto& t : writers) {
            t.wait();
        }
    };

    long count = 0;
    while (it.hasNext()) {
        auto next = it.next();
        if (next.has_stmt_added()) {
            AddStatement(next.stmt_added(), b_spoc, b_cspo, b_opsc, b_pcos);
            added_stmts++;
        } else if (next.has_stmt_removed()) {
            removed_stmts +=
                    RemoveStatements(next.stmt_removed(), b_spoc, b_cspo, b_opsc, b_pcos);
        } else if(next.has_ns_added()) {
            AddNamespace(next.ns_added(), b_prefix, b_url);
            added_ns++;
        } else if(next.has_ns_removed()) {
            RemoveNamespace(next.ns_removed(), b_prefix, b_url);
            removed_ns++;
        }

        count++;
        if (count % FLAGS_write_batch_size == 0) {
            writeBatches();
        }
    }

    writeBatches();

    LOG(INFO) << "Batch update complete. (statements added: " << added_stmts
            << ", statements removed: " << removed_stmts
            << ", namespaces added: " << added_ns
            << ", namespaces removed: " << removed_ns
            << ", time=" << std::chrono::duration <double, std::milli> (
                std::chrono::steady_clock::now() - start).count() << "ms).";

    service::proto::UpdateResponse stats;
    stats.set_added_statements(added_stmts);
    stats.set_removed_statements(removed_stmts);
    stats.set_added_namespaces(added_ns);
    stats.set_removed_namespaces(removed_ns);
    return stats;
}

void LevelDBPersistence::AddNamespace(
        const Namespace &ns, WriteBatch &ns_prefix, WriteBatch &ns_url) {
    DLOG(INFO) << "Adding namespace " << ns.DebugString();

    std::string buffer;
    ns.SerializeToString(&buffer);
    ns_prefix.Put(ns.prefix(), buffer);
    ns_url.Put(ns.uri(), buffer);
}

void LevelDBPersistence::RemoveNamespace(
        const Namespace &pattern, WriteBatch &ns_prefix, WriteBatch &ns_url) {
    DLOG(INFO) << "Removing namespaces matching pattern " << pattern.DebugString();

    GetNamespaces(pattern, [&ns_prefix, &ns_url](const rdf::proto::Namespace& ns) -> bool {
        ns_prefix.Delete(ns.prefix());
        ns_url.Delete(ns.uri());
        return true;
    });
}


void LevelDBPersistence::AddStatement(
        const Statement &stmt,
        WriteBatch &spoc, WriteBatch &cspo, WriteBatch &opsc, WriteBatch &pcos) {
    DLOG(INFO) << "Adding statement " << stmt.DebugString();

    Key key(stmt);

    Statement encoded = stmt;
    rdf::EncodeWellknownURI(&encoded);
    std::string buffer;
    encoded.SerializeToString(&buffer);

    char *k_spoc = key.Create(IndexTypes::SPOC);
    spoc.Put(leveldb::Slice(k_spoc, 4 * KEY_LENGTH), buffer);

    char *k_cspo = key.Create(IndexTypes::CSPO);
    cspo.Put(leveldb::Slice(k_cspo, 4 * KEY_LENGTH), buffer);

    char *k_opsc = key.Create(IndexTypes::OPSC);
    opsc.Put(leveldb::Slice(k_opsc, 4 * KEY_LENGTH), buffer);

    char *k_pcos = key.Create(IndexTypes::PCOS);
    pcos.Put(leveldb::Slice(k_pcos, 4 * KEY_LENGTH), buffer);

    delete[] k_spoc;
    delete[] k_cspo;
    delete[] k_opsc;
    delete[] k_pcos;
}


int64_t LevelDBPersistence::RemoveStatements(
        const Statement& pattern,
        WriteBatch& spoc, WriteBatch& cspo, WriteBatch& opsc, WriteBatch&pcos) {
    DLOG(INFO) << "Removing statements matching " << pattern.DebugString();

    int64_t count = 0;

    GetStatements(pattern, [&](const Statement stmt) -> bool {
        Key key(stmt);

        char* k_spoc = key.Create(IndexTypes::SPOC);
        spoc.Delete(leveldb::Slice(k_spoc, 4 * KEY_LENGTH));

        char* k_cspo = key.Create(IndexTypes::CSPO);
        cspo.Delete(leveldb::Slice(k_cspo, 4 * KEY_LENGTH));

        char* k_opsc = key.Create(IndexTypes::OPSC);
        opsc.Delete(leveldb::Slice(k_opsc, 4 * KEY_LENGTH));

        char* k_pcos = key.Create(IndexTypes::PCOS);
        pcos.Delete(leveldb::Slice(k_pcos, 4 * KEY_LENGTH));

        delete[] k_spoc;
        delete[] k_cspo;
        delete[] k_opsc;
        delete[] k_pcos;

        count++;

        return true;
    });

    return count;
}

int KeyComparator::Compare(const leveldb::Slice& a, const leveldb::Slice& b) const {
    return memcmp(a.data(), b.data(), 4 * KEY_LENGTH);
}


int64_t LevelDBPersistence::Size() {
    int64_t count = 0;
    leveldb::Iterator* it = db_cspo->NewIterator(leveldb::ReadOptions());
    for (it->SeekToFirst(); it->Valid(); it->Next()) {
        count++;
    }

    delete it;
    return count;
}

}  // namespace persistence
}  // namespace marmotta


