include_directories(
        .. ${CMAKE_CURRENT_BINARY_DIR}/..
        ${CMAKE_CURRENT_BINARY_DIR}/../model ${CMAKE_CURRENT_BINARY_DIR}/../service)

add_executable(marmotta_sharding sharding.cc sharding.h server.cc)
target_link_libraries(marmotta_sharding
        marmotta_util marmotta_model marmotta_service
        ${LevelDB_LIBRARY} ${GFLAGS_LIBRARY} ${GLOG_LIBRARY}
        ${CMAKE_THREAD_LIBS_INIT} ${PROTOBUF_LIBRARIES} ${GRPC_LIBRARIES} ${Tcmalloc_LIBRARIES})

