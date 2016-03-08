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
#ifndef MARMOTTA_SPLIT_H
#define MARMOTTA_SPLIT_H

#include <string>
#include <sstream>
#include <vector>

namespace marmotta {
namespace util {

// Split a string at a certain delimiter and add the parts to the vector elems.
std::vector<std::string> &split(const std::string &s, char delim,
                                std::vector<std::string> &elems);

// Split a string, returning a new vector containing the parts.
std::vector<std::string> split(const std::string &s, char delim);

}
}

#endif //MARMOTTA_SPLIT_H
