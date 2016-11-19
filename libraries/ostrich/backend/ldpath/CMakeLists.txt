include_directories(.. ${CMAKE_CURRENT_BINARY_DIR}/..)

add_library(marmotta_ldpath
        ldpath_model.h ldpath_model.cc
        parser_base.h parser_base.cc)
target_link_libraries(marmotta_ldpath ${CMAKE_THREAD_LIBS_INIT})