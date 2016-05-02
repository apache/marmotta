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
#ifndef MARMOTTA_RDF_OPERATORS_H
#define MARMOTTA_RDF_OPERATORS_H

#include "model/rdf_model.h"

namespace marmotta {
namespace rdf {
namespace proto {

inline bool operator==(const Namespace &lhs, const Namespace &rhs) {
    return lhs.uri() == rhs.uri();
}

inline bool operator!=(const Namespace &lhs, const Namespace &rhs) {
    return lhs.uri() != rhs.uri();
}

inline bool operator==(const URI &lhs, const URI &rhs) {
    return lhs.uri() == rhs.uri();
}

inline bool operator!=(const URI &lhs, const URI &rhs) {
    return lhs.uri() != rhs.uri();
}

inline bool operator==(const BNode &lhs, const BNode &rhs) {
    return lhs.id() == rhs.id();
}

inline bool operator!=(const BNode &lhs, const BNode &rhs) {
    return lhs.id() != rhs.id();
}


inline bool operator==(const StringLiteral &lhs, const StringLiteral &rhs) {
    return lhs.content() == rhs.content() && lhs.language() == rhs.language();
}

inline bool operator!=(const StringLiteral &lhs, const StringLiteral &rhs) {
    return lhs.content() != rhs.content() || lhs.language() != rhs.language();
}

inline bool operator==(const DatatypeLiteral &lhs, const DatatypeLiteral &rhs) {
    return lhs.content() == rhs.content() && lhs.datatype().uri() == rhs.datatype().uri();
}

inline bool operator!=(const DatatypeLiteral &lhs, const DatatypeLiteral &rhs) {
    return lhs.content() != rhs.content() || lhs.datatype().uri() != rhs.datatype().uri();
}

bool operator==(const Value &lhs, const Value &rhs);

inline bool operator!=(const Value &lhs, const Value &rhs) {
    return !operator==(lhs,rhs);
};


bool operator==(const Resource &lhs, const Resource &rhs);

inline bool operator!=(const Resource &lhs, const Resource &rhs) {
    return !operator==(lhs,rhs);
};

bool operator==(const Statement &lhs, const Statement &rhs);

inline bool operator!=(const Statement &lhs, const Statement &rhs) {
    return !operator==(lhs,rhs);
};


}  // namespace proto


inline bool operator==(const Namespace &lhs, const Namespace &rhs) {
    return lhs.getMessage() == rhs.getMessage();
}

inline bool operator!=(const Namespace &lhs, const Namespace &rhs) {
    return !operator==(lhs,rhs);
}

inline bool operator==(const URI &lhs, const URI &rhs) {
    return lhs.getMessage() == rhs.getMessage();
}

inline bool operator!=(const URI &lhs, const URI &rhs) {
    return !operator==(lhs,rhs);
}

inline bool operator==(const BNode &lhs, const BNode &rhs) {
    return lhs.getMessage() == rhs.getMessage();
}

inline bool operator!=(const BNode &lhs, const BNode &rhs) {
    return !operator==(lhs,rhs);
}

inline bool operator==(const StringLiteral &lhs, const StringLiteral &rhs) {
    return lhs.getMessage() == rhs.getMessage();
}

inline bool operator!=(const StringLiteral &lhs, const StringLiteral &rhs) {
    return !operator==(lhs,rhs);
}

inline bool operator==(const DatatypeLiteral &lhs, const DatatypeLiteral &rhs) {
    return lhs.getMessage() == rhs.getMessage();
}

inline bool operator!=(const DatatypeLiteral &lhs, const DatatypeLiteral &rhs) {
    return !operator==(lhs,rhs);
}

inline bool operator==(const Value &lhs, const Value &rhs) {
    return lhs.getMessage() == rhs.getMessage();
}

inline bool operator!=(const Value &lhs, const Value &rhs) {
    return !operator==(lhs,rhs);
}

inline bool operator==(const Resource &lhs, const Resource &rhs) {
    return lhs.getMessage() == rhs.getMessage();
}

inline bool operator!=(const Resource &lhs, const Resource &rhs) {
    return !operator==(lhs,rhs);
}

inline bool operator==(const Statement &lhs, const Statement &rhs) {
    return lhs.getMessage() == rhs.getMessage();
}

inline bool operator!=(const Statement &lhs, const Statement &rhs) {
    return !operator==(lhs,rhs);
}

}  // namespace rdf
}  // namespace marmotta

namespace std {

// Define std::hash specializations for our proto messages. Note that this generic
// computation serializes the message and is therefore expensive. Consider using
// specialised implementations instead.
template<>
struct hash<google::protobuf::Message> {
    std::size_t operator()(const google::protobuf::Message &k) const {
        std::string content;
        k.SerializeToString(&content);
        return std::hash<string>()(content);
    }
};

// Hash implementation for URIs. Uses a faster implementation than the generic
// proto message version.
template<>
struct hash<marmotta::rdf::proto::URI> {
    std::size_t operator()(const marmotta::rdf::proto::URI &k) const {
        return std::hash<std::string>()(k.uri());
    }
};

// Hash implementation for BNodes. Uses a faster implementation than the generic
// proto message version.
template<>
struct hash<marmotta::rdf::proto::BNode> {
    std::size_t operator()(const marmotta::rdf::proto::BNode &k) const {
        return std::hash<std::string>()(k.id());
    }
};

// Hash implementation for Resources. Uses a faster implementation than the generic
// proto message version.
template<>
struct hash<marmotta::rdf::proto::Resource> {
    std::size_t operator()(const marmotta::rdf::proto::Resource &k) const {
        if (k.has_uri()) {
            return std::hash<marmotta::rdf::proto::URI>()(k.uri());
        } else if (k.has_bnode()) {
            return std::hash<marmotta::rdf::proto::BNode>()(k.bnode());
        }
        return std::hash<google::protobuf::Message>()(k);
    }
};

template<>
struct hash<marmotta::rdf::proto::Value> {
    std::size_t operator()(const marmotta::rdf::proto::Value &k) const {
        return std::hash<google::protobuf::Message>()(k);
    }
};

template<>
struct hash<marmotta::rdf::proto::StringLiteral> {
    std::size_t operator()(const marmotta::rdf::proto::StringLiteral &k) const {
        return std::hash<google::protobuf::Message>()(k);
    }
};

template<>
struct hash<marmotta::rdf::proto::DatatypeLiteral> {
    std::size_t operator()(const marmotta::rdf::proto::DatatypeLiteral &k) const {
        return std::hash<google::protobuf::Message>()(k);
    }
};

template<>
struct hash<marmotta::rdf::proto::Statement> {
    std::size_t operator()(const marmotta::rdf::proto::Statement &k) const {
        return std::hash<google::protobuf::Message>()(k);
    }
};

template<>
struct hash<marmotta::rdf::proto::Namespace> {
    std::size_t operator()(const marmotta::rdf::proto::Namespace &k) const {
        return std::hash<google::protobuf::Message>()(k);
    }
};
}  // namespace std

#endif //MARMOTTA_RDF_OPERATORS_H
