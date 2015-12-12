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
#include "serializer_base.h"

namespace marmotta {
namespace serializer {

namespace {
static std::map<std::string, rdf::URI> namespacesMap(std::vector<rdf::Namespace> list) {
    std::map<std::string, rdf::URI> result;
    for (auto it = list.cbegin(); it != list.cend(); it++) {
        result[it->getPrefix()] = it->getUri();
    }
    return result;
}
}  // namespace


Format FormatFromString(const std::string &name) {
    if (name == "rdfxml" || name == "rdf/xml" || name == "xml") {
        return RDFXML;
    }
    if (name == "n3" || name == "ntriples" || name == "text/n3") {
        return NTRIPLES;
    }
    if (name == "turtle" || name == "text/turtle") {
        return TURTLE;
    }
    if (name == "textproto" || name == "text/proto") {
        return PROTO_TEXT;
    }
    if (name == "proto" || name == "application/proto") {
        return PROTO;
    }
    if (name == "json" || name == "application/json" || name == "application/rdf+json") {
        return RDFJSON;
    }
    return RDFXML;
}

SerializerBase::SerializerBase(const rdf::URI& baseUri, Format format, std::vector<rdf::Namespace> namespaces)
        : baseUri(baseUri), format(format), namespaces(namespacesMap(namespaces)) { }

SerializerBase::SerializerBase(const rdf::URI& baseUri, Format format, std::map<std::string, rdf::URI> namespaces)
        : baseUri(baseUri), format(format), namespaces(namespaces) { }


}  // namespace serializer
}  // namespace marmotta
