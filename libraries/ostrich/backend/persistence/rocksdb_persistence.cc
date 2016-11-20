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
#include <queue>

#include <gflags/gflags.h>
#include <glog/logging.h>
#include <rocksdb/filter_policy.h>
#include <rocksdb/statistics.h>
#include <rocksdb/write_batch.h>
#include <google/protobuf/wrappers.pb.h>
#include <thread>
#include <algorithm>

#include "rocksdb_persistence.h"
#include "model/rdf_operators.h"

#define CHECK_STATUS(s) CHECK(s.ok()) << "Writing to database failed: " << s.ToString()

DEFINE_int64(write_batch_size, 100000,
             "Maximum number of statements to write in a single batch to the database");
DEFINE_bool(enable_statistics, false,
             "Enable statistics collection and output.");


constexpr char kSPOC[] = "spoc";
constexpr char kCSPO[] = "cspo";
constexpr char kOPSC[] = "opsc";
constexpr char kPCOS[] = "pcos";
constexpr char kNSPREFIX[] = "nsprefix";
constexpr char kNSURI[] = "nsuri";
constexpr char kMETA[] = "meta";

using rocksdb::ColumnFamilyDescriptor;
using rocksdb::ColumnFamilyHandle;
using rocksdb::ColumnFamilyOptions;
using rocksdb::WriteBatch;
using rocksdb::Slice;
using marmotta::rdf::proto::Statement;
using marmotta::rdf::proto::Namespace;
using marmotta::rdf::proto::Resource;

namespace marmotta {
namespace persistence {
namespace {

// Base iterator for wrapping a RocksDB iterators.
template<typename T>
using RocksDBIterator = DBIterator<T, rocksdb::Iterator>;

// Iterator wrapping a RocksDB Statement iterator over a given key range.
class StatementRangeIterator : public RocksDBIterator<Statement> {
 public:
    StatementRangeIterator(rocksdb::Iterator *it, char *loKey, char *hiKey)
            : DBIterator(it), loKey(loKey), hiKey(hiKey) {
        it->Seek(rocksdb::Slice(loKey, 4 * KEY_LENGTH));
    }

    ~StatementRangeIterator() override {
        delete[] loKey;
        delete[] hiKey;
    };

    bool hasNext() override {
        return it->Valid() && it->key().compare(rocksdb::Slice(hiKey, 4 * KEY_LENGTH)) <= 0;
    }

