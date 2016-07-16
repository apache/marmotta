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
#include "rdf_parser.h"
#include <raptor2/raptor2.h>
#include <util/raptor_util.h>
#include <gflags/gflags.h>
#include <glog/logging.h>

DEFINE_int64(parse_buffer_size, 8192, "Size of parse buffer in bytes.");

namespace marmotta {
namespace parser {

Parser::Parser(const rdf::URI& baseUri, Format format)
        : stmt_handler([](const rdf::Statement& stmt) { return true; })
        , ns_handler([](const rdf::Namespace& ns) { return true; })
{
    world = raptor_new_world();
    base  = raptor_new_uri(world, (unsigned char const *) baseUri.getUri().c_str());
    raptor_world_set_log_handler(world, this, raptor_error_handler);

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
        case TRIG:
            parser = raptor_new_parser(world, "trig");
            break;
        case NQUADS:
            parser = raptor_new_parser(world, "nquads");
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
    if (!p->stmt_handler(util::raptor::ConvertStatement(statement))) {
        throw ParseError(p->error);
    };
}


void Parser::raptor_ns_handler(void *user_data, raptor_namespace *nspace) {
    Parser* p = static_cast<Parser*>(user_data);
    if (!p->ns_handler(rdf::Namespace(
            (const char*)raptor_namespace_get_prefix(nspace),
            (const char*)raptor_uri_as_string(raptor_namespace_get_uri(nspace))))) {
        throw ParseError(p->error);
    };
}

void Parser::raptor_error_handler(void *user_data, raptor_log_message* message) {
    Parser* p = static_cast<Parser*>(user_data);
    p->error = std::string("parse error (")
               + std::to_string(message->locator->line) + ":"
               + std::to_string(message->locator->column) + "): "
               + message->text;

    LOG(ERROR) << p->error;
}


void Parser::parse(std::istream &in) {
    if(in) {
        raptor_parser_parse_start(parser, base);

        int status = 0;

        std::unique_ptr<char[]> buffer(new char[FLAGS_parse_buffer_size]);
        while (in.read(buffer.get(), FLAGS_parse_buffer_size)) {
            status = raptor_parser_parse_chunk(
                    parser, (unsigned char const *) buffer.get(), in.gcount(), 0);
            if (status != 0) {
                throw ParseError(error);
            }
        }
        status = raptor_parser_parse_chunk(
                parser, (unsigned char const *) buffer.get(), in.gcount(), 1);
        if (status != 0) {
            throw ParseError(error);
        }
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
    if (name == "nquads" || name == "text/nquads") {
        return NQUADS;
    }
    if (name == "auto" || name == "guess") {
        return GUESS;
    }
    return RDFXML;
}

std::string FormatToString(Format fmt) {
    switch(fmt) {
        case RDFXML:
            return "rdf/xml";
        case RDFA:
            return "text/xhtml+xml";
        case NTRIPLES:
            return "text/n3";
        case TURTLE:
            return "text/turtle";
        case RDFJSON:
            return "application/rdf+json";
        case GUESS:
            return "auto";
        case NQUADS:
            return "text/nquads";
        case TRIG:
            return "text/trig";
    }
    return "";
}
}
}
