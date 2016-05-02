//
// Created by wastl on 19.12.15.
//
#include <cstdlib>
#include <vector>

#include <glog/logging.h>

#include "gtest/gtest.h"
#include "gmock/gmock.h"
#include "boost/filesystem.hpp"

#include "util/iterator.h"
#include "model/rdf_operators.h"
#include "persistence/leveldb_persistence.h"

using namespace boost::filesystem;

using testing::Contains;

namespace marmotta {
namespace rdf {
namespace proto {
std::ostream& operator<<(std::ostream& out, const Statement& stmt) {
    out << rdf::Statement(stmt).as_turtle();
    return out;
}
}
}

namespace persistence {
namespace {


class PersistenceTest : public ::testing::Test {
 protected:
    PersistenceTest() {
        testdir = temp_directory_path()/unique_path();
        create_directory(testdir);

        LOG(INFO) << "Test DB Path: " << testdir.string();

        db = new LevelDBPersistence(testdir.string(), 10 * 1048576);
    }

    ~PersistenceTest() {
        LOG(INFO) << "Destroying Test DB: " << testdir.string();
        delete db;
        remove_all(testdir);
    }

    LevelDBPersistence* db;
    path testdir;
};

TEST_F(PersistenceTest, TestAddNamespaces) {
    std::vector<rdf::proto::Namespace> ns = {
            rdf::Namespace("ex", "http://www.example.com/").getMessage(),
            rdf::Namespace("foo", "http://www.foo.com/").getMessage(),
    };

    util::CollectionIterator<rdf::proto::Namespace> it(ns);
    db->AddNamespaces(it);

    {
        rdf::Namespace pattern;
        pattern.setPrefix("foo");
        auto it = db->GetNamespaces(pattern.getMessage());
        EXPECT_TRUE(it->hasNext());
        EXPECT_EQ(ns[1], it->next());
        EXPECT_FALSE(it->hasNext());
    }

    {
        rdf::Namespace pattern;
        pattern.setPrefix("bar");
        auto it = db->GetNamespaces(pattern.getMessage());
        EXPECT_FALSE(it->hasNext());
    }

    {
        rdf::Namespace pattern;
        pattern.setUri("http://www.example.com/");
        auto it = db->GetNamespaces(pattern.getMessage());
        EXPECT_TRUE(it->hasNext());
        EXPECT_EQ(ns[0], it->next());
        EXPECT_FALSE(it->hasNext());
    }
}


TEST_F(PersistenceTest, TestAddStatements) {
    std::vector<rdf::proto::Statement> stmts = {
            rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"),
                           rdf::URI("http://example.com/o1")).getMessage(),
            rdf::Statement(rdf::URI("http://example.com/s2"), rdf::URI("http://example.com/p2"),
                           rdf::URI("http://example.com/o2")).getMessage()
    };

    util::CollectionIterator<rdf::proto::Statement> it(stmts);
    db->AddStatements(it);

    EXPECT_EQ(2, db->Size());
    for (const auto& stmt : stmts) {
        auto it = db->GetStatements(stmt);
        ASSERT_TRUE(it->hasNext());
        EXPECT_EQ(stmt, it->next());
        EXPECT_FALSE(it->hasNext());
    }
}

// Test pattern queries that can be answered directly by the index.
TEST_F(PersistenceTest, TestGetStatementsIndexed) {
    std::vector<rdf::proto::Statement> stmts = {
            rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"),
                           rdf::URI("http://example.com/o1")).getMessage(),
            rdf::Statement(rdf::URI("http://example.com/s2"), rdf::URI("http://example.com/p1"),
                           rdf::URI("http://example.com/o1")).getMessage(),
            rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p2"),
                           rdf::URI("http://example.com/o2")).getMessage(),
            rdf::Statement(rdf::URI("http://example.com/s2"), rdf::URI("http://example.com/p2"),
                           rdf::URI("http://example.com/o2")).getMessage(),
            rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p3"),
                           rdf::URI("http://example.com/o3")).getMessage(),
    };

    util::CollectionIterator<rdf::proto::Statement> it(stmts);
    db->AddStatements(it);

    EXPECT_EQ(5, db->Size());

    rdf::Statement pattern1;
    pattern1.setSubject(rdf::URI("http://example.com/s1"));
    auto it1 = db->GetStatements(pattern1.getMessage());
    for (int i=0; i<3; i++) {
        ASSERT_TRUE(it1->hasNext());
        EXPECT_THAT(stmts, Contains(it1->next()));
    }
    EXPECT_FALSE(it1->hasNext());

    rdf::Statement pattern2;
    pattern2.setObject(rdf::URI("http://example.com/o1"));
    auto it2 = db->GetStatements(pattern2.getMessage());
    for (int i=0; i<2; i++) {
        ASSERT_TRUE(it2->hasNext());
        EXPECT_THAT(stmts, Contains(it2->next()));
    }
    EXPECT_FALSE(it2->hasNext());

    rdf::Statement pattern3;
    pattern3.setPredicate(rdf::URI("http://example.com/p1"));
    auto it3 = db->GetStatements(pattern3.getMessage());
    for (int i=0; i<2; i++) {
        ASSERT_TRUE(it3->hasNext());
        EXPECT_THAT(stmts, Contains(it3->next()));
    }
    EXPECT_FALSE(it3->hasNext());
}

