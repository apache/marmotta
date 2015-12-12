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
#ifndef MARMOTTA_BASE_SERIALIZER_H
#define MARMOTTA_BASE_SERIALIZER_H

#include <string>
#include <map>
#include <memory>
#include <vector>
#include <google/protobuf/io/zero_copy_stream_impl.h>

#include <model/rdf_model.h>
#include <util/iterator.h>


namespace marmotta {
namespace serializer {

enum Format {
    RDFXML, RDFXML_ABBREV, TURTLE, NTRIPLES, NQUADS, RDFJSON, SPARQL_JSON, GRAPHVIZ, PROTO, PROTO_TEXT
};


/**
 * Return the format matching the string name passed as argument.
 */
Format FormatFromString(const std::string &name);

/**
 * Serialize statements in various RDF text formats. This class and its subclasses are not thread safe.
 */
class SerializerBase {
 public:
    using StatementIterator = util::CloseableIterator<rdf::Statement>;

    SerializerBase(const rdf::URI &baseUri, Format format)
            : SerializerBase(baseUri, format, std::map<std::string, rdf::URI>()) { };

    SerializerBase(const rdf::URI &baseUri, Format format, std::vector<rdf::Namespace> namespaces);

    SerializerBase(const rdf::URI &baseUri, Format format, std::map<std::string, rdf::URI> namespaces);

    virtual ~SerializerBase() { };

    void serialize(const rdf::Statement &stmt, std::ostream &out) {
        prepare(out);
        serialize(stmt);
        close();
    };

    void serialize(StatementIterator &it, std::ostream &out) {
        prepare(out);
        for (; it.hasNext(); ++it) {
            serialize(*it);
        }
        close();
    };

 protected:
    rdf::URI baseUri;
    Format format;
    std::map<std::string, rdf::URI> namespaces;

    virtual void prepare(std::ostream &out) = 0;

    virtual void serialize(const rdf::Statement &stmt) = 0;

    virtual void close() = 0;
};


class SerializationError : std::exception {
 public:
    SerializationError(const char* message) : message(message) { }
    SerializationError(std::string &message) : message(message) { }

    const std::string &getMessage() const {
        return message;
    }

 private:
    std::string message;
};


}  // namespace serializer
}  // namespace marmotta

#endif //MARMOTTA_BASE_SERIALIZER_H
