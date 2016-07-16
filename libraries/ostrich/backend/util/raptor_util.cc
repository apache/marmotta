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
#include "raptor_util.h"
#include <gflags/gflags.h>
#include <glog/logging.h>

DEFINE_bool(parse_check_utf8, false, "Validate UTF-8 in string literals.");


namespace marmotta {
namespace util {
namespace raptor {

// Helper macros. Some Rasqal functions copy the input string themselves, others don't.
#define STR(s) (const unsigned char*)s.c_str()
#define CPSTR(s) (const unsigned char*)strdup(s.c_str())

rdf::Resource ConvertResource(raptor_term *node) {
    if (node == nullptr) {
        return rdf::Resource();
    }

    switch (node->type) {
        case RAPTOR_TERM_TYPE_URI:
            return rdf::URI(std::string((const char*)raptor_uri_as_string(node->value.uri)));
        case RAPTOR_TERM_TYPE_BLANK:
            return rdf::BNode(std::string((const char*)node->value.blank.string,
                                          node->value.blank.string_len));
        default:
            LOG(INFO) << "Error: unsupported resource type " << node->type;
            return rdf::Resource();
    }
}


rdf::Value ConvertValue(raptor_term *node) {
    if (node == nullptr) {
        return rdf::Value();
    }

    switch (node->type) {
        case RAPTOR_TERM_TYPE_URI:
            return rdf::URI((const char*)raptor_uri_as_string(node->value.uri));
        case RAPTOR_TERM_TYPE_BLANK:
            return rdf::BNode(std::string((const char*)node->value.blank.string,
                                          node->value.blank.string_len));
        case RAPTOR_TERM_TYPE_LITERAL:
            if (FLAGS_parse_check_utf8) {
                if (!google::protobuf::internal::IsStructurallyValidUTF8(
                        (const char *) node->value.literal.string, node->value.literal.string_len)) {
                    LOG(WARNING) << "Invalid UTF8 in literal content, skipping";
                    return rdf::Value();
                }
            }
            if(node->value.literal.language != nullptr) {
                return rdf::StringLiteral(
                        std::string((const char*)node->value.literal.string, node->value.literal.string_len),
                        std::string((const char*)node->value.literal.language, node->value.literal.language_len)
                );
            } else if(node->value.literal.datatype != nullptr) {
                return rdf::DatatypeLiteral(
                        std::string((const char*)node->value.literal.string, node->value.literal.string_len),
                        rdf::URI((const char*)raptor_uri_as_string(node->value.literal.datatype))
                );
            } else {
                return rdf::StringLiteral(
                        std::string((const char*)node->value.literal.string, node->value.literal.string_len)
                );
            }
        default:
            LOG(WARNING) << "Error: unsupported node type " << node->type;
            return rdf::Value();
    }
}


rdf::URI ConvertURI(raptor_term *node) {
    if (node == nullptr) {
        return rdf::URI();
    }

    switch (node->type) {
        case RAPTOR_TERM_TYPE_URI:
            return rdf::URI((const char*)raptor_uri_as_string(node->value.uri));
        default:
            return rdf::URI();
    }
}


rdf::Statement ConvertStatement(raptor_statement *triple) {
    if (triple->graph != nullptr) {
        return rdf::Statement(
                ConvertResource(triple->subject),
                ConvertURI(triple->predicate),
                ConvertValue(triple->object),
                ConvertResource(triple->graph)
        );
    } else {
        return rdf::Statement(
                ConvertResource(triple->subject),
                ConvertURI(triple->predicate),
                ConvertValue(triple->object)
        );

    }
}

namespace {
raptor_term *AsStringLiteral(raptor_world* world, const rdf::Value &v) {
    rdf::StringLiteral l(v.getMessage().literal().stringliteral());

    return raptor_new_term_from_counted_literal(
            world,
            STR(l.getContent()), l.getContent().size(),
            nullptr,
            STR(l.getLanguage()), l.getLanguage().size());
}

raptor_term *AsDatatypeLiteral(raptor_world* world, const rdf::Value &v) {
    rdf::DatatypeLiteral l(v.getMessage().literal().dataliteral());

    return raptor_new_term_from_counted_literal(
            world,
            STR(l.getContent()), l.getContent().size(),
            raptor_new_uri(world, STR(l.getDatatype().getUri())),
            (unsigned char const *) "", 0);
}
}  // namespace


/*
 * Convert a Marmotta Resource into a raptor term.
 */
raptor_term* AsTerm(raptor_world* world, const rdf::Resource& r) {
    switch (r.type) {
        case rdf::Resource::URI:
            return raptor_new_term_from_uri_string(world, STR(r.stringValue()));
        case rdf::Resource::BNODE:
            return raptor_new_term_from_blank(world, STR(r.stringValue()));
        default:
            return nullptr;
    }
}

/*
 * Convert a Marmotta Value into a raptor term.
 */
raptor_term* AsTerm(raptor_world* world, const rdf::Value& v) {
    switch (v.type) {
        case rdf::Value::URI:
            return raptor_new_term_from_uri_string(world, STR(v.stringValue()));
        case rdf::Value::BNODE:
            return raptor_new_term_from_blank(world, STR(v.stringValue()));
        case rdf::Value::STRING_LITERAL:
            return AsStringLiteral(world, v);
        case rdf::Value::DATATYPE_LITERAL:
            return AsDatatypeLiteral(world, v);
        default:
            return nullptr;
    }

}

/*
 * Convert a Marmotta URI into a raptor term.
 */
raptor_term* AsTerm(raptor_world* world, const rdf::URI& u) {
    return raptor_new_term_from_uri_string(world, STR(u.stringValue()));
}

}  // namespace raptor
}  // namespace util
}  // namespace marmotta

