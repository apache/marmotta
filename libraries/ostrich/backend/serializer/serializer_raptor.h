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
#ifndef MARMOTTA_RDF_SERIALIZER_H
#define MARMOTTA_RDF_SERIALIZER_H

#include "serializer_base.h"
#include <raptor2/raptor2.h>

namespace marmotta {
namespace serializer {

/**
 * Serializer implementation using the Raptor library to write out statements
 * in different RDF serialization formats.
 */
class RaptorSerializer : public SerializerBase {
 public:
    RaptorSerializer(const rdf::URI& baseUri, Format format);
    RaptorSerializer(const rdf::URI& baseUri, Format format, std::vector<rdf::Namespace> namespaces);
    RaptorSerializer(const rdf::URI& baseUri, Format format, std::map<std::string, rdf::URI> namespaces);
    ~RaptorSerializer() override;

 private:
    raptor_serializer* serializer;
    raptor_world*      world;
    raptor_uri*        base;
    raptor_iostream*   stream;

    void prepare(std::ostream& out) override;
    void serialize(const rdf::Statement& stmt) override;
    void close() override;

    void initRaptor();
};


}
}

#endif //MARMOTTA_RDF_SERIALIZER_H
