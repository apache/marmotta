include_directories(.. ${CMAKE_CURRENT_BINARY_DIR}/..)

add_library(marmotta_util murmur3.cc murmur3.h split.cc split.h iterator.h)