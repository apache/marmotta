include_directories(.. ${CMAKE_CURRENT_BINARY_DIR}/..)

add_library(marmotta_serializer serializer_raptor.h serializer_raptor.cc serializer_raptor.cc serializer_proto.cc serializer_proto.h serializer_base.cc serializer_base.h serializer.cc serializer.h)
target_link_libraries(marmotta_serializer marmotta_model ${CMAKE_THREAD_LIBS_INIT} ${RAPTOR_LIBRARY})