include_directories(${GTEST_INCLUDE_DIRS})
include_directories(..)
include_directories(${CMAKE_CURRENT_BINARY_DIR}/..)
include_directories(${CMAKE_CURRENT_BINARY_DIR}/../model)
include_directories(${RAPTOR_INCLUDE_DIR}/raptor2)

add_library(gtest STATIC gtest/gtest.h gmock/gmock.h gmock-gtest-all.cc)

add_executable(model_tests StatementTest.cc main.cc)
target_link_libraries(model_tests gtest marmotta_model ${GLOG_LIBRARY})

add_executable(sparql_tests SparqlTest.cc main.cc)
target_link_libraries(sparql_tests gtest marmotta_model marmotta_sparql ${GLOG_LIBRARY})

add_executable(persistence_tests main.cc PersistenceTest.cc)
target_link_libraries(persistence_tests gtest marmotta_leveldb ${GLOG_LIBRARY} ${Boost_LIBRARIES})

add_test(NAME ModelTest COMMAND model_tests)
add_test(NAME SparqlTest COMMAND sparql_tests)
add_test(NAME PersistenceTest COMMAND persistence_tests)