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
#include <functional>

#include <raptor2/raptor2.h>
#include <rasqal/rasqal.h>
#include <glog/logging.h>
#include <chrono>

#include "sparql/rasqal_adapter.h"
#include "sparql/rasqal_model.h"
#include "util/raptor_util.h"
#include "util/time_logger.h"

// Rasqal notoriously uses unsigned strings, macro to convert C++ strings.
#define STR(s) (const unsigned char*)s.c_str()

namespace marmotta {
namespace sparql {

namespace {

#ifndef NDEBUG
// Format binding names for debugging
std::string formatVariables(rasqal_variable *bindings[4]) {
    std::string result = "(";
    for (int i=0; i<4; i++) {
        if (bindings[i] != nullptr) {
            result += (const char*)bindings[i]->name;
            result += " ";
        } else {
            result += "_ ";
        }
    }
    result += ")";
    return result;
}

std::string formatBindings(const std::map<std::string, rdf::Value>& bindings) {
    std::string result="(";
    for (auto it=bindings.cbegin(); it != bindings.cend(); it++) {
        result += it->first + "=" + it->second.as_turtle() + " ";
    }
    result += ")";
    return result;
}
#endif

void log_handler(void *user_data, raptor_log_message *message) {
    LOG_IF(ERROR, message->code >= 0) << "SPARQL Error(" << message->code << "): " << message->text;
}

// Bind the current statement to the variables configured in the triple match.
rasqal_triple_parts bind_match(
        struct rasqal_triples_match_s *rtm, void *user_data,
        rasqal_variable *bindings[4], rasqal_triple_parts parts) {
    StatementIterator *it = (StatementIterator *) rtm->user_data;
    const rdf::Statement& s = it->next();

    int r = 0;

#ifndef NDEBUG
    DLOG(INFO) << "Binding variables " << formatVariables(bindings) << " for statement " << s.as_turtle();
#endif
    if ((parts & RASQAL_TRIPLE_SUBJECT) != 0) {
        rasqal_variable_set_value(bindings[0], rasqal::AsLiteral(rtm->world, s.getSubject()));
        r |= RASQAL_TRIPLE_SUBJECT;
    }
    if ((parts & RASQAL_TRIPLE_PREDICATE) != 0) {
        rasqal_variable_set_value(bindings[1], rasqal::AsLiteral(rtm->world, s.getPredicate()));
        r |= RASQAL_TRIPLE_PREDICATE;
    }
    if ((parts & RASQAL_TRIPLE_OBJECT) != 0) {
        rasqal_variable_set_value(bindings[2], rasqal::AsLiteral(rtm->world, s.getObject()));
        r |= RASQAL_TRIPLE_OBJECT;
    }
    if ((parts & RASQAL_TRIPLE_ORIGIN) != 0) {
        rasqal_variable_set_value(bindings[3], rasqal::AsLiteral(rtm->world, s.getContext()));
        r |= RASQAL_TRIPLE_ORIGIN;
    }

    return (rasqal_triple_parts) r;
}

// Increment the iterator contained in the triple match user data.
void next_match(struct rasqal_triples_match_s *rtm, void *user_data) {
    DLOG(INFO) << "Next result";
}

// Return true in case the iterator has no next element.
int is_end(struct rasqal_triples_match_s *rtm, void *user_data) {
    StatementIterator *it = (StatementIterator *) rtm->user_data;
    return !it->hasNext();
}

// Delete iterator and make sure its destructors are called in the C++ way.
void finish(struct rasqal_triples_match_s *rtm, void *user_data) {
    DLOG(INFO) << "Finish result iteration.";
    StatementIterator *it = (StatementIterator *) rtm->user_data;
    delete it;
    rtm->user_data = nullptr;
}

// Init a Rasqal triples match using the interator returned by GetStatements()
int init_triples_match(
        rasqal_triples_match *rtm, struct rasqal_triples_source_s *rts,
        void *user_data, rasqal_triple_meta *m, rasqal_triple *t) {
    DLOG(INFO) << "Get statements (exact=" << rtm->is_exact << ", finished=" << rtm->finished << ")";

    SparqlService *self = (SparqlService *) *(void**)user_data;

    optional<rdf::Resource> s;
    optional<rdf::URI> p;
    optional<rdf::Value> o;
    optional<rdf::Resource> c;

    rasqal_variable* var;
    if ((var=rasqal_literal_as_variable(t->subject))) {
        m->bindings[0] = var;
        if (var->value) {
            s = rasqal::ConvertResource(var->value);
        }
    } else {
        s = rasqal::ConvertResource(t->subject);
    }

    if ((var=rasqal_literal_as_variable(t->predicate))) {
        m->bindings[1] = var;
        if (var->value) {
            p = rasqal::ConvertURI(var->value);
        }
    } else {
        p = rasqal::ConvertURI(t->predicate);
    }

    if ((var=rasqal_literal_as_variable(t->object))) {
        m->bindings[2] = var;
        if (var->value) {
            o = rasqal::ConvertValue(var->value);
        }
    } else {
        o = rasqal::ConvertValue(t->object);
    }

    if(t->origin) {
        if ((var=rasqal_literal_as_variable(t->origin))) {
            m->bindings[3] = var;
            if (var->value) {
                c = rasqal::ConvertResource(var->value);
            }
        } else {
            c = rasqal::ConvertResource(t->origin);
        }
    }

    // Store C++ iterator in user_data and take ownership
    auto it = self->Source().GetStatements(s, p, o, c);
    rtm->user_data = it.release();

    rtm->bind_match = bind_match;
    rtm->next_match = next_match;
    rtm->is_end = is_end;
    rtm->finish = finish;

    return 0;
}

// Check for triple presence, using the SparqlService::HasStatement method.
int triple_present(
        struct rasqal_triples_source_s *rts, void *user_data, rasqal_triple *t) {
    DLOG(INFO) << "Check triple";