// Test pattern queries that trigger filtering because the index alone cannot answer these queries.
TEST_F(PersistenceTest, TestGetStatementsFiltered) {
    std::vector<rdf::proto::Statement> stmts = {
            rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"),
                           rdf::URI("http://example.com/o1")).getMessage(),
            rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p2"),
                           rdf::URI("http://example.com/o1")).getMessage(),
            rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p3"),
                           rdf::URI("http://example.com/o1")).getMessage(),
            rdf::Statement(rdf::URI("http://example.com/s2"), rdf::URI("http://example.com/p1"),
                           rdf::URI("http://example.com/o2")).getMessage(),
            rdf::Statement(rdf::URI("http://example.com/s2"), rdf::URI("http://example.com/p2"),
                           rdf::URI("http://example.com/o2")).getMessage(),
    };

    util::CollectionIterator<rdf::proto::Statement> it(stmts);
    db->AddStatements(it);

    EXPECT_EQ(5, db->Size());

    rdf::Statement pattern1;
    pattern1.setSubject(rdf::URI("http://example.com/s1"));
    pattern1.setObject(rdf::URI("http://example.com/o1"));
    auto it1 = db->GetStatements(pattern1.getMessage());
    for (int i=0; i<3; i++) {
        ASSERT_TRUE(it1->hasNext());
        EXPECT_THAT(stmts, Contains(it1->next()));
    }
    EXPECT_FALSE(it1->hasNext());

    rdf::Statement pattern2;
    pattern2.setSubject(rdf::URI("http://example.com/s2"));
    pattern2.setObject(rdf::URI("http://example.com/o2"));
    auto it2 = db->GetStatements(pattern2.getMessage());
    for (int i=0; i<2; i++) {
        ASSERT_TRUE(it2->hasNext());
        EXPECT_THAT(stmts, Contains(it2->next()));
    }
    EXPECT_FALSE(it2->hasNext());
}


TEST_F(PersistenceTest, TestRemoveStatements) {
    std::vector<rdf::proto::Statement> stmts = {
            rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"),
                           rdf::URI("http://example.com/o1")).getMessage(),
            rdf::Statement(rdf::URI("http://example.com/s2"), rdf::URI("http://example.com/p2"),
                           rdf::URI("http://example.com/o2")).getMessage()
    };

    util::CollectionIterator<rdf::proto::Statement> it(stmts);
    db->AddStatements(it);
    ASSERT_EQ(2, db->Size());

    {
        auto it1 = db->GetStatements(stmts[0]);
        EXPECT_TRUE(it1->hasNext());
    }

    db->RemoveStatements(stmts[0]);
    EXPECT_EQ(1, db->Size());

    {
        auto it2 = db->GetStatements(stmts[0]);
        EXPECT_FALSE(it2->hasNext());
    }

}

TEST_F(PersistenceTest, TestUpdates) {
    std::vector<rdf::proto::Statement> stmts = {
            rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"),
                           rdf::URI("http://example.com/o1")).getMessage(),
            rdf::Statement(rdf::URI("http://example.com/s2"), rdf::URI("http://example.com/p2"),
                           rdf::URI("http://example.com/o2")).getMessage()
    };

    util::CollectionIterator<rdf::proto::Statement> it(stmts);
    db->AddStatements(it);
    ASSERT_EQ(2, db->Size());

    service::proto::UpdateRequest removeReq;
    *removeReq.mutable_stmt_removed() = stmts[0];
    service::proto::UpdateRequest addReq;
    *addReq.mutable_stmt_added() =
            rdf::Statement(rdf::URI("http://example.com/s1"), rdf::URI("http://example.com/p1"),
                           rdf::URI("http://example.com/o3")).getMessage();


    util::CollectionIterator<service::proto::UpdateRequest> updates({ removeReq, addReq });
    db->Update(updates);
    ASSERT_EQ(2, db->Size());

    {
        auto it = db->GetStatements(stmts[0]);
        EXPECT_FALSE(it->hasNext());
    }

    {
        auto it = db->GetStatements(addReq.stmt_added());
        EXPECT_TRUE(it->hasNext());
    }

}


}
}
}
