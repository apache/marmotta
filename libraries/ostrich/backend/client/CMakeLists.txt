include_directories(.. ${CMAKE_CURRENT_BINARY_DIR}/.. ${CMAKE_CURRENT_BINARY_DIR}/../model)

add_executable(marmotta_client client.cc)
target_link_libraries(marmotta_client
        marmotta_model marmotta_service marmotta_parser marmotta_serializer
        ${GFLAGS_LIBRARY}
        ${CMAKE_THREAD_LIBS_INIT} ${PROTOBUF_LIBRARIES} ${GRPC_LIBRARIES})

