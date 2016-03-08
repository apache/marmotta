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
#include <fstream>

#ifdef HAVE_IOSTREAMS
// support b/gzipped files
#include <boost/iostreams/filtering_streambuf.hpp>
#include <boost/iostreams/copy.hpp>
#include <boost/iostreams/filter/gzip.hpp>
#include <boost/iostreams/filter/bzip2.hpp>
#endif

#include <google/protobuf/text_format.h>
#include <google/protobuf/empty.pb.h>
#include <google/protobuf/wrappers.pb.h>

#include <gflags/gflags.h>
#include <thread>
#include <glog/logging.h>
#include <sys/stat.h>

#include "model/rdf_model.h"
#include "parser/rdf_parser.h"
#include "serializer/serializer.h"
#include "persistence/leveldb_persistence.h"

using namespace marmotta;
using google::protobuf::TextFormat;

#ifdef HAVE_IOSTREAMS
using namespace boost::iostreams;
#endif

class MarmottaClient {
 public:
    MarmottaClient(marmotta::persistence::LevelDBPersistence* db)
            : db(db){ }

    void importDataset(std::istream& in, parser::Format format) {
        auto start = std::chrono::steady_clock::now();
        int64_t count = 0;

        parser::Parser p("http://www.example.com", format);
        util::ProducerConsumerIterator<rdf::proto::Statement> stmtit;
        util::ProducerConsumerIterator<rdf::proto::Namespace> nsit;
        p.setStatementHandler([&stmtit](const rdf::Statement& stmt) {
            stmtit.add(stmt.getMessage());
        });
        p.setNamespaceHandler([&nsit](const rdf::Namespace& ns) {
            nsit.add(ns.getMessage());
        });

        std::thread([&p, &in, &stmtit, &nsit]() {
            p.parse(in);
            stmtit.finish();
            nsit.finish();
        });

        db->AddStatements(stmtit);
        db->AddNamespaces(nsit);
    }


    void patternQuery(const rdf::Statement &pattern, std::ostream &out, serializer::Format format) {
    }

    void patternDelete(const rdf::Statement &pattern) {
        db->RemoveStatements(pattern.getMessage());
    }

    void tupleQuery(const std::string& query, std::ostream &out) {
        /*
        ClientContext context;
        spq::SparqlRequest request;
        request.set_query(query);

        std::unique_ptr<ClientReader<spq::SparqlResponse>> reader(
                sparql_->TupleQuery(&context, request));

        auto out_ = new google::protobuf::io::OstreamOutputStream(&out);
        spq::SparqlResponse result;
        while (reader->Read(&result)) {
            TextFormat::Print(result, dynamic_cast<google::protobuf::io::ZeroCopyOutputStream*>(out_));
        }
        delete out_;
         */
    }

    void listNamespaces(std::ostream &out) {
        /*
        ClientContext context;

        google::protobuf::Empty pattern;

        std::unique_ptr<ClientReader<rdf::proto::Namespace> > reader(
                stub_->GetNamespaces(&context, pattern));

        NamespaceReader it(reader.get());
        for (; it.hasNext(); ++it) {
            out << (*it).getPrefix() << " = " << (*it).getUri() << std::endl;
        }
         */
    }

    int64_t size() {
        return db->Size();
    }
 private:
    marmotta::persistence::LevelDBPersistence* db;
};


DEFINE_string(format, "rdfxml", "RDF format to use for parsing/serializing.");
DEFINE_string(output, "", "File to write result to.");
DEFINE_string(db, "/tmp/testdb", "Path to database. Will be created if non-existant.");
DEFINE_int64(cache_size, 100 * 1048576, "Cache size used by the database (in bytes).");

#ifdef HAVE_IOSTREAMS
DEFINE_bool(gzip, false, "Input files are gzip compressed.");
DEFINE_bool(bzip2, false, "Input files are bzip2 compressed.");
#endif

int main(int argc, char** argv) {
    GOOGLE_PROTOBUF_VERIFY_VERSION;

    // Initialize Google's logging library.
    google::InitGoogleLogging(argv[0]);
    google::ParseCommandLineFlags(&argc, &argv, true);

    mkdir(FLAGS_db.c_str(), 0700);
    marmotta::persistence::LevelDBPersistence persistence(FLAGS_db, FLAGS_cache_size);

    MarmottaClient client(&persistence);

    if ("import" == std::string(argv[1])) {
#ifdef HAVE_IOSTREAMS
        std::ifstream file(argv[2]);
        filtering_streambuf<input> cin;
        if (FLAGS_bzip2) {
            cin.push(bzip2_decompressor());
        }
        if (FLAGS_gzip) {
            cin.push(gzip_decompressor());
        }
        cin.push(file);

        std::istream in(&cin);
#else
        std::ifstream in(argv[2]);
#endif
        std::cout << "Importing " << argv[2] << " ... " << std::endl;
        client.importDataset(in, parser::FormatFromString(FLAGS_format));
        std::cout << "Finished!" << std::endl;
    }

    if ("select" == std::string(argv[1])) {
        rdf::proto::Statement query;
        TextFormat::ParseFromString(argv[2], &query);
        if (FLAGS_output != "") {
            std::ofstream out(FLAGS_output);
            client.patternQuery(rdf::Statement(query), out, serializer::FormatFromString(FLAGS_format));
        } else {
            client.patternQuery(rdf::Statement(query), std::cout, serializer::FormatFromString(FLAGS_format));
        }
    }

    if ("sparql" == std::string(argv[1])) {
        std::string query = argv[2];
        if (FLAGS_output != "") {
            std::ofstream out(FLAGS_output);
            client.tupleQuery(query, out);
        } else {
            client.tupleQuery(query, std::cout);
        }
    }

    if ("delete" == std::string(argv[1])) {
        rdf::proto::Statement query;
        TextFormat::ParseFromString(argv[2], &query);
        client.patternDelete(rdf::Statement(query));
    }

    if ("size" == std::string(argv[1])) {
        std::cout << "Size: " << client.size() << std::endl;
    }


    if ("namespaces" == std::string(argv[1])) {
        if (FLAGS_output != "") {
            std::ofstream out(FLAGS_output);
            client.listNamespaces(out);
        } else {
            client.listNamespaces(std::cout);
        }
    }

    google::protobuf::ShutdownProtobufLibrary();

    return 0;
}