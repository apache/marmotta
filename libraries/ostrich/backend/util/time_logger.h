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
#ifndef MARMOTTA_TIME_LOGGER_H
#define MARMOTTA_TIME_LOGGER_H

#include <string>
#include <chrono>

namespace marmotta {
namespace util {

/**
 * A time logger, writes a logging message when initialised and timing
 * information when destructed.
 */
class TimeLogger {
 public:
    TimeLogger(const std::string& name);

    ~TimeLogger();

 private:
    std::string name_;
    std::chrono::time_point<std::chrono::steady_clock> start_;
};

}  // namespace util
}  // namespace marmotta

#endif //MARMOTTA_TIME_LOGGER_H
