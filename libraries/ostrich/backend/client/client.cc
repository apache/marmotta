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

#include <grpc/grpc.h>
#include <grpc++/channel.h>
#include <grpc++/client_context.h>
#include <grpc++/create_channel.h>
#include <grpc++/security/credentials.h>
#include <grpc++/support/sync_stream.h>

#include <google/protobuf/text_format.h>
#include <google/protobuf/empty.pb.h>
#include <google/protobuf/wrappers.pb.h>

#include <gflags/gflags.h>
#include <glog/logging.h>

#include "model/rdf_model.h"
#include "parser/rdf_parser.h"
#include "serializer/serializer.h"
#include "service/sail.pb.h"
#include "service/sail.grpc.pb.h"
#include "service/sparql.pb.h"
#include "service/sparql.grpc.pb.h"


using grpc::Channel;
using grpc::ClientContext;
using grpc::ClientReader;
using grpc::ClientReaderWriter;
using grpc::ClientWriter;
using grpc::Status;
using google::protobuf::TextFormat;

using namespace marmotta;
namespace svc = marmotta::service::proto;
namespace spq = marmotta::sparql::proto;

#ifdef HAVE_IOSTREAMS
using namespace boost::iostreams;
#endif

// A STL iterator wrapper around a client reader.
template <class T, class Proto>
class ClientReaderIterator : public util::CloseableIterator<T> {
 public:
    ClientReaderIterator() : finished(true) { }

    ClientReaderIterator(ClientReader<Proto>* r) : reader(r), finished(false) {
        read();
    }

    const T& next() override {
        current_ = T(buffer);
        read();
        return current_;
    }

    const T& current() const override {
        return current_;
    }

    bool hasNext() override {
        return !finished;
    }

 private:
    ClientReader<Proto>* reader;
    Proto buffer;
    T current_;
    bool finished;

    void read() {
        if (!finished) {
            finished = !reader->Read(&buffer);
            if (finished) {
                auto st = reader->Finish();
                LOG_IF(FATAL, !st.ok()) << "Reading results failed: " << st.error_message();
            }
        }
    }
};

typedef ClientReaderIterator<rdf::Statement, rdf::proto::Statement> StatementReader;
typedef ClientReaderIterator<rdf::Namespace, rdf::proto::Namespace> NamespaceReader;

class MarmottaClient {
 public:
    MarmottaClient(const std::string& server)
            : stub_(svc::SailService::NewStub(
            grpc::CreateChannel(server, grpc::InsecureChannelCredentials()))),
              sparql_(spq::SparqlService::NewStub(
                      grpc::CreateChannel(server, grpc::InsecureChannelCredentials()))){}

    void importDataset(std::istream& in, parser::Format format) {
        auto start = std::chrono::steady_clock::now();
        int64_t count = 0;

        ClientContext nscontext, stmtcontext;

        google::protobuf::Int64Value nsstats;
        google::protobuf::Int64Value stmtstats;

        std::unique_ptr<ClientWriter<rdf::proto::Namespace> > nswriter(
                stub_->AddNamespaces(&nscontext, &nsstats));
        std::unique_ptr<ClientWriter<rdf::proto::Statement> > stmtwriter(
                stub_->AddStatements(&stmtcontext, &stmtstats));

        parser::Parser p("http://www.example.com", format);
        p.setStatementHandler([&stmtwriter, &start, &count](const rdf::Statement& stmt) {
            stmtwriter->Write(stmt.getMessage());
            if (++count % 100000 == 0) {
                double rate = 100000 * 1000 / std::chrono::duration <double, std::milli> (
                        std::chrono::steady_clock::now() - start).count();
                std::cout << "Imported " << count << " statements (" << rate << " statements/sec)" << std::endl;
                start = std::chrono::steady_clock::now();
            }
        });
        p.setNamespaceHandler([&nswriter](const rdf::Namespace& ns) {
            nswriter->Write(ns.getMessage());
        });
        p.parse(in);

        stmtwriter->WritesDone();
        nswriter->WritesDone();

        Status nsst = nswriter->Finish();
        Status stmtst = stmtwriter->Finish();

        if (nsst.ok() && stmtst.ok()) {
            std::cout << "Added " << nsstats.value() << " namespaces and "
                                  << stmtstats.value() << " statements" << std::endl;
        } else {
            std::cout << "Failed writing data to server: " << stmtst.error_message() << std::endl;
        }
    }


