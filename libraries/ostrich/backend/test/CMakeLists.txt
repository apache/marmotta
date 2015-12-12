include_directories(.. ${CMAKE_CURRENT_BINARY_DIR}/.. ${RAPTOR_INCLUDE_DIR}/raptor2)

add_library(gtest STATIC gtest.h gtest-all.cc)

add_executable(model_tests StatementTest.cc main.cc)
target_link_libraries(model_tests gtest marmotta_model ${GLOG_LIBRARY})

add_executable(sparql_tests SparqlTest.cc main.cc)
target_link_libraries(sparql_tests gtest marmotta_model marmotta_sparql ${GLOG_LIBRARY})

add_test(NAME ModelTest
         COMMAND model_tests)
