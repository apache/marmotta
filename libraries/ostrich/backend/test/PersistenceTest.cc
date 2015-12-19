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
        LOG(INFO) << "Destroying Test DB";
        delete db;
        remove_all(testdir);
    }

    LevelDBPersistence* db;
    path testdir;
};

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

}
}
}
