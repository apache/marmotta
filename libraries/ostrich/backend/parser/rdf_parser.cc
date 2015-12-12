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
#include "rdf_parser.h"

namespace marmotta {
namespace parser {
Parser::Parser(const rdf::URI& baseUri, Format format)
        : stmt_handler([](const rdf::Statement& stmt) { })
        , ns_handler([](const rdf::Namespace& ns) { })
{
    world = raptor_new_world();
    base  = raptor_new_uri(world, (unsigned char const *) baseUri.getUri().c_str());

    switch (format) {
        case RDFXML:
            parser = raptor_new_parser(world, "rdfxml");
            break;
        case TURTLE:
            parser = raptor_new_parser(world, "turtle");
            break;
        case NTRIPLES:
            parser = raptor_new_parser(world, "ntriples");
            break;
        case RDFA:
            parser = raptor_new_parser(world, "rdfa");
            break;
        case RDFJSON:
            parser = raptor_new_parser(world, "json");
            break;
        case GUESS:
            parser = raptor_new_parser(world, "guess");
            break;
    }

    raptor_parser_set_statement_handler(parser, this, raptor_stmt_handler);
    raptor_parser_set_namespace_handler(parser, this, raptor_ns_handler);
}

Parser::~Parser() {
    raptor_free_parser(parser);
    raptor_free_uri(base);
    raptor_free_world(world);
}


void Parser::raptor_stmt_handler(void *user_data, raptor_statement *statement) {
    Parser* p = static_cast<Parser*>(user_data);

    rdf::Resource subject; rdf::URI predicate; rdf::Value object; rdf::Resource context;
    switch (statement->subject->type) {
        case RAPTOR_TERM_TYPE_URI:
            subject = rdf::URI((const char*)raptor_uri_as_string(statement->subject->value.uri));
            break;
        case RAPTOR_TERM_TYPE_BLANK:
            subject = rdf::BNode(std::string((const char*)statement->subject->value.blank.string, statement->subject->value.blank.string_len));
            break;
        default:
            raptor_parser_parse_abort(p->parser);
            throw ParseError("invalid subject term type");
    }

    switch (statement->predicate->type) {
        case RAPTOR_TERM_TYPE_URI:
            predicate = rdf::URI((const char*)raptor_uri_as_string(statement->predicate->value.uri));
            break;
        default:
            raptor_parser_parse_abort(p->parser);
            throw ParseError("invalid predicate term type");
    }

    switch (statement->object->type) {
        case RAPTOR_TERM_TYPE_URI:
            object = rdf::URI((const char*)raptor_uri_as_string(statement->object->value.uri));
            break;
        case RAPTOR_TERM_TYPE_BLANK:
            object = rdf::BNode(std::string((const char*)statement->object->value.blank.string, statement->object->value.blank.string_len));
            break;
        case RAPTOR_TERM_TYPE_LITERAL:
            if(statement->object->value.literal.language != NULL) {
                object = rdf::StringLiteral(
                        std::string((const char*)statement->object->value.literal.string, statement->object->value.literal.string_len),
                        std::string((const char*)statement->object->value.literal.language, statement->object->value.literal.language_len)
                );
            } else if(statement->object->value.literal.datatype != NULL) {
                object = rdf::DatatypeLiteral(
                        std::string((const char*)statement->object->value.literal.string, statement->object->value.literal.string_len),
                        rdf::URI((const char*)raptor_uri_as_string(statement->object->value.literal.datatype))
                );
            } else {
                object = rdf::StringLiteral(
                        std::string((const char*)statement->object->value.literal.string, statement->object->value.literal.string_len)
                );
            }
            break;
        default:
            raptor_parser_parse_abort(p->parser);
            throw ParseError("invalid object term type");
    }

    if (statement->graph != NULL) {
        switch (statement->graph->type) {
            case RAPTOR_TERM_TYPE_URI:
                context = rdf::URI((const char*)raptor_uri_as_string(statement->graph->value.uri));
                break;
            case RAPTOR_TERM_TYPE_BLANK:
                context = rdf::BNode(std::string((const char*)statement->graph->value.blank.string, statement->graph->value.blank.string_len));
                break;
            default:
                raptor_parser_parse_abort(p->parser);
                throw ParseError("invalid graph term type");
        }
    } else {
        context = rdf::URI();
    }

    p->stmt_handler(rdf::Statement(subject, predicate, object, context));
}


void Parser::raptor_ns_handler(void *user_data, raptor_namespace *nspace) {
    Parser* p = static_cast<Parser*>(user_data);
    p->ns_handler(rdf::Namespace(
            (const char*)raptor_namespace_get_prefix(nspace),
            (const char*)raptor_uri_as_string(raptor_namespace_get_uri(nspace))));
}

void Parser::parse(std::istream &in) {
    if(in) {
        raptor_parser_parse_start(parser, base);

        char buffer[8192];
        while (in.read(buffer, 8192)) {
            raptor_parser_parse_chunk(parser, (unsigned char const *) buffer, in.gcount(), 0);
        }
        raptor_parser_parse_chunk(parser, (unsigned char const *) buffer, in.gcount(), 1);
    }
}

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
    if (name == "json" || name == "application/json" || name == "application/rdf+json") {
        return RDFJSON;
    }
    if (name == "auto" || name == "guess") {
        return GUESS;
    }
    return RDFXML;
}

}
}
