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
#ifndef MARMOTTA_RASQAL_ADAPTER_H
#define MARMOTTA_RASQAL_ADAPTER_H

#include <memory>
#include <rasqal/rasqal.h>

#include "model/rdf_model.h"
#include "util/iterator.h"

namespace marmotta {
namespace sparql {

using StatementIterator = util::CloseableIterator<rdf::Statement>;

/**
 * An abstract superclass for more easily interfacing from the C++ Marmotta model
 * with C-based Rasqal.
 */
class TripleSource {
 public:

    /**
     * Check for presence of a complete statement.
     *
     * Parameters with nullptr value are interpreted as wildcards.
     */
    virtual bool HasStatement(
            const rdf::Resource* s, const rdf::URI* p, const rdf::Value* o,
            const rdf::Resource* c) = 0;

    /**
     * Return an iterator over statements matching the given subject, predicate,
     * object and context. The caller takes ownership of the pointer.
     *
     * Parameters with nullptr value are interpreted as wildcards.
     */
    virtual std::unique_ptr<StatementIterator> GetStatements(
            const rdf::Resource* s, const rdf::URI* p, const rdf::Value* o,
            const rdf::Resource* c) = 0;
};

class SparqlException : public std::exception {
 public:

    SparqlException(const std::string &message, const std::string &query) : message(message), query(query) { }

    const char *what() const noexcept override {
        return message.c_str();
    }

 private:
    std::string message;
    std::string query;
};

/**
 * Class SparqlService provides a SPARQL wrapper around a triple source using
 * Rasqal.
 */
class SparqlService {
 public:
    using RowType = std::map<std::string, rdf::Value>;

    SparqlService(std::unique_ptr<TripleSource> source);

    /**
     * Free any C-style resources, particularly the rasqal world.
     */
    ~SparqlService();

    void TupleQuery(const std::string& query, std::function<bool(const RowType&)> row_handler);

    /**
     * Return a reference to the triple source managed by this service.
     */
    TripleSource& Source() {
        return *source;
    }

 private:
    std::unique_ptr<TripleSource> source;

    rasqal_world* world;
};

}  // namespace sparql
}  // namespace marmotta


#endif //MARMOTTA_RASQAL_ADAPTER_H
