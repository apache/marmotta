//
// Created by wastl on 18.04.15.
//

#include "gtest.h"
#include "model/rdf_model.h"
#include "model/rdf_operators.h"

namespace marmotta {

TEST(URITest, Construct) {
    rdf::URI uri1("http://www.example.com/U1");
    rdf::URI uri2(std::string("http://www.example.com/U2"));

    EXPECT_EQ(uri1.getUri(), "http://www.example.com/U1");
    EXPECT_EQ(uri2.getUri(), "http://www.example.com/U2");
}

TEST(URITest, Equality) {
    rdf::URI uri1("http://www.example.com/U1");
    rdf::URI uri2("http://www.example.com/U1");
    rdf::URI uri3("http://www.example.com/U3");

    EXPECT_EQ(uri1, uri2);
    EXPECT_NE(uri1, uri3);
}

TEST(URITest, ProtoEquality) {
    rdf::URI uri1("http://www.example.com/U1");
    rdf::URI uri2("http://www.example.com/U1");
    rdf::URI uri3("http://www.example.com/U3");

    EXPECT_EQ(uri1.getMessage(), uri2.getMessage());
    EXPECT_NE(uri1.getMessage(), uri3.getMessage());
}

TEST(BNodeTest, Construct) {
    rdf::BNode bNode1("n1");
    rdf::BNode bNode2(std::string("n2"));

    EXPECT_EQ(bNode1.getId(), "n1");
    EXPECT_EQ(bNode2.getId(), "n2");
}

TEST(BNodeTest, Equality) {
    rdf::BNode bNode1("n1");
    rdf::BNode bNode2("n1");
    rdf::BNode bNode3("n3");

    EXPECT_EQ(bNode1, bNode2);
    EXPECT_NE(bNode1, bNode3);
}

TEST(BNodeTest, ProtoEquality) {
    rdf::BNode bNode1("n1");
    rdf::BNode bNode2("n1");
    rdf::BNode bNode3("n3");

    EXPECT_EQ(bNode1.getMessage(), bNode2.getMessage());
    EXPECT_NE(bNode1.getMessage(), bNode3.getMessage());
}

TEST(StringLiteralTest, Construct) {
    rdf::StringLiteral l1("Hello, World!");
    rdf::StringLiteral l2("Hello, World!", "en");
    rdf::StringLiteral l3(std::string("Hello, World!"));

    EXPECT_EQ(l1.getContent(), "Hello, World!");
    EXPECT_EQ(l1.getLanguage(), "");

    EXPECT_EQ(l2.getContent(), "Hello, World!");
    EXPECT_EQ(l2.getLanguage(), "en");

    EXPECT_EQ(l3.getContent(), "Hello, World!");
    EXPECT_EQ(l3.getLanguage(), "");
}

TEST(StringLiteralTest, Equality) {
    rdf::StringLiteral l1("Hello, World!");
    rdf::StringLiteral l2("Hello, World!");
    rdf::StringLiteral l3("Hello, World!", "en");
    rdf::StringLiteral l4("The quick brown fox jumps over the lazy dog.");

    EXPECT_EQ(l1, l2);
    EXPECT_NE(l1, l3);
    EXPECT_NE(l1, l4);
}

TEST(StringLiteralTest, ProtoEquality) {
    rdf::StringLiteral l1("Hello, World!");
    rdf::StringLiteral l2("Hello, World!");
    rdf::StringLiteral l3("Hello, World!", "en");
    rdf::StringLiteral l4("The quick brown fox jumps over the lazy dog.");

    EXPECT_EQ(l1.getMessage(), l2.getMessage());
    EXPECT_NE(l1.getMessage(), l3.getMessage());
    EXPECT_NE(l1.getMessage(), l4.getMessage());
}

TEST(ValueTest, Construct) {
    rdf::Value v1(rdf::URI("http://www.example.com/U1"));
    rdf::Value v2(rdf::BNode("n1"));
    rdf::Value v3(rdf::StringLiteral("Hello, World!"));
    
    EXPECT_EQ(v1.stringValue(), "http://www.example.com/U1");
    EXPECT_EQ(v2.stringValue(), "n1");
    EXPECT_EQ(v3.stringValue(), "Hello, World!");
}

TEST(ValueTest, Equality) {
    rdf::Value v1(rdf::URI("http://www.example.com/U1"));
    rdf::Value v2(rdf::URI("http://www.example.com/U1"));
    rdf::Value v3(rdf::BNode("n3"));

    EXPECT_EQ(v1, v2);
    EXPECT_NE(v1, v3);
}

TEST(ValueTest, ProtoEquality) {
    rdf::Value v1(rdf::URI("http://www.example.com/U1"));
    rdf::Value v2(rdf::URI("http://www.example.com/U1"));
    rdf::Value v3(rdf::BNode("n3"));

    EXPECT_EQ(v1.getMessage(), v2.getMessage());
    EXPECT_NE(v1.getMessage(), v3.getMessage());
}

TEST(StatementTest, Construct) {
    rdf::Statement s(rdf::URI("http://www.example.com/S1"), rdf::URI("http://www.example.com/P1"), "Hello World!");

    EXPECT_EQ(s.getSubject(), "http://www.example.com/S1");
    EXPECT_EQ(s.getPredicate(), "http://www.example.com/P1");
    EXPECT_EQ(s.getObject(), "Hello World!");
}
}