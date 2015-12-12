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
#include <raptor2/raptor2.h>
#include <rasqal/rasqal.h>
#include <glog/logging.h>
#include "rasqal_model.h"

namespace marmotta {
namespace sparql {
namespace rasqal {

// Helper macros. Some Rasqal functions copy the input string themselves, others don't.
#define STR(s) (const unsigned char*)s.c_str()
#define CPSTR(s) (const unsigned char*)strdup(s.c_str())

rdf::Resource ConvertResource(rasqal_literal *node) {
    switch (node->type) {
        case RASQAL_LITERAL_URI:
            return rdf::URI(std::string((const char*)raptor_uri_as_string(node->value.uri)));
        case RASQAL_LITERAL_BLANK:
            return rdf::BNode(std::string((const char*)node->string, node->string_len));
        default:
            LOG(INFO) << "Error: unsupported resource type " << node->type;
            return rdf::Resource();
    }
}


rdf::Value ConvertValue(rasqal_literal *node) {
    std::string label((const char*)node->string, node->string_len);
    rdf::Value r;
    char* s;
    switch (node->type) {
        case RASQAL_LITERAL_URI:
            return rdf::URI((const char*)raptor_uri_as_string(node->value.uri));
        case RASQAL_LITERAL_BLANK:
            return rdf::BNode(label);
        case RASQAL_LITERAL_STRING:
            if (node->language) {
                return rdf::StringLiteral(label, node->language);
            } else {
                return rdf::StringLiteral(label);

            }
        case RASQAL_LITERAL_XSD_STRING:
            return rdf::DatatypeLiteral(
                    label, rdf::URI((const char*)raptor_uri_as_string(node->datatype)));
        case RASQAL_LITERAL_BOOLEAN:
            return rdf::DatatypeLiteral(
                    node->value.integer==0?"false":"true",
                    rdf::URI((const char*)raptor_uri_as_string(node->datatype)));
        case RASQAL_LITERAL_INTEGER:
            return rdf::DatatypeLiteral(
                    std::to_string(node->value.integer),
                    rdf::URI((const char*)raptor_uri_as_string(node->datatype)));
        case RASQAL_LITERAL_FLOAT:
        case RASQAL_LITERAL_DOUBLE:
            return rdf::DatatypeLiteral(
                    std::to_string(node->value.floating),
                    rdf::URI((const char*)raptor_uri_as_string(node->datatype)));
        case RASQAL_LITERAL_DECIMAL:
            s = rasqal_xsd_decimal_as_string(node->value.decimal);
            r = rdf::DatatypeLiteral(
                    s, rdf::URI((const char*)raptor_uri_as_string(node->datatype)));
            free(s);
            return std::move(r); // r is an lvalue, explicit move
        case RASQAL_LITERAL_DATETIME:
            s = rasqal_xsd_datetime_to_string(node->value.datetime);
            r = rdf::DatatypeLiteral(
                    s, rdf::URI((const char*)raptor_uri_as_string(node->datatype)));
            free(s);
            return std::move(r); // r is an lvalue, explicit move
        case RASQAL_LITERAL_DATE:
            s = rasqal_xsd_date_to_string(node->value.date);
            r = rdf::DatatypeLiteral(
                    s, rdf::URI((const char*)raptor_uri_as_string(node->datatype)));
            free(s);
            return std::move(r); // r is an lvalue, explicit move
        default:
            LOG(INFO) << "Error: unsupported node type " << node->type;
            return rdf::Value();
    }
}


rdf::URI ConvertURI(rasqal_literal *node) {
    switch (node->type) {
        case RASQAL_LITERAL_URI:
            return rdf::URI((const char*)raptor_uri_as_string(node->value.uri));
        default:
            return rdf::URI();
    }
}


rdf::Statement ConvertStatement(rasqal_triple *triple) {
    if (triple->flags == RASQAL_TRIPLE_SPOG) {
        return rdf::Statement(
                ConvertResource(triple->subject),
                ConvertURI(triple->predicate),
                ConvertValue(triple->object),
                ConvertResource(triple->origin)
        );
    } else {
        return rdf::Statement(
                ConvertResource(triple->subject),
                ConvertURI(triple->predicate),
                ConvertValue(triple->object)
        );

    }
}

rasqal_literal *AsStringLiteral(rasqal_world* world, const rdf::Value &v) {
    rdf::StringLiteral l(v.getMessage().literal().stringliteral());

    return rasqal_new_string_literal(
            world,
            CPSTR(l.getContent()),
            strdup(l.getLanguage().c_str()),
            nullptr,
            nullptr);
}

rasqal_literal *AsDatatypeLiteral(rasqal_world* world, const rdf::Value &v) {
    raptor_world* raptorWorld = rasqal_world_get_raptor(world);
    rdf::DatatypeLiteral l(v.getMessage().literal().dataliteral());

    return rasqal_new_string_literal(
            world,
            CPSTR(l.getContent()),
            nullptr,
            raptor_new_uri(raptorWorld, STR(l.getDatatype().stringValue())),
            nullptr);
}

rasqal_literal *AsLiteral(rasqal_world* world, const rdf::Resource &r) {
    raptor_world* raptorWorld = rasqal_world_get_raptor(world);
    switch (r.type) {
        case rdf::Resource::URI:
            return rasqal_new_uri_literal(
                    world,
                    raptor_new_uri(raptorWorld, STR(r.stringValue())));
        case rdf::Resource::BNODE:
            return rasqal_new_simple_literal(
                    world, RASQAL_LITERAL_BLANK, CPSTR(r.stringValue()));
        default:
            return nullptr;
    }
}

rasqal_literal *AsLiteral(rasqal_world* world, const rdf::Value &v) {
    raptor_world* raptorWorld = rasqal_world_get_raptor(world);
    switch (v.type) {
        case rdf::Value::URI:
            return rasqal_new_uri_literal(
                    world, raptor_new_uri(raptorWorld, STR(v.stringValue())));
        case rdf::Value::BNODE:
            return rasqal_new_simple_literal(
                    world, RASQAL_LITERAL_BLANK, CPSTR(v.stringValue()));
        case rdf::Value::STRING_LITERAL:
            return AsStringLiteral(world, v);
        case rdf::Value::DATATYPE_LITERAL:
            return AsDatatypeLiteral(world, v);
        default:
            return nullptr;
    }
}

rasqal_literal *AsLiteral(rasqal_world* world, const rdf::URI &u) {
    raptor_world* raptorWorld = rasqal_world_get_raptor(world);
    return rasqal_new_uri_literal(
            world, raptor_new_uri(raptorWorld, STR(u.stringValue())));
}
}  // namespace rasqal
}  // namespace sparql
}  // namespace marmotta