 private:
    char *loKey;
    char *hiKey;
};


}  // namespace


RocksDBPersistence::RocksDBPersistence(const std::string &path, int64_t cacheSize) {
    rocksdb::Options options;
    options.create_if_missing = true;
    options.create_missing_column_families = true;

    options.IncreaseParallelism();
    options.OptimizeLevelStyleCompaction();

    // Custom comparator for our keys.
    options.comparator = &comparator_;

    // Write buffer size 16MB (fast bulk imports)
    options.write_buffer_size = 16384 * 1024;

    if (FLAGS_enable_statistics) {
        options.statistics = rocksdb::CreateDBStatistics();
        options.stats_dump_period_sec = 300;
    }

    ColumnFamilyOptions cfOptions;
    cfOptions.OptimizeLevelStyleCompaction();

    // Initialise column families.
    std::vector<ColumnFamilyDescriptor> columnFamilies = {
            ColumnFamilyDescriptor(kSPOC, cfOptions),
            ColumnFamilyDescriptor(kCSPO, cfOptions),
            ColumnFamilyDescriptor(kOPSC, cfOptions),
            ColumnFamilyDescriptor(kPCOS, cfOptions),
            ColumnFamilyDescriptor(kNSPREFIX, cfOptions),
            ColumnFamilyDescriptor(kNSURI, cfOptions),
            ColumnFamilyDescriptor(kMETA, cfOptions),
            ColumnFamilyDescriptor("default", cfOptions),
    };

    rocksdb::DB* db;
    rocksdb::Status status = rocksdb::DB::Open(options, path + "/data.db", columnFamilies, &handles_, &db);
    CHECK_STATUS(status);
    database_.reset(db);

    LOG(INFO) << "RocksDB Database initialised.";
}

RocksDBPersistence::~RocksDBPersistence() {
    std::for_each(handles_.begin(), handles_.end(), [](ColumnFamilyHandle* h) {
        delete h;
    });
}


service::proto::UpdateResponse  RocksDBPersistence::AddNamespaces(NamespaceIterator& it) {
    DLOG(INFO) << "Starting batch namespace import operation.";
    int64_t count = 0;

    rocksdb::WriteBatch batch;

    while (it.hasNext()) {
        AddNamespace(it.next(), batch);
        count++;
    }
    CHECK_STATUS(database_->Write(rocksdb::WriteOptions(), &batch));

    DLOG(INFO) << "Imported " << count << " namespaces";

    service::proto::UpdateResponse result;
    result.set_added_namespaces(count);
    return result;
}

std::unique_ptr<RocksDBPersistence::NamespaceIterator> RocksDBPersistence::GetNamespaces(
        const rdf::proto::Namespace &pattern) {
    DLOG(INFO) << "Get namespaces matching pattern " << pattern.DebugString();

    Namespace ns;

    ColumnFamilyHandle *h = nullptr;
    std::string key, value;
    if (pattern.prefix() != "") {
        key = pattern.prefix();
        h = handles_[Handles::NSPREFIX];
    } else if(pattern.uri() != "") {
        key = pattern.uri();
        h = handles_[Handles::NSURI];
    }
    if (h != nullptr) {
        // Either prefix or uri given, report the correct namespace value.
        rocksdb::Status s = database_->Get(rocksdb::ReadOptions(), h, key, &value);
        if (s.ok()) {
            ns.ParseFromString(value);
            return std::make_unique<util::SingletonIterator<Namespace>>(std::move(ns));
        } else {
            return std::make_unique<util::EmptyIterator<Namespace>>();
        }
    } else {
        // Pattern was empty, iterate over all namespaces and report them.
        return std::make_unique<RocksDBIterator<Namespace>>(
                database_->NewIterator(rocksdb::ReadOptions(), handles_[Handles::NSPREFIX]));
    }
}


void RocksDBPersistence::GetNamespaces(
        const Namespace &pattern, RocksDBPersistence::NamespaceHandler callback) {
    int64_t count = 0;

    bool cbsuccess = true;
    for(auto it = GetNamespaces(pattern); cbsuccess && it->hasNext();) {
        cbsuccess = callback(it->next());
        count++;
    }

    DLOG(INFO) << "Get namespaces done (count=" << count <<")";
}


service::proto::UpdateResponse RocksDBPersistence::AddStatements(StatementIterator& it) {
    auto start = std::chrono::steady_clock::now();
    LOG(INFO) << "Starting batch statement import operation.";
    int64_t count = 0;

    rocksdb::WriteBatch batch;
    while (it.hasNext()) {
        AddStatement(it.next(), batch);
        count++;

        if (count % FLAGS_write_batch_size == 0) {
            CHECK_STATUS(database_->Write(rocksdb::WriteOptions(), &batch));
            batch.Clear();
        }
    }

    CHECK_STATUS(database_->Write(rocksdb::WriteOptions(), &batch));
    batch.Clear();

    LOG(INFO) << "Imported " << count << " statements (time="
              << std::chrono::duration <double, std::milli> (
                   std::chrono::steady_clock::now() - start).count()
              << "ms).";

    service::proto::UpdateResponse result;
    result.set_added_statements(count);
    return result;
}


std::unique_ptr<RocksDBPersistence::StatementIterator> RocksDBPersistence::GetStatements(
        const rdf::proto::Statement &pattern) {
    DLOG(INFO) << "Get statements matching pattern " << pattern.DebugString();

    Pattern query(pattern);

    ColumnFamilyHandle* h;
    switch (query.Type()) {
        case IndexTypes::SPOC:
            h = handles_[Handles::ISPOC];
            DLOG(INFO) << "Query: Using index type SPOC";
            break;
        case IndexTypes::CSPO:
            h = handles_[Handles::ICSPO];
            DLOG(INFO) << "Query: Using index type CSPO";
            break;
        case IndexTypes::OPSC:
            h = handles_[Handles::IOPSC];
            DLOG(INFO) << "Query: Using index type OPSC";
            break;
        case IndexTypes::PCOS:
            h = handles_[Handles::IPCOS];
            DLOG(INFO) << "Query: Using index type PCOS";
            break;
    };

    if (query.NeedsFilter()) {
        DLOG(INFO) << "Retrieving statements with filter.";
        return std::make_unique<util::FilteringIterator<Statement>>(
                new StatementRangeIterator(
                        database_->NewIterator(rocksdb::ReadOptions(), h), query.MinKey(), query.MaxKey()),
                [&pattern](const Statement& stmt) -> bool { return Matches(pattern, stmt); });
    } else {
        DLOG(INFO) << "Retrieving statements without filter.";
        return std::make_unique<StatementRangeIterator>(
                database_->NewIterator(rocksdb::ReadOptions(), h), query.MinKey(), query.MaxKey());
    }
}


void RocksDBPersistence::GetStatements(
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


service::proto::UpdateResponse RocksDBPersistence::RemoveStatements(const rdf::proto::Statement& pattern) {
    auto start = std::chrono::steady_clock::now();
    DLOG(INFO) << "Remove statements matching pattern " << pattern.DebugString();

    rocksdb::WriteBatch batch;

    int64_t count = RemoveStatements(pattern, batch);
    CHECK_STATUS(database_->Write(rocksdb::WriteOptions(), &batch));

    DLOG(INFO) << "Removed " << count << " statements (time=" <<
               std::chrono::duration <double, std::milli> (
                       std::chrono::steady_clock::now() - start).count()
               << "ms).";

    service::proto::UpdateResponse result;
    result.set_removed_statements(count);
    return result;
}

service::proto::UpdateResponse RocksDBPersistence::Update(RocksDBPersistence::UpdateIterator &it) {
    auto start = std::chrono::steady_clock::now();
    LOG(INFO) << "Starting batch update operation.";

    WriteBatch batch;
    int64_t added_stmts = 0, removed_stmts = 0, added_ns = 0, removed_ns = 0;

    long count = 0;
    while (it.hasNext()) {
        auto next = it.next();
        if (next.has_stmt_added()) {
            AddStatement(next.stmt_added(), batch);
            added_stmts++;
        } else if (next.has_stmt_removed()) {
            removed_stmts +=
                    RemoveStatements(next.stmt_removed(), batch);
        } else if(next.has_ns_added()) {
            AddNamespace(next.ns_added(), batch);
            added_ns++;
        } else if(next.has_ns_removed()) {
            RemoveNamespace(next.ns_removed(), batch);
            removed_ns++;
        }

        count++;
        if (count % FLAGS_write_batch_size == 0) {
            CHECK_STATUS(database_->Write(rocksdb::WriteOptions(), &batch));
            batch.Clear();
        }
    }

    CHECK_STATUS(database_->Write(rocksdb::WriteOptions(), &batch));
    batch.Clear();

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

void RocksDBPersistence::AddNamespace(
        const Namespace &ns, WriteBatch &batch) {
    DLOG(INFO) << "Adding namespace " << ns.DebugString();

    std::string buffer;
    ns.SerializeToString(&buffer);
    batch.Put(handles_[Handles::NSPREFIX], ns.prefix(), buffer);
    batch.Put(handles_[Handles::NSURI], ns.uri(), buffer);
}

void RocksDBPersistence::RemoveNamespace(
        const Namespace &pattern, WriteBatch &batch) {
    DLOG(INFO) << "Removing namespaces matching pattern " << pattern.DebugString();

    GetNamespaces(pattern, [&batch, this](const rdf::proto::Namespace& ns) -> bool {
        batch.Delete(handles_[Handles::NSPREFIX], ns.prefix());
        batch.Delete(handles_[Handles::NSURI], ns.uri());
        return true;
    });
}


void RocksDBPersistence::AddStatement(
        const Statement &stmt, WriteBatch &batch) {
    DLOG(INFO) << "Adding statement " << stmt.DebugString();

    Statement encoded = stmt;
    rdf::EncodeWellknownURI(&encoded);
    std::string buffer;
    encoded.SerializeToString(&buffer);

    Key key(stmt);

    char *k_spoc = key.Create(IndexTypes::SPOC);
    batch.Put(handles_[Handles::ISPOC], rocksdb::Slice(k_spoc, 4 * KEY_LENGTH), buffer);

    char *k_cspo = key.Create(IndexTypes::CSPO);
    batch.Put(handles_[Handles::ICSPO], rocksdb::Slice(k_cspo, 4 * KEY_LENGTH), buffer);

    char *k_opsc = key.Create(IndexTypes::OPSC);
    batch.Put(handles_[Handles::IOPSC], rocksdb::Slice(k_opsc, 4 * KEY_LENGTH), buffer);

    char *k_pcos = key.Create(IndexTypes::PCOS);
    batch.Put(handles_[Handles::IPCOS], rocksdb::Slice(k_pcos, 4 * KEY_LENGTH), buffer);

    delete[] k_spoc;
    delete[] k_cspo;
    delete[] k_opsc;
    delete[] k_pcos;
}


int64_t RocksDBPersistence::RemoveStatements(
        const Statement& pattern, WriteBatch& batch) {
    DLOG(INFO) << "Removing statements matching " << pattern.DebugString();

    int64_t count = 0;

    GetStatements(pattern, [&](const Statement stmt) -> bool {
        Key key(stmt);

        char* k_spoc = key.Create(IndexTypes::SPOC);
        batch.Delete(handles_[Handles::ISPOC], rocksdb::Slice(k_spoc, 4 * KEY_LENGTH));

        char* k_cspo = key.Create(IndexTypes::CSPO);
        batch.Delete(handles_[Handles::ICSPO], rocksdb::Slice(k_cspo, 4 * KEY_LENGTH));

        char* k_opsc = key.Create(IndexTypes::OPSC);
        batch.Delete(handles_[Handles::IOPSC], rocksdb::Slice(k_opsc, 4 * KEY_LENGTH));

        char* k_pcos = key.Create(IndexTypes::PCOS);
        batch.Delete(handles_[Handles::IPCOS], rocksdb::Slice(k_pcos, 4 * KEY_LENGTH));

        delete[] k_spoc;
        delete[] k_cspo;
        delete[] k_opsc;
        delete[] k_pcos;

        count++;

        return true;
    });

    return count;
}

int KeyComparator::Compare(const rocksdb::Slice& a, const rocksdb::Slice& b) const {
    return memcmp(a.data(), b.data(), 4 * KEY_LENGTH);
}


int64_t RocksDBPersistence::Size() {
    int64_t count = 0;
    rocksdb::Iterator* it = database_->NewIterator(rocksdb::ReadOptions(), handles_[Handles::ISPOC]);
    for (it->SeekToFirst(); it->Valid(); it->Next()) {
        count++;
    }

    delete it;
    return count;
}

}  // namespace persistence
}  // namespace marmotta


