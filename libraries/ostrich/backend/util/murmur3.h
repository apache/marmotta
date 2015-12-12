//
// Implementation of Murmur3 hashing using the C++ reference implementation.
// See https://code.google.com/p/smhasher/wiki/MurmurHash
//

#ifndef MARMOTTA_MURMUR3_H
#define MARMOTTA_MURMUR3_H

#include <stdint.h>

void MurmurHash3_x86_32  ( const void * key, int len, uint32_t seed, void * out );

void MurmurHash3_x86_128 ( const void * key, int len, uint32_t seed, void * out );

void MurmurHash3_x64_128 ( const void * key, int len, uint32_t seed, void * out );


#endif //MARMOTTA_MURMUR3_H
