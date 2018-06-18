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
#ifndef MARMOTTA_PROTO_SERIALIZER_H
#define MARMOTTA_PROTO_SERIALIZER_H

#include "serializer_base.h"

namespace marmotta {
namespace serializer {
/**
 * Serialize statements as binary proto wire format according to model.proto.
 */
class ProtoSerializer : public SerializerBase {
 public:
    ProtoSerializer(const rdf::URI& baseUri, Format format)
            : ProtoSerializer(baseUri, format, std::map<std::string, rdf::URI>()) {};
    ProtoSerializer(const rdf::URI& baseUri, Format format, std::vector<rdf::Namespace> namespaces)
            : SerializerBase(baseUri, format, namespaces) {};
    ProtoSerializer(const rdf::URI& baseUri, Format format, std::map<std::string, rdf::URI> namespaces)
            : SerializerBase(baseUri, format, namespaces) {};

 private:
    void prepare(std::ostream& out) override;
    void serialize(const rdf::Statement& stmt) override;
    void close() override;

    google::protobuf::io::OstreamOutputStream* out_;
    marmotta::rdf::proto::Statements stmts_;
};



}
}
#endif //MARMOTTA_PROTO_SERIALIZER_H
