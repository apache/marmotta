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
#include <new>

#include "rdf_model.h"

namespace marmotta {
namespace rdf {

static std::string as_turtle_(const proto::URI& uri) {
    return "<" + uri.uri() + ">";
}

static std::string as_turtle_(const proto::BNode& bnode) {
    return "_:" + bnode.id();
}

static std::string as_turtle_(const proto::StringLiteral& literal) {
    if (literal.language() == "") {
        return "\"" + literal.content() + "\"";
    } else {
        return "\"" + literal.content() + "\"@" + literal.language();
    }
}

static std::string as_turtle_(const proto::DatatypeLiteral& literal) {
    return "\"" + literal.content() + "\"^^" + as_turtle_(literal.datatype());
}

static std::string as_turtle_(const proto::Resource& resource) {
    if (resource.has_uri()) {
        return as_turtle_(resource.uri());
    }
    if (resource.has_bnode()) {
        return as_turtle_(resource.bnode());
    }
    return "";
}

static std::string as_turtle_(const proto::Value& value) {
    if (value.has_resource()) {
        if (value.resource().has_uri()) {
            return as_turtle_(value.resource().uri());
        }
        if (value.resource().has_bnode()) {
            return as_turtle_(value.resource().bnode());
        }
    }
    if (value.has_literal()) {
        if (value.literal().has_stringliteral()) {
            return as_turtle_(value.literal().stringliteral());
        }
        if (value.literal().has_dataliteral()) {
            return as_turtle_(value.literal().dataliteral());
        }
    }
    return "";
}

std::string URI::as_turtle() const {
    return as_turtle_(internal_);
}

std::string BNode::as_turtle() const {
    return as_turtle_(internal_);
}

std::string StringLiteral::as_turtle() const {
    return as_turtle_(internal_);
}

std::string DatatypeLiteral::as_turtle() const {
    return as_turtle_(internal_);
}



std::string Resource::stringValue() const {
    switch (type) {
        case URI:
            return internal_.uri().uri();
        case BNODE:
            return internal_.bnode().id();
        default:
            return "";
    }
}

std::string Resource::as_turtle() const {
    return as_turtle_(internal_);
}


Value &Value::operator=(const marmotta::rdf::URI &_uri) {
    type = URI;
    internal_.mutable_resource()->mutable_uri()->MergeFrom(_uri.getMessage());
    return *this;
}


Value &Value::operator=(const BNode &_bnode) {
    type = BNODE;
    internal_.mutable_resource()->mutable_bnode()->MergeFrom(_bnode.getMessage());
    return *this;
}

Value &Value::operator=(const StringLiteral &literal) {
    type = STRING_LITERAL;
    internal_.mutable_literal()->mutable_stringliteral()->MergeFrom(literal.getMessage());
    return *this;
}

Value &Value::operator=(const DatatypeLiteral &literal) {
    type = DATATYPE_LITERAL;
    internal_.mutable_literal()->mutable_dataliteral()->MergeFrom(literal.getMessage());
    return *this;
}

Value &Value::operator=(marmotta::rdf::URI &&_uri) {
    type = URI;
    internal_.mutable_resource()->mutable_uri()->Swap(&_uri.internal_);
    return *this;
}


Value &Value::operator=(BNode &&_bnode) {
    type = BNODE;
    internal_.mutable_resource()->mutable_bnode()->Swap(&_bnode.internal_);
    return *this;
}

Value &Value::operator=(StringLiteral &&literal) {
    type = STRING_LITERAL;
    internal_.mutable_literal()->mutable_stringliteral()->Swap(&literal.internal_);
    return *this;
}

Value &Value::operator=(DatatypeLiteral &&literal) {
    type = DATATYPE_LITERAL;
    internal_.mutable_literal()->mutable_dataliteral()->Swap(&literal.internal_);
    return *this;
}

std::string Value::stringValue() const {
    switch (type) {
        case URI:
            return internal_.resource().uri().uri();
        case BNODE:
            return internal_.resource().bnode().id();
        case STRING_LITERAL:
            return internal_.literal().stringliteral().content();
        case DATATYPE_LITERAL:
            return internal_.literal().dataliteral().content();
        default:
            return "";
    }
}

std::string Value::as_turtle() const {
    return as_turtle_(internal_);
}


std::string Statement::as_turtle() const {
    if (hasContext()) {
        return as_turtle_(internal_.context()) + " { " +
               as_turtle_(internal_.subject()) + " " +
               as_turtle_(internal_.predicate()) + " " +
               as_turtle_(internal_.object()) + ". }";
    } else {
        return as_turtle_(internal_.subject()) + " " +
               as_turtle_(internal_.predicate()) + " " +
               as_turtle_(internal_.object()) + ".";
    }
}

Value::Value(const proto::Value& v) : internal_(v) {
    if (v.has_resource()) {
        if (v.resource().has_uri())
            type = URI;
        else
            type = BNODE;
    } else if (v.has_literal()) {
        if (v.literal().has_stringliteral())
            type = STRING_LITERAL;
        else
            type = DATATYPE_LITERAL;
    } else {
        type = NONE;
    }
}

Value::Value(proto::Value&& v) {
    if (v.has_resource()) {
        if (v.resource().has_uri())
            type = URI;
        else
            type = BNODE;
    } else if (v.has_literal()) {
        if (v.literal().has_stringliteral())
            type = STRING_LITERAL;
        else
            type = DATATYPE_LITERAL;
    } else {
        type = NONE;
    }
    internal_.Swap(&v);
}

Resource::Resource(const proto::Resource& v) : internal_(v) {
    if (v.has_uri())
        type = URI;
    else if (v.has_bnode())
        type = BNODE;
    else
        type = NONE;
}

Resource::Resource(proto::Resource&& v) {
    if (v.has_uri())
        type = URI;
    else if (v.has_bnode())
        type = BNODE;
    else
        type = NONE;
    internal_.Swap(&v);
}

Resource &Resource::operator=(const rdf::URI &uri) {
    type = URI;
    internal_.mutable_uri()->MergeFrom(uri.getMessage());
    return *this;
}

Resource &Resource::operator=(const rdf::BNode &bnode) {
    type = BNODE;
    internal_.mutable_bnode()->MergeFrom(bnode.getMessage());
    return *this;
}

Resource &Resource::operator=(rdf::URI &&uri) {
    type = URI;
    internal_.mutable_uri()->Swap(&uri.internal_);
    return *this;
}

Resource &Resource::operator=(rdf::BNode &&bnode) {
    type = BNODE;
    internal_.mutable_bnode()->Swap(&bnode.internal_);
    return *this;
}

URI &URI::operator=(proto::URI &&other) {
    internal_.Swap(&other);
    return *this;
}

URI &URI::operator=(const URI &other) {
    internal_.MergeFrom(other.internal_);
    return *this;
}

URI &URI::operator=(URI &&other) {
    internal_.Swap(&other.internal_);
    return *this;
}

BNode &BNode::operator=(proto::BNode &&other) {
    internal_.Swap(&other);
    return *this;
}

BNode &BNode::operator=(const BNode &other) {
    internal_.MergeFrom(other.internal_);
    return *this;
}

BNode &BNode::operator=(BNode &&other) {
    internal_.Swap(&other.internal_);
    return *this;
}

StringLiteral &StringLiteral::operator=(proto::StringLiteral &&other) {
    internal_.Swap(&other);
    return *this;
}

StringLiteral &StringLiteral::operator=(const StringLiteral &other) {
    internal_.MergeFrom(other.internal_);
    return *this;
}

StringLiteral &StringLiteral::operator=(StringLiteral &&other) {
    internal_.Swap(&other.internal_);
    return *this;
}

DatatypeLiteral &DatatypeLiteral::operator=(proto::DatatypeLiteral &&other) {
    internal_.Swap(&other);
    return *this;
}

DatatypeLiteral &DatatypeLiteral::operator=(const DatatypeLiteral &other) {
    internal_.MergeFrom(other.internal_);
    return *this;
}

DatatypeLiteral &DatatypeLiteral::operator=(DatatypeLiteral &&other) {
    internal_.Swap(&other.internal_);
    return *this;
}

Statement &Statement::operator=(const proto::Statement &other) {
    internal_.MergeFrom(other);
    return *this;
}

Statement &Statement::operator=(proto::Statement &&other) {
    internal_.Swap(&other);
    return *this;
}

Statement &Statement::operator=(const Statement &other) {
    internal_.MergeFrom(other.internal_);
    return *this;
}

Statement &Statement::operator=(Statement &&other) {
    internal_.Swap(&other.internal_);
    return *this;
}
}  // namespace rdf
}  // namespace marmotta
