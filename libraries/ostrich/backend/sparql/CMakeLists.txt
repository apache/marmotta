include_directories(.. ${CMAKE_CURRENT_BINARY_DIR}/.. ${RAPTOR_INCLUDE_DIR}/raptor2)

add_library(marmotta_sparql
        rasqal_model.cc rasqal_model.h rasqal_adapter.cc rasqal_adapter.h)
target_link_libraries(marmotta_sparql marmotta_model marmotta_util marmotta_raptor_util
        ${CMAKE_THREAD_LIBS_INIT}
        ${PROTOBUF_LIBRARIES} ${GRPC_LIBRARIES}
        ${RASQAL_LIBRARIES} ${RAPTOR_LIBRARY} ${GLOG_LIBRARY})