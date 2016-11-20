//
// Created by wastl on 18.04.15.
//

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