include_directories(
        ".."
        "${gmock_SOURCE_DIR}/include"
        "${gtest_SOURCE_DIR}/include"
        "${CMAKE_CURRENT_BINARY_DIR}/.."
        "${CMAKE_CURRENT_BINARY_DIR}/../model"
        "${RAPTOR_INCLUDE_DIR}/raptor2"
)

add_executable(model_tests NamespaceTest.cc StatementTest.cc main.cc)
target_link_libraries(model_tests gtest_main marmotta_model ${GLOG_LIBRARY})

add_executable(sparql_tests SparqlTest.cc main.cc)
target_link_libraries(sparql_tests gtest_main marmotta_model marmotta_sparql ${GLOG_LIBRARY})

add_executable(leveldb_tests main.cc LevelDBTest.cc)
target_link_libraries(leveldb_tests gtest_main marmotta_leveldb ${GLOG_LIBRARY} ${Boost_LIBRARIES})

add_executable(persistence_tests main.cc PersistenceTest.cc)
target_link_libraries(persistence_tests gtest_main marmotta_persistence ${GLOG_LIBRARY} ${Boost_LIBRARIES})

add_test(NAME ModelTest COMMAND model_tests)
add_test(NAME SparqlTest COMMAND sparql_tests)
add_test(NAME LevelDBTest COMMAND leveldb_tests)
add_test(NAME PersistenceTest COMMAND persistence_tests)