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
#include "gtest/gtest.h"
#include "model/rdf_model.h"
#include "model/rdf_operators.h"
#include "model/rdf_namespaces.h"

namespace marmotta {
namespace rdf {

TEST(NamespaceTest, EncodeURI) {
    std::string uri1 = "http://www.w3.org/2002/07/owl#sameAs";
    std::string uri2 = "http://marmotta.apache.org/test/uri1";

    EncodeWellknownURI(&uri1);
    EXPECT_EQ("owl:sameAs", uri1);

    EncodeWellknownURI(&uri2);
    EXPECT_EQ("http://marmotta.apache.org/test/uri1", uri2);
}

TEST(NamespaceTest, EncodeURIProto) {
    rdf::URI uri1 = "http://www.w3.org/2002/07/owl#sameAs";
    rdf::URI uri2 = "http://marmotta.apache.org/test/uri1";

    rdf::proto::URI msg1 = uri1.getMessage();
    rdf::proto::URI msg2 = uri2.getMessage();

    EncodeWellknownURI(&msg1);
    EXPECT_EQ("owl:sameAs", msg1.uri());

    EncodeWellknownURI(&msg2);
    EXPECT_EQ("http://marmotta.apache.org/test/uri1", msg2.uri());
}


TEST(NamespaceTest, DecodeURI) {
    std::string uri1 = "owl:sameAs";
    std::string uri2 = "http://marmotta.apache.org/test/uri1";

    DecodeWellknownURI(&uri1);
    EXPECT_EQ("http://www.w3.org/2002/07/owl#sameAs", uri1);

    DecodeWellknownURI(&uri2);
    EXPECT_EQ("http://marmotta.apache.org/test/uri1", uri2);
}

TEST(NamespaceTest, DecodeURIProto) {
    rdf::URI uri1 = "owl:sameAs";
    rdf::URI uri2 = "http://marmotta.apache.org/test/uri1";

    rdf::proto::URI msg1 = uri1.getMessage();
    rdf::proto::URI msg2 = uri2.getMessage();

    DecodeWellknownURI(&msg1);
    EXPECT_EQ("http://www.w3.org/2002/07/owl#sameAs", msg1.uri());

    DecodeWellknownURI(&msg2);
    EXPECT_EQ("http://marmotta.apache.org/test/uri1", msg2.uri());
}

}  // namespace rdf
}  // namespace marmotta