    optional<rdf::Resource> s = rasqal::ConvertResource(t->subject);
    optional<rdf::URI> p = rasqal::ConvertURI(t->predicate);
    optional<rdf::Value> o = rasqal::ConvertValue(t->object);
    optional<rdf::Resource> c;

    SparqlService *self = (SparqlService *) *(void**)user_data;
    if ((t->flags & RASQAL_TRIPLE_ORIGIN) != 0) {
        c = rasqal::ConvertResource(t->origin);
    }
    return self->Source().HasStatement(s, p, o, c);
}

void free_triples_source(void *user_data) {
    DLOG(INFO) << "Free triples source";
}

// Init a Rasqal triple source, wrapping the Marmotta TripleSource (factory_user_data)
int new_triples_source(rasqal_query* query, void *factory_user_data, void *user_data, rasqal_triples_source* rts) {
    DLOG(INFO) << "Init triples source";

    rts->version = 1;
    rts->init_triples_match = init_triples_match;
    rts->triple_present = triple_present;
    rts->free_triples_source = free_triples_source;
    rts->user_data = (void**)malloc(sizeof(void*));
    *((void**)rts->user_data) = factory_user_data;

    return 0;
}

// Init a Rasqal triple source, wrapping the Marmotta TripleSource (factory_user_data)
int init_triples_source(
        rasqal_query *query, void *factory_user_data, void *user_data,
        rasqal_triples_source *rts, rasqal_triples_error_handler handler) {
    return new_triples_source(query, factory_user_data, user_data, rts);
}

// Init a Rasqal triple factory
int init_factory(rasqal_triples_source_factory *factory) {
    DLOG(INFO) << "Init query factory";
    factory->version = 1;
    factory->new_triples_source = new_triples_source;
    factory->init_triples_source = init_triples_source;
    return 0;
}
}  // namespace


SparqlService::SparqlService(std::unique_ptr<TripleSource> source)
        : source(std::move(source)) {
    // Initialise Rasqal world.
    world = rasqal_new_world();
    rasqal_world_open(world);

    // Redirect logging output to glog.
    rasqal_world_set_log_handler(world, nullptr, log_handler);

    // Register our triple source with Rasqal, providing the relevant wrappers.
    rasqal_set_triples_source_factory(world, &init_factory, this);
}

SparqlService::~SparqlService() {
    rasqal_free_world(world);
}

void SparqlService::TupleQuery(const std::string& query, const rdf::URI& base_uri,
                               std::function<bool(const RowType&)> row_handler) {
    util::TimeLogger timeLogger("SPARQL tuple query");

    auto q = rasqal_new_query(world, "sparql11-query", nullptr);
    auto base = raptor_new_uri(rasqal_world_get_raptor(world),
                               STR(base_uri.getUri()));
    if (rasqal_query_prepare(q, STR(query), base) != 0) {
        raptor_free_uri(base);
        rasqal_free_query(q);
        throw SparqlException("could not parse query", query);
    }

    bool next = true;
    auto r = rasqal_query_execute(q);
    if (r == nullptr) {
        raptor_free_uri(base);
        rasqal_free_query(q);
        throw SparqlException("query execution failed", query);
    }

    if (!rasqal_query_results_is_bindings(r)) {
        rasqal_free_query_results(r);
        rasqal_free_query(q);
        raptor_free_uri(base);
        throw SparqlException("query is not a tuple query", query);
    }

    int rowcount = 0;
    while (next && rasqal_query_results_finished(r) == 0) {
        RowType row;
        for (int i=0; i<rasqal_query_results_get_bindings_count(r); i++) {
            row[(const char*)rasqal_query_results_get_binding_name(r,i)] =
                    rasqal::ConvertValue(rasqal_query_results_get_binding_value(r,i));
        }
#ifndef NDEBUG
        DLOG(INFO) << "Row " << rowcount << ": " << formatBindings(row);
#endif

        next = row_handler(row);
        rasqal_query_results_next(r);

        rowcount++;
    }

    rasqal_free_query_results(r);
    rasqal_free_query(q);
    raptor_free_uri(base);
}


void SparqlService::GraphQuery(const std::string& query, const rdf::URI& base_uri,
                               std::function<bool(const rdf::Statement&)> stmt_handler) {
    util::TimeLogger timeLogger("SPARQL graph query");

    auto q = rasqal_new_query(world, "sparql11-query", nullptr);
    auto base = raptor_new_uri(rasqal_world_get_raptor(world),
                               STR(base_uri.getUri()));
    if (rasqal_query_prepare(q, STR(query), base) != 0) {
        raptor_free_uri(base);
        rasqal_free_query(q);
        throw SparqlException("could not parse query", query);
    }

    bool next = true;
    auto r = rasqal_query_execute(q);
    if (r == nullptr) {
        raptor_free_uri(base);
        rasqal_free_query(q);
        throw SparqlException("Query execution failed", query);
    }

    if (!rasqal_query_results_is_graph(r)) {
        rasqal_free_query_results(r);
        rasqal_free_query(q);
        raptor_free_uri(base);
        throw SparqlException("query is not a graph query", query);
    }

    while (next) {
        next = stmt_handler(util::raptor::ConvertStatement(rasqal_query_results_get_triple(r)))
            && rasqal_query_results_next_triple(r) == 0;
    }

    rasqal_free_query_results(r);
    rasqal_free_query(q);
    raptor_free_uri(base);
}

bool SparqlService::AskQuery(const std::string& query, const rdf::URI& base_uri) {
    util::TimeLogger timeLogger("SPARQL ask query");

    auto q = rasqal_new_query(world, "sparql11-query", nullptr);
    auto base = raptor_new_uri(rasqal_world_get_raptor(world),
                               STR(base_uri.getUri()));
    if (rasqal_query_prepare(q, STR(query), base) != 0) {
        raptor_free_uri(base);
        rasqal_free_query(q);
        throw SparqlException("could not parse query", query);
    }

    auto r = rasqal_query_execute(q);
    if (r == nullptr || rasqal_query_results_get_boolean(r) < 0) {
        raptor_free_uri(base);
        rasqal_free_query(q);
        throw SparqlException("query execution failed", query);
    }

    if (!rasqal_query_results_is_boolean(r)) {
        rasqal_free_query_results(r);
        rasqal_free_query(q);
        raptor_free_uri(base);
        throw SparqlException("query is not a boolean query", query);
    }

    bool result = rasqal_query_results_get_boolean(r) > 0;

    rasqal_free_query_results(r);
    rasqal_free_query(q);
    raptor_free_uri(base);

    return result;
}


}  // namespace sparql
}  // namespace marmotta

