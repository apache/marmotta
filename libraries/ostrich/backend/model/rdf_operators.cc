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
#include "rdf_operators.h"

namespace marmotta {
namespace rdf {
namespace proto {

bool operator==(const Value &lhs, const Value &rhs) {
    if (lhs.has_resource() && rhs.has_resource()) {
        if (lhs.resource().has_uri() && rhs.resource().has_uri()) {
            return lhs.resource().uri() == rhs.resource().uri();
        } else if (lhs.resource().has_bnode() && rhs.resource().has_bnode()) {
            return lhs.resource().bnode() == rhs.resource().bnode();
        }
    } else if(lhs.has_literal() && rhs.has_literal()) {
        if (lhs.literal().has_stringliteral() && rhs.literal().has_stringliteral()) {
            return lhs.literal().stringliteral() == rhs.literal().stringliteral();
        } else if (lhs.literal().has_dataliteral() && rhs.literal().has_dataliteral()) {
            return lhs.literal().dataliteral() == rhs.literal().dataliteral();
        }
    }
    return false;
}

bool operator==(const Resource &lhs, const Resource &rhs) {
    if (lhs.has_uri() && rhs.has_uri()) {
        return lhs.uri() == rhs.uri();
    } else if (lhs.has_bnode() && rhs.has_bnode()) {
        return lhs.bnode() == rhs.bnode();
    }
    return false;
}

bool operator==(const Statement &lhs, const Statement &rhs) {
    return operator==(lhs.subject(), rhs.subject()) &&
           operator==(lhs.predicate(), rhs.predicate()) &&
           operator==(lhs.object(), rhs.object()) &&
           operator==(lhs.context(), rhs.context());

}


}  // namespace proto
}  // namespace rdf
}  // namespace marmotta
