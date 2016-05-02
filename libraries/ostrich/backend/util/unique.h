//
// Created by wastl on 20.12.15.
//

#include <memory>

#ifndef MARMOTTA_UNIQUE_H
#define MARMOTTA_UNIQUE_H
namespace marmotta {
namespace util {

/**
 * Backport of C++14 make_unique implementation.
 */
template<typename T, typename ...Args>
std::unique_ptr<T> make_unique( Args&& ...args )
{
    return std::unique_ptr<T>( new T( std::forward<Args>(args)... ) );
}

}  // namespace util
}  // namespace marmotta
#endif //MARMOTTA_UNIQUE_H
