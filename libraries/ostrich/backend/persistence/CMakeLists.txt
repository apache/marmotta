include_directories(.. ${CMAKE_CURRENT_BINARY_DIR}/.. ${CMAKE_CURRENT_BINARY_DIR}/../model ${RAPTOR_INCLUDE_DIR}/raptor2)

add_library(marmotta_persistence
        leveldb_sparql.cc leveldb_sparql.h
        base_persistence.cc base_persistence.h
        leveldb_service.cc leveldb_service.h )
target_link_libraries(marmotta_persistence
        marmotta_model marmotta_util marmotta_sparql marmotta_service
        ${GLOG_LIBRARY} ${PROTOBUF_LIBRARIES})

if (LevelDB_FOUND)
# Shared Marmotta Ostrich persistence implementation
add_library(marmotta_leveldb leveldb_persistence.cc leveldb_persistence.h)
target_link_libraries(marmotta_leveldb
        marmotta_persistence ${LevelDB_LIBRARY} ${GLOG_LIBRARY} ${PROTOBUF_LIBRARIES})

# Server binary
add_executable(leveldb_persistence leveldb_server.cc)
target_link_libraries(leveldb_persistence marmotta_service marmotta_leveldb
        ${GFLAGS_LIBRARY} ${CMAKE_THREAD_LIBS_INIT} ${GRPC_LIBRARIES} ${Tcmalloc_LIBRARIES})
install(TARGETS leveldb_persistence DESTINATION bin)
endif()

if (RocksDB_FOUND)
add_library(marmotta_rocksdb
        rocksdb_persistence.cc rocksdb_persistence.h)
target_link_libraries(marmotta_rocksdb
        marmotta_persistence ${RocksDB_LIBRARY} ${GLOG_LIBRARY} ${PROTOBUF_LIBRARIES})

# Server binary
add_executable(rocksdb_persistence rocksdb_server.cc )
target_link_libraries(rocksdb_persistence marmotta_service marmotta_rocksdb
        ${GFLAGS_LIBRARY} ${CMAKE_THREAD_LIBS_INIT} ${GRPC_LIBRARIES} ${Tcmalloc_LIBRARIES})
install(TARGETS rocksdb_persistence DESTINATION bin)
endif()


# Command line admin tool
add_executable(marmotta_updatedb marmotta_updatedb.cc)
target_link_libraries(marmotta_updatedb marmotta_leveldb marmotta_parser marmotta_serializer
        ${GFLAGS_LIBRARY} ${Boost_LIBRARIES} ${CMAKE_THREAD_LIBS_INIT} ${Tcmalloc_LIBRARIES})
install(TARGETS marmotta_updatedb DESTINATION bin)
