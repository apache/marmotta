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
#include "persistence/base_persistence.h"

#include <cstring>

#include "model/rdf_namespaces.h"
#include "model/rdf_operators.h"
#include "util/murmur3.h"

using marmotta::rdf::proto::Statement;

namespace marmotta {
namespace persistence {
namespace {
inline bool computeKey(const std::string* s, char* result) {
    // 128bit keys, use murmur
    if (s != nullptr) {
#ifdef __x86_64__
        MurmurHash3_x64_128(s->data(), s->size(), 13, result);
#else
        MurmurHash3_x86_128(s->data(), s->size(), 13, result);
#endif
        return true;
    } else {
        return false;
    }
}

inline bool computeKey(
        const google::protobuf::Message& msg, bool enabled, char* result) {
    if (enabled) {
        std::string s;
        msg.SerializeToString(&s);
        return computeKey(&s, result);
    }
    return false;
}

inline void copyKey(const char* hash, bool enabled, int base, char* dest) {
    if (enabled)
        memcpy(dest, hash, kKeyLength);
    else
        memset(dest, base, kKeyLength);
}
}

Key::Key(const std::string* s, const std::string* p,
         const std::string* o, const std::string* c)
        : sEnabled(computeKey(s, sHash)), pEnabled(computeKey(p, pHash))
        , oEnabled(computeKey(o, oHash)), cEnabled(computeKey(c, cHash)) {
}

Key::Key(const rdf::proto::Statement& stmt)
        : sEnabled(computeKey(stmt.subject(), stmt.has_subject(), sHash))
        , pEnabled(computeKey(stmt.predicate(), stmt.has_predicate(), pHash))
        , oEnabled(computeKey(stmt.object(), stmt.has_object(), oHash))
        , cEnabled(computeKey(stmt.context(), stmt.has_context(), cHash)) {
}

char* Key::Create(IndexTypes type, BoundTypes bound) const {
    char* result = new char[kKeyLength * 4];
    memset(result, 0x00, kKeyLength);

    int base = 0x00;

    switch (bound) {
        case LOWER:
            base = 0x00;
            break;
        case UPPER:
            base = 0xFF;
            break;
    }

    switch (type) {
        case SPOC:
            copyKey(sHash, sEnabled, base, result);
            copyKey(pHash, pEnabled, base, &result[kKeyLength]);
            copyKey(oHash, oEnabled, base, &result[2 * kKeyLength]);
            copyKey(cHash, cEnabled, base, &result[3 * kKeyLength]);
            break;
        case CSPO:
            copyKey(cHash, cEnabled, base, result);
            copyKey(sHash, sEnabled, base, &result[kKeyLength]);
            copyKey(pHash, pEnabled, base, &result[2 * kKeyLength]);
            copyKey(oHash, oEnabled, base, &result[3 * kKeyLength]);
            break;
        case OPSC:
            copyKey(oHash, oEnabled, base, result);
            copyKey(pHash, pEnabled, base, &result[kKeyLength]);
            copyKey(sHash, sEnabled, base, &result[2 * kKeyLength]);
            copyKey(cHash, cEnabled, base, &result[3 * kKeyLength]);
            break;
        case PCOS:
            copyKey(pHash, pEnabled, base, result);
            copyKey(cHash, cEnabled, base, &result[kKeyLength]);
            copyKey(oHash, oEnabled, base, &result[2 * kKeyLength]);
            copyKey(sHash, sEnabled, base, &result[3 * kKeyLength]);
            break;
    }
    return result;
}



Pattern::Pattern(const Statement& pattern) : key_(pattern), needsFilter_(true) {

    if (pattern.has_subject()) {
        // Subject is usually most selective, so if it is present use the
        // subject-based databases first.
        if (pattern.has_context()) {
            type_ = CSPO;
        } else {
            type_ = SPOC;
        }

        // Filter needed if there is no predicate but an object.
        needsFilter_ = !(pattern.has_predicate()) && pattern.has_object();
    } else if (pattern.has_object()) {
        // Second-best option is object.
        type_ = OPSC;

        // Filter needed if there is a context (subject already checked, predicate irrelevant).
        needsFilter_ = pattern.has_context();
    } else if (pattern.has_predicate()) {
        // Predicate is usually least selective.
        type_ = PCOS;

        // No filter needed, object and subject are not set.
        needsFilter_ = false;
    } else if (pattern.has_context()) {
        type_ = CSPO;

        // No filter needed, subject, predicate object are not set.
        needsFilter_ = false;
    } else {
        // Fall back to SPOC.
        type_ = SPOC;

        // No filter needed, we just scan from the beginning.
        needsFilter_ = false;
    }
}

/**
 * Return the lower key for querying the index (range [MinKey,MaxKey) ).
 */
char* Pattern::MinKey() const {
    return key_.Create(Type(), LOWER);
}

/**
 * Return the upper key for querying the index (range [MinKey,MaxKey) ).
 */
char* Pattern::MaxKey() const {
    return key_.Create(Type(), UPPER);
}


// Return true if the statement matches the pattern. Wildcards (empty fields)
// in the pattern are ignored.
bool Matches(const Statement& pattern, const Statement& stmt) {
    // equality operators defined in rdf_model.h
    if (pattern.has_context() && stmt.context() != pattern.context()) {
        return false;
    }
    if (pattern.has_subject() && stmt.subject() != pattern.subject()) {
        return false;
    }
    if (pattern.has_predicate() && stmt.predicate() != pattern.predicate()) {
        return false;
    }
    return !(pattern.has_object() && stmt.object() != pattern.object());
}
}  // namespace persistence
}  // namespace marmotta

