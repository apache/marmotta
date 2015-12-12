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
#include "serializer_proto.h"

#include <google/protobuf/text_format.h>
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream.h>


namespace marmotta {
namespace serializer {

void ProtoSerializer::prepare(std::ostream &out) {
    out_ = new google::protobuf::io::OstreamOutputStream(&out);
}

void ProtoSerializer::serialize(const rdf::Statement &stmt) {
    stmts_.add_statement()->MergeFrom(stmt.getMessage());
}

void ProtoSerializer::close() {
    google::protobuf::io::CodedOutputStream* coded_output =
            new google::protobuf::io::CodedOutputStream(out_);
    switch (format) {
        case PROTO:
            stmts_.SerializeToCodedStream(coded_output);
            break;
        case PROTO_TEXT:
            google::protobuf::TextFormat::Print(
                    stmts_, dynamic_cast<google::protobuf::io::ZeroCopyOutputStream*>(out_));
            break;
    }
    stmts_.Clear();
    delete coded_output;
    delete out_;
}

}  // namespace serializer
}  // namespace marmotta
