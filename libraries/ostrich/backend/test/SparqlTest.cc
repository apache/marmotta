//
// Created by wastl on 09.12.15.
//
#include <glog/logging.h>
#include "gtest/gtest.h"
#include "sparql/rasqal_adapter.h"
#include "model/rdf_operators.h"

namespace marmotta {
namespace sparql {

namespace {

const rdf::URI base_uri("http://example.com/");


using MockStatementIterator = util::CollectionIterator<rdf::Statement>;

class MockTripleSource : public TripleSource {

 public:
    MockTripleSource(std::vector<rdf::Statement> statements)
            : statements(statements) { }

    bool HasStatement(const rdf::Resource *s, const rdf::URI *p, const rdf::Value *o, const rdf::Resource *c) override {
        for (const auto& stmt : statements) {
            bool match = true;
            if (s != nullptr && *s != stmt.getSubject()) {
                match = false;
            }
            if (p != nullptr && *p != stmt.getPredicate()) {
                match = false;
            }
            if (o != nullptr && *o != stmt.getObject()) {
                match = false;
            }
            if (c != nullptr && *c != stmt.getContext()) {
                match = false;
            }
            if (!match) {
                return false;
            }
        }
        return false;
    }

    std::unique_ptr<StatementIterator> GetStatements(const rdf::Resource *s, const rdf::URI *p,
                                                             const rdf::Value *o, const rdf::Resource *c) override {
        std::vector<rdf::Statement> results;
        for (const auto& stmt : statements) {
            bool match = true;
            if (s != nullptr && *s != stmt.getSubject()) {
                match = false;
            }
            if (p != nullptr && *p != stmt.getPredicate()) {
                match = false;
            }
            if (o != nullptr && *o != stmt.getObject()) {
                match = false;
            }
            if (c != nullptr && *c != stmt.getContext()) {
                match = false;
            }
            if (match) {
                results.push_back(stmt);
            }
        }
        return std::unique_ptr<StatementIterator>(new MockStatementIterator(results));
    }

