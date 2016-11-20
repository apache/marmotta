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
#include "model/rdf_namespaces.h"

namespace marmotta {
namespace rdf {

const std::map<std::string, std::string>& NamespacesByPrefix() {
    static const std::map<std::string, std::string> kNamespacePrefixes = {
            {"skos:", "http://www.w3.org/2004/02/skos/core#"},
            {"rdf:",  "http://www.w3.org/1999/02/22-rdf-syntax-ns#"},
            {"rdfs:", "http://www.w3.org/2000/01/rdf-schema#"},
            {"owl:",  "http://www.w3.org/2002/07/owl#"},
            {"xmls:", "http://www.w3.org/2001/XMLSchema#"},
            {"foaf:", "http://xmlns.com/foaf/0.1/"},
            {"dcterms:", "http://purl.org/dc/terms/"},
            {"dcelems:", "http://purl.org/dc/elements/1.1/"},
            {"dctypes:", "http://purl.org/dc/dcmitype/"},
            {"dbpedia:", "http://dbpedia.org/resource/"},

    };
    return kNamespacePrefixes;
}

// Apply prefix substitution for well-known URIs to save disk space.
// Modifies the string passed as argument.
void EncodeWellknownURI(std::string* uri) {
    for (auto& ns : NamespacesByPrefix()) {
        if (uri->compare(0, ns.second.size(), ns.second) == 0) {
            std::string tmp = ns.first;
            tmp += uri->substr(ns.second.size());
            uri->swap(tmp);
            return;
        }
    }
}

// Unapply prefix substitution for well-known URIs.
// Modifies the string passed as argument.
void DecodeWellknownURI(std::string* uri) {
    for (auto& ns : NamespacesByPrefix()) {
        if (uri->compare(0, ns.first.size(), ns.first) == 0) {
            std::string tmp = ns.second;
            tmp += uri->substr(ns.first.size());
            uri->swap(tmp);
            return;
        }
    }
}

}  // namespace rdf
}  // namespace marmotta

