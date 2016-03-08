//
// Created by wastl on 13.02.16.
//

#include <glog/logging.h>
#include "time_logger.h"

marmotta::util::TimeLogger::TimeLogger(const std::string &name)
        : start_(std::chrono::steady_clock::now())
        , name_(name) {
    LOG(INFO) << name << " started.";
}

marmotta::util::TimeLogger::~TimeLogger() {
    LOG(INFO) << name_ << " finished (time=" << std::chrono::duration <double, std::milli> (
            std::chrono::steady_clock::now() - start_).count() << "ms).";
}
