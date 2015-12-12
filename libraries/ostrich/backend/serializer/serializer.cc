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
#include "serializer.h"

#include "serializer_raptor.h"
#include "serializer_proto.h"

namespace marmotta {
namespace serializer {

Serializer::Serializer(const rdf::URI &baseUri, Format format, std::vector<rdf::Namespace> namespaces) {
    switch(format) {
        case PROTO:
        case PROTO_TEXT:
            impl.reset(new ProtoSerializer(baseUri, format, namespaces));
            break;
        default:
            impl.reset(new RaptorSerializer(baseUri, format, namespaces));
    }
}

Serializer::Serializer(const rdf::URI &baseUri, Format format, std::map<std::string, rdf::URI> namespaces) {
    switch(format) {
        case PROTO:
        case PROTO_TEXT:
            impl.reset(new ProtoSerializer(baseUri, format, namespaces));
            break;
        default:
            impl.reset(new RaptorSerializer(baseUri, format, namespaces));
    }
}

}  // namespace serializer
}  // namespace marmotta
