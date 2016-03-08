//
// Created by wastl on 13.02.16.
//

#ifndef MARMOTTA_TIME_LOGGER_H
#define MARMOTTA_TIME_LOGGER_H

#include <string>
#include <chrono>

namespace marmotta {
namespace util {

/**
 * A time logger, writes a logging message when initialised and timing
 * information when destructed.
 */
class TimeLogger {
 public:
    TimeLogger(const std::string& name);

    ~TimeLogger();

 private:
    std::string name_;
    std::chrono::time_point<std::chrono::steady_clock> start_;
};

}  // namespace util
}  // namespace marmotta

#endif //MARMOTTA_TIME_LOGGER_H