 private:
    std::vector<rdf::Statement> statements;

};
}  // namespace


TEST(SPARQLTest, Simple) {
    SparqlService svc(std::unique_ptr<TripleSource>(new MockTripleSource(
            {
                    rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"), rdf::URI("http://example.com/o1"))
            }
    )));

    int count = 0;
    rdf::Value s, p, o;
    svc.TupleQuery("SELECT * WHERE {?s ?p ?o}", base_uri,
                   [&](const SparqlService::RowType& row) {
        count++;
        s = row.at("s");
        p = row.at("p");
        o = row.at("o");

        return true;
    });

    EXPECT_EQ(1, count);
    EXPECT_EQ("http://example.com/s1", s.stringValue());
    EXPECT_EQ("http://example.com/p1", p.stringValue());
    EXPECT_EQ("http://example.com/o1", o.stringValue());
}

TEST(SPARQLTest, SubjectPattern) {
    SparqlService svc(std::unique_ptr<TripleSource>(new MockTripleSource(
            {
                    rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"), rdf::URI("http://example.com/o1")),
                    rdf::Statement(rdf::URI("http://example.com/s2"), rdf::URI("http://example.com/p2"), rdf::URI("http://example.com/o2"))
            }
    )));

    int count = 0;
    rdf::Value p, o;
    svc.TupleQuery("SELECT * WHERE {<http://example.com/s1> ?p ?o}", base_uri,
                   [&](const SparqlService::RowType& row) {
        count++;
        p = row.at("p");
        o = row.at("o");

        return true;
    });

    EXPECT_EQ(1, count);
    EXPECT_EQ("http://example.com/p1", p.stringValue());
    EXPECT_EQ("http://example.com/o1", o.stringValue());
}

TEST(SPARQLTest, PredicatePattern) {
    SparqlService svc(std::unique_ptr<TripleSource>(new MockTripleSource(
            {
                    rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"), rdf::URI("http://example.com/o1")),
                    rdf::Statement(rdf::URI("http://example.com/s2"), rdf::URI("http://example.com/p2"), rdf::URI("http://example.com/o2"))
            }
    )));

    int count = 0;
    rdf::Value s, o;
    svc.TupleQuery("SELECT * WHERE {?s <http://example.com/p1> ?o}", base_uri,
                   [&](const SparqlService::RowType& row) {
        count++;
        s = row.at("s");
        o = row.at("o");

        return true;
    });

    EXPECT_EQ(1, count);
    EXPECT_EQ("http://example.com/s1", s.stringValue());
    EXPECT_EQ("http://example.com/o1", o.stringValue());
}

TEST(SPARQLTest, ObjectPattern) {
    SparqlService svc(std::unique_ptr<TripleSource>(new MockTripleSource(
            {
                    rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"), rdf::URI("http://example.com/o1")),
                    rdf::Statement(rdf::URI("http://example.com/s2"), rdf::URI("http://example.com/p2"), rdf::URI("http://example.com/o2"))
            }
    )));

    int count = 0;
    rdf::Value s, p;
    svc.TupleQuery("SELECT * WHERE {?s ?p <http://example.com/o1>}", base_uri,
                   [&](const SparqlService::RowType& row) {
        count++;
        s = row.at("s");
        p = row.at("p");

        return true;
    });

    EXPECT_EQ(1, count);
    EXPECT_EQ("http://example.com/p1", p.stringValue());
    EXPECT_EQ("http://example.com/s1", s.stringValue());
}

TEST(SPARQLTest, BNode) {
    SparqlService svc(std::unique_ptr<TripleSource>(new MockTripleSource(
            {
                    rdf::Statement(rdf::BNode("n1"), rdf::URI("http://example.com/p1"), rdf::URI("http://example.com/o1")),
                    rdf::Statement(rdf::BNode("n2"), rdf::URI("http://example.com/p2"), rdf::URI("http://example.com/o2"))
            }
    )));

    int count = 0;
    rdf::Value s, p;
    svc.TupleQuery("SELECT * WHERE {?s ?p <http://example.com/o1>}", base_uri,
                   [&](const SparqlService::RowType& row) {
        count++;
        s = row.at("s");
        p = row.at("p");

        return true;
    });

    EXPECT_EQ(1, count);
    EXPECT_EQ("http://example.com/p1", p.stringValue());
    EXPECT_EQ("n1", s.stringValue());
}

TEST(SPARQLTest, Filter) {
    SparqlService svc(std::unique_ptr<TripleSource>(new MockTripleSource(
            {
                    rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"), rdf::URI("http://example.com/o1")),
                    rdf::Statement(rdf::URI("http://example.com/s2"), rdf::URI("http://example.com/p2"), rdf::URI("http://example.com/o2"))
            }
    )));

    int count = 0;
    rdf::Value s, p, o;
    svc.TupleQuery("SELECT * WHERE {?s ?p ?o . FILTER(?o = <http://example.com/o1>)}", base_uri,
                   [&](const SparqlService::RowType& row) {
        count++;
        s = row.at("s");
        p = row.at("p");
        o = row.at("o");

        return true;
    });

    EXPECT_EQ(1, count);
    EXPECT_EQ("http://example.com/p1", p.stringValue());
    EXPECT_EQ("http://example.com/s1", s.stringValue());
    EXPECT_EQ("http://example.com/o1", o.stringValue());
}

TEST(SPARQLTest, Join) {
    SparqlService svc(std::unique_ptr<TripleSource>(new MockTripleSource(
            {
                    rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"), rdf::URI("http://example.com/o1")),
                    rdf::Statement(rdf::URI("http://example.com/o1"), rdf::URI("http://example.com/p2"), rdf::URI("http://example.com/o2"))
            }
    )));

    int count = 0;
    rdf::Value s, o;
    svc.TupleQuery("SELECT * WHERE {?s ?p1 ?o1 . ?o1 ?p2 ?o }", base_uri,
                   [&](const SparqlService::RowType& row) {
        count++;
        s = row.at("s");
        o = row.at("o");

        return true;
    });

    EXPECT_EQ(1, count);
    EXPECT_EQ("http://example.com/s1", s.stringValue());
    EXPECT_EQ("http://example.com/o2", o.stringValue());
}

TEST(SPARQLTest, Graph) {
    rdf::Statement stmt = rdf::Statement(rdf::URI("http://example.com/s1"),
                                         rdf::URI("http://example.com/p1"),
                                         rdf::URI("http://example.com/o1"));
    SparqlService svc(std::unique_ptr<TripleSource>(new MockTripleSource({stmt})));

    int count = 0;
    rdf::Value s, p, o;
    svc.GraphQuery("CONSTRUCT { ?s ?p ?o . } WHERE {?s ?p ?o}", base_uri,
                   [&](const rdf::Statement& row) {
        count++;

        EXPECT_EQ(stmt, row);

        return true;
    });

    EXPECT_EQ(1, count);
}


TEST(SPARQLTest, AskTrue) {
    rdf::Statement stmt = rdf::Statement(rdf::URI("http://example.com/s1"),
                                         rdf::URI("http://example.com/p1"),
                                         rdf::URI("http://example.com/o1"));
    SparqlService svc(std::unique_ptr<TripleSource>(new MockTripleSource({stmt})));

    EXPECT_TRUE(svc.AskQuery("ASK {}", base_uri));
}


TEST(SPARQLTest, AskFalse) {
    rdf::Statement stmt = rdf::Statement(rdf::URI("http://example.com/s1"),
                                         rdf::URI("http://example.com/p1"),
                                         rdf::URI("http://example.com/o1"));
    SparqlService svc(std::unique_ptr<TripleSource>(new MockTripleSource({stmt})));

    EXPECT_FALSE(svc.AskQuery("ASK { <http://example.com/s2> ?p ?o}", base_uri));
}


}  // namespace sparql
}  // namespace marmotta