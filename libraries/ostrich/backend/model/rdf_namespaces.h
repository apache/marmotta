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
#ifndef MARMOTTA_RDF_NAMESPACES_H
#define MARMOTTA_RDF_NAMESPACES_H

#include <map>
#include <string>

#include "model/rdf_model.h"

// Contains maps of well-known default namespaces.
namespace marmotta {
namespace rdf {

using NsMap = std::map<std::string, std::string>;

// Return a map from namespace prefix name (including ":") to
// namespace URI.
const NsMap& NamespacesByPrefix();

// Apply prefix substitution for well-known URIs to save disk space.
// Modifies the string passed as argument.
void EncodeWellknownURI(std::string* uri,
                        const NsMap & namespaces = NamespacesByPrefix());

// Apply prefix substitution for well-known URIs to save disk space.
// Replaces the uri string of the URI with the encoded one
inline void EncodeWellknownURI(proto::URI* value,
                               const NsMap & namespaces = NamespacesByPrefix()){
    EncodeWellknownURI(value->mutable_uri(), namespaces);
}

// Apply prefix substitution for well-known URIs to save disk space.
// Replaces the uri string of the type URI with the encoded one
inline void EncodeWellknownURI(proto::DatatypeLiteral* value,
                               const NsMap & namespaces = NamespacesByPrefix()) {
    EncodeWellknownURI(value->mutable_datatype(), namespaces);
}

// Apply prefix substitution for well-known URIs to save disk space.
// Cases:
// - value is a URI: replace the uri string with the encoded one
// - otherwise: do nothing
inline void EncodeWellknownURI(proto::Resource* value,
                               const NsMap & namespaces = NamespacesByPrefix()) {
    if (value->has_uri()) {
        EncodeWellknownURI(value->mutable_uri(), namespaces);
    }
}

// Apply prefix substitution for well-known URIs to save disk space.
// Cases:
// - value is a URI: replace the uri string with the encoded one
// - value is a DatatypeLiteral: replace type URI with encoded one
// - otherwise: do nothing
inline void EncodeWellknownURI(proto::Value* value,
                               const NsMap & namespaces = NamespacesByPrefix()) {
    if (value->has_resource()) {
        EncodeWellknownURI(value->mutable_resource(), namespaces);
    } else if (value->has_literal() && value->mutable_literal()->has_dataliteral()) {
        EncodeWellknownURI(value->mutable_literal()->mutable_dataliteral(), namespaces);
    }
}

// Apply prefix substitution for well-known URIs to save disk space.
// Performs prefix substitution for subject, predicate, object and context.
inline void EncodeWellknownURI(proto::Statement* stmt,
                               const NsMap & namespaces = NamespacesByPrefix()) {
    if (stmt->has_subject()) {
        EncodeWellknownURI(stmt->mutable_subject(), namespaces);
    }
    if (stmt->has_predicate()) {
        EncodeWellknownURI(stmt->mutable_predicate(), namespaces);
    }
    if (stmt->has_object()) {
        EncodeWellknownURI(stmt->mutable_object(), namespaces);
    }
    if (stmt->has_context()) {
        EncodeWellknownURI(stmt->mutable_context(), namespaces);
    }
}

// Compatibility placeholder, does nothing for namespaces.
inline void EncodeWellknownURI(proto::Namespace* ns) {}

// Unapply prefix substitution for well-known URIs.
// Modifies the string passed as argument.
void DecodeWellknownURI(std::string* uri,
                        const NsMap & namespaces = NamespacesByPrefix());

// Unapply prefix substitution for well-known URIs.
// Replaces the uri string of the URI with the decoded one
inline void DecodeWellknownURI(proto::URI* value,
                               const NsMap & namespaces = NamespacesByPrefix()){
    DecodeWellknownURI(value->mutable_uri(), namespaces);
}

// Unapply prefix substitution for well-known URIs.
// Replaces the uri string of the type URI with the decoded one
inline void DecodeWellknownURI(proto::DatatypeLiteral* value,
                               const NsMap & namespaces = NamespacesByPrefix()) {
    DecodeWellknownURI(value->mutable_datatype(), namespaces);
}

// Unapply prefix substitution for well-known URIs.
// Cases:
// - value is a URI: replace the uri string with the decoded one
// - otherwise: do nothing
inline void DecodeWellknownURI(proto::Resource* value,
                               const NsMap & namespaces = NamespacesByPrefix()) {
    if (value->has_uri()) {
        DecodeWellknownURI(value->mutable_uri(), namespaces);
    }
}

// Unapply prefix substitution for well-known URIs.
// Cases:
// - value is a URI: replace the uri string with the decoded one
// - value is a DatatypeLiteral: replace type URI with decoded one
// - otherwise: do nothing
inline void DecodeWellknownURI(proto::Value* value,
                               const NsMap & namespaces = NamespacesByPrefix()) {
    if (value->has_resource()) {
        DecodeWellknownURI(value->mutable_resource(), namespaces);
    } else if (value->has_literal() && value->mutable_literal()->has_dataliteral()) {
        DecodeWellknownURI(value->mutable_literal()->mutable_dataliteral(), namespaces);
    }
}

// Apply prefix substitution for well-known URIs to save disk space.
// Performs prefix substitution for subject, predicate, object and context.
inline void DecodeWellknownURI(proto::Statement* stmt,
                               const NsMap & namespaces = NamespacesByPrefix()) {
    if (stmt->has_subject()) {
        DecodeWellknownURI(stmt->mutable_subject(), namespaces);
    }
    if (stmt->has_predicate()) {
        DecodeWellknownURI(stmt->mutable_predicate(), namespaces);
    }
    if (stmt->has_object()) {
        DecodeWellknownURI(stmt->mutable_object(), namespaces);
    }
    if (stmt->has_context()) {
        DecodeWellknownURI(stmt->mutable_context(), namespaces);
    }
}

// Compatibility placeholder, does nothing for namespaces.
inline void DecodeWellknownURI(proto::Namespace* ns) {}

}  // namespace rdf
}  // namespace marmotta

#endif //MARMOTTA_RDF_NAMESPACES_H
