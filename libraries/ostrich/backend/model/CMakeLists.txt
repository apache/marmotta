file(GLOB ProtoFiles "${CMAKE_CURRENT_SOURCE_DIR}/*.proto")
PROTOBUF_GENERATE_CPP(PROTO_SRCS PROTO_HDRS ${ProtoFiles})
include_directories(.. ${CMAKE_CURRENT_BINARY_DIR}/..)

add_library(marmotta_model rdf_model.cc rdf_model.h ${PROTO_SRCS} ${PROTO_HDRS} rdf_operators.h rdf_operators.cc)
target_link_libraries(marmotta_model ${CMAKE_THREAD_LIBS_INIT} ${PROTOBUF_LIBRARIES})