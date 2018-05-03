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
#include "serializer_raptor.h"
#include <raptor2/raptor2.h>
#include <util/raptor_util.h>

#define STR(s) (const unsigned char*)s.c_str()
#define CPSTR(s) (const unsigned char*)strdup(s.c_str())

namespace marmotta {
namespace serializer {

namespace {
static int std_iostream_write_byte(void *context, const int byte) {
    std::ostream *out = (std::ostream *) context;
    out->write((char const *) &byte, 1);
    if (*out) {
        return 0;
    } else {
        return 1;
    }
}

static int std_iostream_write_bytes(void *context, const void *ptr, size_t size, size_t nmemb) {
    std::ostream *out = (std::ostream *) context;
    out->write((char const *) ptr, size * nmemb);
    if (*out) {
        return 0;
    } else {
        return 1;
    }
}

static int std_iostream_read_bytes(void *context, void *ptr, size_t size, size_t nmemb) {
    std::istream *in = (std::istream *) context;

    if (!*in) {
        return -1;
    }

    in->read((char *) ptr, size * nmemb);
    return (int) in->gcount();
}

static int std_iostream_read_eof(void *context) {
    std::istream *in = (std::istream *) context;

    if (in->eof()) {
        return 1;
    } else {
        return 0;
    }
}

const raptor_iostream_handler raptor_handler = {
        2, NULL, NULL,
        &std_iostream_write_byte, &std_iostream_write_bytes, NULL,
        &std_iostream_read_bytes, &std_iostream_read_eof
};


inline std::string raptorFormat(Format format) {
    switch (format) {
        case Format::RDFXML:
            return "rdfxml";
        case Format::RDFXML_ABBREV:
            return "rdfxml-abbrev";
        case Format::GRAPHVIZ:
            return "dot";
        case Format::NQUADS:
            return "nquads";
        case Format::NTRIPLES:
            return "ntriples";
        case Format::TURTLE:
            return "turtle";
        case Format::RDFJSON:
            return "json";
        case Format::SPARQL_JSON:
            return "json-triples";
        default:
            throw SerializationError("RDF Serializer: unsupported format");
    }
}
}  // namespace

RaptorSerializer::RaptorSerializer(const rdf::URI& baseUri, Format format)
        : SerializerBase(baseUri, format) {

    world = raptor_new_world();
    base  = raptor_new_uri(world, STR(baseUri.getUri()));
    initRaptor();
}

RaptorSerializer::RaptorSerializer(const rdf::URI& baseUri, Format format, std::vector<rdf::Namespace> namespaces)
        : SerializerBase(baseUri, format, namespaces) {

    world = raptor_new_world();
    base  = raptor_new_uri(world, STR(baseUri.getUri()));
    initRaptor();
}

RaptorSerializer::RaptorSerializer(const rdf::URI& baseUri, Format format, std::map<std::string, rdf::URI> namespaces)
        : SerializerBase(baseUri, format, namespaces) {

    world = raptor_new_world();
    base  = raptor_new_uri(world, STR(baseUri.getUri()));
    initRaptor();
}


RaptorSerializer::~RaptorSerializer() {
    // check for NULL in case a move operation has set the fields to a null pointer
    if(serializer != nullptr)
        raptor_free_serializer(serializer);

    if(base != nullptr)
        raptor_free_uri(base);

    if(world != nullptr)
        raptor_free_world(world);

}


void RaptorSerializer::initRaptor() {
    serializer = raptor_new_serializer(world, raptorFormat(format).c_str());
    for(const auto &e : namespaces) {
        raptor_uri* uri = raptor_new_uri(world, STR(e.second.getUri()));
        raptor_serializer_set_namespace(serializer, uri, CPSTR(e.first));
    }
    raptor_world_set_log_handler(world, this, [](void *user_data, raptor_log_message* message){
        std::cerr << message->level << ": " << message->text << std::endl;
    });
}

void RaptorSerializer::prepare(std::ostream &out) {
    stream = raptor_new_iostream_from_handler(world, &out, &raptor_handler);
    raptor_serializer_start_to_iostream(serializer, base, stream);
}

void RaptorSerializer::serialize(const rdf::Statement &stmt) {
    raptor_statement* triple = raptor_new_statement(world);

    triple->subject   = util::raptor::AsTerm(world, stmt.getSubject());
    triple->predicate = util::raptor::AsTerm(world, stmt.getPredicate());
    triple->object    = util::raptor::AsTerm(world, stmt.getObject());
    triple->graph     = util::raptor::AsTerm(world, stmt.getContext());

    raptor_serializer_serialize_statement(serializer, triple);

    raptor_free_statement(triple);
}

void RaptorSerializer::close() {
    raptor_serializer_serialize_end(serializer);
    raptor_free_iostream(stream);
}

}  // namespace serializer
}  // namespace marmotta
