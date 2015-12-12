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
#ifndef MARMOTTA_RASQAL_MODEL_H
#define MARMOTTA_RASQAL_MODEL_H

#include <memory>
#include <rasqal/rasqal.h>

#include "model/rdf_model.h"

namespace marmotta {
namespace sparql {
namespace rasqal {

/*
 * Convert a rasqal literal into a Marmotta Resource. Returns empty in case
 * the node cannot be converted.
 */
rdf::Resource ConvertResource(rasqal_literal* node);

/*
 * Convert a rasqal literal into a Marmotta Value. Returns empty in case
 * the node cannot be converted.
 */
rdf::Value ConvertValue(rasqal_literal* node);

/*
 * Convert a rasqal literal into a Marmotta URI. Returns empty in case
 * the node cannot be converted.
 */
rdf::URI ConvertURI(rasqal_literal* node);

/*
 * Convert a rasqal triple into a Marmotta Statement. Returns empty in case
 * the node cannot be converted.
 */
rdf::Statement ConvertStatement(rasqal_triple* triple);

/*
 * Convert a Marmotta Resource into a rasqal literal.
 */
rasqal_literal* AsLiteral(rasqal_world* world, const rdf::Resource& r);

/*
 * Convert a Marmotta Value into a rasqal literal.
 */
rasqal_literal* AsLiteral(rasqal_world* world, const rdf::Value& v);

/*
 * Convert a Marmotta URI into a rasqal literal.
 */
rasqal_literal* AsLiteral(rasqal_world* world, const rdf::URI& u);


}  // namespace rasqal
}  // namespace sparql
}  // namespace marmotta

#endif //MARMOTTA_RASQAL_MODEL_H
