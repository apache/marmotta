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
#ifndef MARMOTTA_RAPTOR_MODEL_H
#define MARMOTTA_RAPTOR_MODEL_H

#include <memory>
#include <raptor2/raptor2.h>

#include "model/rdf_model.h"

namespace marmotta {
namespace util {
namespace raptor {

/*
 * Convert a raptor term into a Marmotta Resource. Returns empty in case
 * the node cannot be converted.
 */
rdf::Resource ConvertResource(raptor_term* node);

/*
 * Convert a raptor term into a Marmotta Value. Returns empty in case
 * the node cannot be converted.
 */
rdf::Value ConvertValue(raptor_term* node);

/*
 * Convert a raptor term into a Marmotta URI. Returns empty in case
 * the node cannot be converted.
 */
rdf::URI ConvertURI(raptor_term* node);

/*
 * Convert a raptor triple into a Marmotta Statement. Returns empty in case
 * the node cannot be converted.
 */
rdf::Statement ConvertStatement(raptor_statement* triple);

/*
 * Convert a Marmotta Resource into a raptor term.
 */
raptor_term* AsTerm(raptor_world* world, const rdf::Resource& r);

/*
 * Convert a Marmotta Value into a raptor term.
 */
raptor_term* AsTerm(raptor_world* world, const rdf::Value& v);

/*
 * Convert a Marmotta URI into a raptor term.
 */
raptor_term* AsTerm(raptor_world* world, const rdf::URI& u);

}  // namespace raptor
}  // namespace util
}  // namespace marmotta


#endif //MARMOTTA_RAPTOR_MODEL_H
