include_directories(.. ${CMAKE_CURRENT_BINARY_DIR}/.. ${CMAKE_CURRENT_BINARY_DIR}/../model ${RAPTOR_INCLUDE_DIR}/raptor2)

file(GLOB ProtoFiles "${CMAKE_CURRENT_SOURCE_DIR}/*.proto")
PROTOBUF_GENERATE_CPP(PROTO_SRCS PROTO_HDRS ${ProtoFiles})
PROTOBUF_GENERATE_GRPC_CPP(GRPC_SRCS GRPC_HDRS ${ProtoFiles})
include_directories(${CMAKE_CURRENT_BINARY_DIR})

add_library(marmotta_service ${PROTO_SRCS} ${PROTO_HDRS} ${GRPC_SRCS} ${GRPC_HDRS})
target_link_libraries(marmotta_service marmotta_model ${CMAKE_THREAD_LIBS_INIT} ${PROTOBUF_LIBRARIES} ${GRPC_LIBRARIES})