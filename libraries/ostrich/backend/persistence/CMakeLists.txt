include_directories(.. ${CMAKE_CURRENT_BINARY_DIR}/.. ${CMAKE_CURRENT_BINARY_DIR}/../model ${RAPTOR_INCLUDE_DIR}/raptor2)

add_executable(marmotta_persistence
        leveldb_persistence.cc leveldb_persistence.h leveldb_service.cc leveldb_service.h
        leveldb_server.cc leveldb_sparql.cc leveldb_sparql.h)
target_link_libraries(marmotta_persistence
        marmotta_model marmotta_service marmotta_util marmotta_sparql
        ${LevelDB_LIBRARY} ${GFLAGS_LIBRARY} ${GLOG_LIBRARY}
        ${CMAKE_THREAD_LIBS_INIT} ${PROTOBUF_LIBRARIES} ${GRPC_LIBRARIES} ${Tcmalloc_LIBRARIES})

