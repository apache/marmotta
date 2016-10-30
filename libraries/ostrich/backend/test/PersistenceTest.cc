//
// Created by wastl on 24.08.16.
//
#include "persistence/base_persistence.h"

#include "gtest/gtest.h"
#include "gmock/gmock.h"

#include "model/rdf_operators.h"

namespace marmotta {
namespace persistence {
namespace test {

bool keysEqual(const char* key1, const char* key2) {
    return memcmp(key1, key2, 4 * kKeyLength) == 0;
}

bool keysNotEqual(const char* key1, const char* key2) {
    return memcmp(key1, key2, 4 * kKeyLength) != 0;
}

bool lessThan(const char* key1, const char* key2) {
    return memcmp(key1, key2, 4 * kKeyLength) < 0;
}

// Test that the keys generated for different statements are also different.
TEST(KeyTest, StatementsDiffer) {
    rdf::Statement stmt1(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"),
                         rdf::URI("http://example.com/o1"));
    rdf::Statement stmt2(rdf::URI("http://example.com/s2"), rdf::URI("http://example.com/p2"),
                         rdf::URI("http://example.com/o2"));

    Key key1(stmt1.getMessage());
    Key key2(stmt2.getMessage());

    for (auto t : {SPOC, CSPO, OPSC, PCOS}) {
        char* k1 = key1.Create(t);
        char* k2 = key2.Create(t);
        EXPECT_PRED2(keysNotEqual, k1, k2);
        delete[] k1;
        delete[] k2;
    }
}

// Test that the upper and lower bound of a range over context are different.
TEST(KeyTest, BoundsDiffer) {
    rdf::Statement stmt(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"),
                         rdf::URI("http://example.com/o1"));

    Key key(stmt.getMessage());

    for (auto t : {SPOC, CSPO, OPSC, PCOS}) {
        char* k1 = key.Create(t, LOWER);
        char* k2 = key.Create(t, UPPER);
        EXPECT_PRED2(keysNotEqual, k1, k2);
        EXPECT_PRED2(lessThan, k1, k2);
        delete[] k1;
        delete[] k2;
    }
}

TEST(URITest, EncodeURI) {
    std::string uri1 = "http://www.w3.org/2002/07/owl#sameAs";
    std::string uri2 = "http://marmotta.apache.org/test/uri1";

    EncodeWellknownURI(&uri1);
    EXPECT_EQ("owl:sameAs", uri1);

    EncodeWellknownURI(&uri2);
    EXPECT_EQ("http://marmotta.apache.org/test/uri1", uri2);
}

TEST(URITest, EncodeURIProto) {
    rdf::URI uri1 = "http://www.w3.org/2002/07/owl#sameAs";
    rdf::URI uri2 = "http://marmotta.apache.org/test/uri1";

    rdf::proto::URI msg1 = uri1.getMessage();
    rdf::proto::URI msg2 = uri2.getMessage();

    EncodeWellknownURI(&msg1);
    EXPECT_EQ("owl:sameAs", msg1.uri());

    EncodeWellknownURI(&msg2);
    EXPECT_EQ("http://marmotta.apache.org/test/uri1", msg2.uri());
}


TEST(URITest, DecodeURI) {
    std::string uri1 = "owl:sameAs";
    std::string uri2 = "http://marmotta.apache.org/test/uri1";

    DecodeWellknownURI(&uri1);
    EXPECT_EQ("http://www.w3.org/2002/07/owl#sameAs", uri1);

    DecodeWellknownURI(&uri2);
    EXPECT_EQ("http://marmotta.apache.org/test/uri1", uri2);
}

TEST(URITest, DecodeURIProto) {
    rdf::URI uri1 = "owl:sameAs";
    rdf::URI uri2 = "http://marmotta.apache.org/test/uri1";

    rdf::proto::URI msg1 = uri1.getMessage();
    rdf::proto::URI msg2 = uri2.getMessage();

    DecodeWellknownURI(&msg1);
    EXPECT_EQ("http://www.w3.org/2002/07/owl#sameAs", msg1.uri());

    DecodeWellknownURI(&msg2);
    EXPECT_EQ("http://marmotta.apache.org/test/uri1", msg2.uri());
}


}  // namespace test
}  // namespace persistence
}  // namespace marmotta
