# - Try to find the Rasqal rdf query library (http://librdf.org/rasqal/)
# Once done this will define
#
#  RASQAL_FOUND       - system has Rasqal
#  RASQAL_LIBRARIES   - Link these to use RASQAL
#  RASQAL_INCLUDE_DIR - The include directory for using rasqal
#  RASQAL_DEFINITIONS - Compiler switches required for using RASQAL
#  RASQAL_VERSION     - The rasqal version string

# (c) 2007-2009 Sebastian Trueg <trueg@kde.org>
#
# Based on FindFontconfig Copyright (c) 2006,2007 Laurent Montel, <montel@kde.org>
#
# Redistribution and use is allowed according to the terms of the BSD license.
# For details see the accompanying COPYING-CMAKE-SCRIPTS file.


if(WINCE)
  FIND_PROGRAM(
    RASQAL_CONFIG
    NAMES rasqal-config
    PATHS ${HOST_BINDIR} NO_DEFAULT_PATH
    )
else(WINCE)
  FIND_PROGRAM(
    RASQAL_CONFIG
    NAMES rasqal-config
    )
endif(WINCE)

  if(RASQAL_CONFIG)
    EXECUTE_PROCESS(
      COMMAND ${RASQAL_CONFIG} --version
      OUTPUT_VARIABLE RASQAL_VERSION
      )
    if(RASQAL_VERSION)
      STRING(REPLACE "\n" "" RASQAL_VERSION ${RASQAL_VERSION})
  
      # extract include paths from rasqal-config
      EXECUTE_PROCESS(
        COMMAND ${RASQAL_CONFIG} --cflags
        OUTPUT_VARIABLE rasqal_CFLAGS_ARGS)
      STRING( REPLACE " " ";" rasqal_CFLAGS_ARGS ${rasqal_CFLAGS_ARGS} )
      FOREACH( _ARG ${rasqal_CFLAGS_ARGS} )
        IF(${_ARG} MATCHES "^-I")
          STRING(REGEX REPLACE "^-I" "" _ARG ${_ARG})
          STRING( REPLACE "\n" "" _ARG ${_ARG} )
          LIST(APPEND rasqal_INCLUDE_DIRS ${_ARG})
        ENDIF(${_ARG} MATCHES "^-I")
      ENDFOREACH(_ARG)
  
      # extract lib paths from rasqal-config
      EXECUTE_PROCESS(
        COMMAND ${RASQAL_CONFIG} --libs
        OUTPUT_VARIABLE rasqal_CFLAGS_ARGS)
      STRING( REPLACE " " ";" rasqal_CFLAGS_ARGS ${rasqal_CFLAGS_ARGS} )
      FOREACH( _ARG ${rasqal_CFLAGS_ARGS} )
        IF(${_ARG} MATCHES "^-L")
          STRING(REGEX REPLACE "^-L" "" _ARG ${_ARG})
          LIST(APPEND rasqal_LIBRARY_DIRS ${_ARG})
        ENDIF(${_ARG} MATCHES "^-L")
      ENDFOREACH(_ARG)
    endif(RASQAL_VERSION)
  endif(RASQAL_CONFIG)

  find_path(RASQAL_INCLUDE_DIR rasqal.h
    PATHS
    ${redland_INCLUDE_DIRS}
    ${rasqal_INCLUDE_DIRS}
    /usr/X11/include
    PATH_SUFFIXES redland rasqal
  )

  find_library(RASQAL_LIBRARIES NAMES rasqal librasqal
    PATHS
    ${rasqal_LIBRARY_DIRS}
  )

  include(FindPackageHandleStandardArgs)
  find_package_handle_standard_args(
      Rasqal
      VERSION_VAR   RASQAL_VERSION
      REQUIRED_VARS RASQAL_LIBRARIES RASQAL_INCLUDE_DIR)

  if (RASQAL_FOUND)
    set(RASQAL_DEFINITIONS ${rasqal_CFLAGS})
    if (NOT Rasqal_FIND_QUIETLY)
      message(STATUS "Found Rasqal ${RASQAL_VERSION}: libs - ${RASQAL_LIBRARIES}; includes - ${RASQAL_INCLUDE_DIR}")
    endif (NOT Rasqal_FIND_QUIETLY)
  else (RASQAL_FOUND)
    if (Rasqal_FIND_REQUIRED)
      message(FATAL_ERROR "Could NOT find Rasqal")
    endif (Rasqal_FIND_REQUIRED)
  endif (RASQAL_FOUND)


mark_as_advanced(RASQAL_INCLUDE_DIR_TMP
                 RASQAL_INCLUDE_DIR 
                 RASQAL_LIBRARIES)