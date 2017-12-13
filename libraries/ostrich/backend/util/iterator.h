/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
// A collection of iterators used by different components.
//

#ifndef MARMOTTA_ITERATOR_H
#define MARMOTTA_ITERATOR_H

#include <queue>
#include <mutex>
#include <condition_variable>

namespace marmotta {
namespace util {

/**
 * A common iterator class for iterators binding resources.
 */
template<typename T>
class CloseableIterator {
 public:

    /**
     * Close the iterator, freeing any wrapped resources
     */
    virtual ~CloseableIterator() {}

    /**
     * Increment iterator to next element and return a reference to this element.
    */
    virtual const T& next() = 0;

    /**
     * Return a reference to the current element.
     */
    virtual const T& current() const = 0;

    /**
     * Return true in case the iterator has more elements.
     */
    virtual bool hasNext() = 0;

};

/**
 * An empty iterator.
 */
template<typename T>
class EmptyIterator : public CloseableIterator<T> {
 public:
    EmptyIterator() { }

    const T &next() override {
        throw std::out_of_range("No more elements");
    };

    const T &current() const override {
        throw std::out_of_range("No more elements");
    };

    bool hasNext() override {
        return false;
    };
};



/**
 * An iterator wrapping a single element.
 */
template<typename T>
class SingletonIterator : public CloseableIterator<T> {
 public:
    SingletonIterator(T&& value) : value(value), incremented(false) { }

    const T &next() override {
        if (!incremented) {
            incremented = true;
            return value;
        } else {
            throw std::out_of_range("No more elements");
        }
    };

    const T &current() const override {
        if (!incremented) {
            return value;
        } else {
            throw std::out_of_range("No more elements");
        }
    };

    bool hasNext() override {
        return !incremented;
    };

 private:
    T value;
    bool incremented;

};

/**
 * An iterator wrapping a standard STL collection iterator.
 */
template<typename T>
class CollectionIterator : public CloseableIterator<T> {
 public:
    CollectionIterator(std::vector<T> values)
            : values(values), index(0) {
    }

    const T& next() override {
        index++;
        return values[index-1];
    };

    const T& current() const override {
        return values[index-1];
    };

    bool hasNext() override {
        return index < values.size();
    };

 private:
    std::vector<T> values;
    int index;
};

/**
 * An iterator implementation supporting to pass a predicate for filtering values.
 */
template<typename T>
class FilteringIterator : public CloseableIterator<T> {
 public:
    using PredicateFn = std::function<bool(const T&)>;

    /**
     * Create a filtering iterator using the given predicate as filter.
     * The predicate function should return true for accepted values.
     * Takes ownership of the iterator passed as argument.
     */
    FilteringIterator(CloseableIterator<T>* it,  PredicateFn pred)
            : it(it), pred(pred), nextExists(false) {
        // run findNext once so next is set
        findNext();
    }

    /**
     * Increment iterator to next element.
     */
    const T& next() override {
        findNext();
        return current_;
    };

    const T& current() const override {
        return current_;
    };

    /**
     * Return true in case the iterator has more elements.
     */
    bool hasNext() override {
        return nextExists;
    };


 private:
    std::unique_ptr<CloseableIterator<T>> it;
    PredicateFn pred;

    T current_;
    T next_;

    bool nextExists;

    void findNext() {
        current_ = std::move(next_);
        nextExists = false;

        while (it->hasNext()) {
            next_ = it->next();
            if (pred(next_)) {
                nextExists = true;
                break;
            }
        }
    }
};

/**
 * An abstract iterator implementation supporting to convert values
 * from one type to another. Subclasses must implement the convert() method.
 */
template<typename F, typename T>
class ConvertingIterator : public CloseableIterator<T> {
 public:
    ConvertingIterator(CloseableIterator<F>* it) : it(it) { }

    /**
     * Increment iterator to next element.
     */
    const T& next() override {
        current_ = std::move(convert(it->next()));
        return current_;
    };

    const T& current() const override {
        return current_;
    };

    /**
     * Return true in case the iterator has more elements.
     */
    bool hasNext() override {
        return it->hasNext();
    };

 protected:
    virtual T convert(const F& from) = 0;

 private:
    std::unique_ptr<CloseableIterator<F>> it;
    T current_;
};


/**
 * A multi-threaded iterator supporting iteration over the results
 * successively added by a producer. Blocks while the internal queue
 * is empty and the producer is not yet finished reporting. The
 * producer has to explicitly call finish() when there are no more
 * elements to report.
 */
template<typename T>
class ProducerConsumerIterator : public CloseableIterator<T> {
 public:
    ProducerConsumerIterator() {}

    void add(const T& value) {
        std::unique_lock<std::mutex> lock(mutex);
        queue.push(value);
        condition.notify_one();
    }

    void finish() {
        std::unique_lock<std::mutex> lock(mutex);
        finished = true;
        condition.notify_all();
    }

    /**
     * Increment iterator to next element.
     */
    const T& next() override {
        if (hasNext()) {
            std::unique_lock<std::mutex> lock(mutex);
            current_ = queue.front();
            queue.pop();
        }
        return current_;
    };

    const T& current() const override {
        return current_;
    };

    /**
     * Return true in case the iterator has more elements.
     */
    bool hasNext() override {
        std::unique_lock<std::mutex> lock(mutex);
        if (queue.size() > 0) {
            return true;
        }
        if (finished) {
            return false;
        }
        condition.wait(lock);
        return hasNext();
    };

 private:
    std::queue<T> queue;
    std::mutex mutex;
    std::condition_variable condition;

    bool finished;
    T current_;
};

}
}


#endif //MARMOTTA_ITERATOR_H