    void patternQuery(const rdf::Statement &pattern, std::ostream &out, serializer::Format format) {
        ClientContext context;

        std::unique_ptr<ClientReader<rdf::proto::Statement> > reader(
            stub_->GetStatements(&context, pattern.getMessage()));

        StatementReader it(reader.get());

        serializer::Serializer serializer("http://www.example.com", format);
        serializer.serialize(it, out);
    }

    void patternDelete(const rdf::Statement &pattern) {
        ClientContext context;
        google::protobuf::Int64Value result;

        Status status = stub_->RemoveStatements(&context, pattern.getMessage(), &result);
        if (status.ok()) {
            std::cout << "Deleted " << result.value() << " statements." << std::endl;
        } else {
            std::cerr << "Failed deleting statements: " << status.error_message() << std::endl;
        }
    }

    void tupleQuery(const std::string& query, std::ostream &out) {
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
    }

    void graphQuery(const std::string& query, std::ostream &out, serializer::Format format) {
        ClientContext context;
        spq::SparqlRequest request;
        request.set_query(query);

        std::unique_ptr<ClientReader<rdf::proto::Statement>> reader(
                sparql_->GraphQuery(&context, request));

        StatementReader it(reader.get());

        try {
            serializer::Serializer serializer("http://www.example.com", format);
            serializer.serialize(it, out);
        } catch(serializer::SerializationError e) {
            LOG(FATAL) << "Serialization error: " << e.getMessage();
        }
    }

    void askQuery(const std::string& query, std::ostream &out) {
        ClientContext context;
        spq::SparqlRequest request;
        request.set_query(query);

        google::protobuf::BoolValue result;

        Status status = sparql_->AskQuery(&context, request, &result);
        if (status.ok()) {
            if (result.value()) {
                out << "YES" << std::endl;
            } else {
                out << "NO" << std::endl;
            }
        } else {
            LOG(FATAL) << "SPARQL query failed: " << status.error_message();
        }
    }


    void listNamespaces(std::ostream &out) {
        ClientContext context;

        google::protobuf::Empty pattern;

        std::unique_ptr<ClientReader<rdf::proto::Namespace> > reader(
                stub_->GetNamespaces(&context, pattern));

        NamespaceReader it(reader.get());
        while (it.hasNext()) {
            const auto& ns = it.next();
            out << ns.getPrefix() << " = " << ns.getUri() << std::endl;
        }
    }

    int64_t size(const svc::ContextRequest& r) {
        ClientContext context;
        google::protobuf::Int64Value result;

        Status status = stub_->Size(&context, r, &result);
        if (status.ok()) {
            return result.value();
        } else {
            LOG(FATAL) << "Calculating size failed: " << status.error_message();
        }
    }
 private:
    std::unique_ptr<svc::SailService::Stub> stub_;
    std::unique_ptr<spq::SparqlService::Stub> sparql_;
};


DEFINE_string(format, "rdfxml", "RDF format to use for parsing/serializing.");
DEFINE_string(host, "localhost", "Address/name of server to access.");
DEFINE_string(port, "10000", "Port of server to access.");
DEFINE_string(output, "", "File to write result to.");

#ifdef HAVE_IOSTREAMS
DEFINE_bool(gzip, false, "Input files are gzip compressed.");
DEFINE_bool(bzip2, false, "Input files are bzip2 compressed.");
#endif

int main(int argc, char** argv) {
    GOOGLE_PROTOBUF_VERIFY_VERSION;

    // Initialize Google's logging library.
    google::InitGoogleLogging(argv[0]);
    google::ParseCommandLineFlags(&argc, &argv, true);

    MarmottaClient client(FLAGS_host + ":" + FLAGS_port);

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

    if ("construct" == std::string(argv[1])) {
        std::string query = argv[2];
        if (FLAGS_output != "") {
            std::ofstream out(FLAGS_output);
            client.graphQuery(query, out, serializer::FormatFromString(FLAGS_format));
        } else {
            client.graphQuery(query, std::cout, serializer::FormatFromString(FLAGS_format));
        }
    }

    if ("ask" == std::string(argv[1])) {
        std::string query = argv[2];
        if (FLAGS_output != "") {
            std::ofstream out(FLAGS_output);
            client.askQuery(query, out);
        } else {
            client.askQuery(query, std::cout);
        }
    }

    if ("delete" == std::string(argv[1])) {
        rdf::proto::Statement query;
        TextFormat::ParseFromString(argv[2], &query);
        client.patternDelete(rdf::Statement(query));
    }

    if ("size" == std::string(argv[1])) {
        svc::ContextRequest query;
        TextFormat::ParseFromString(argv[2], &query);
        std::cout << "Size: " << client.size(query) << std::endl;
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