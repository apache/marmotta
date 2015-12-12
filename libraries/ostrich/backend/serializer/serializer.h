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
#ifndef MARMOTTA_SERIALIZER_H
#define MARMOTTA_SERIALIZER_H

#include "serializer_base.h"

namespace marmotta {
namespace serializer {


class Serializer {
 public:
    using StatementIterator = util::CloseableIterator<rdf::Statement>;

    Serializer(const rdf::URI& baseUri, Format format)
            : Serializer(baseUri, format, std::map<std::string, rdf::URI>()) {};
    Serializer(const rdf::URI& baseUri, Format format, std::vector<rdf::Namespace> namespaces);
    Serializer(const rdf::URI& baseUri, Format format, std::map<std::string, rdf::URI> namespaces);

    ~Serializer() {};

    void serialize(const rdf::Statement& stmt, std::ostream& out) {
        impl->serialize(stmt, out);
    };

    void serialize(StatementIterator it, std::ostream& out) {
        impl->serialize(it, out);
    };

 private:
    std::unique_ptr<SerializerBase> impl;
};


}  // namespace serializer
}  // namespace marmotta

#endif //MARMOTTA_SERIALIZER_H
