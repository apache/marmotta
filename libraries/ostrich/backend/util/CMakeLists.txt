include_directories(.. ${CMAKE_CURRENT_BINARY_DIR}/..)

add_library(marmotta_util murmur3.cc murmur3.h split.cc split.h iterator.h unique.h time_logger.cc time_logger.h)

add_library(marmotta_raptor_util raptor_util.h raptor_util.cc)
target_link_libraries(marmotta_raptor_util marmotta_model ${CMAKE_THREAD_LIBS_INIT} ${RAPTOR_LIBRARY})