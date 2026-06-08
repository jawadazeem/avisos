#pragma once

#include <queue>
#include <stdexcept>

namespace avisos::util {

template <typename T>
class FixedQueue {
public:
    explicit FixedQueue(size_t capacity) : max_capacity_(capacity) {}

    bool push(const T& value) {
        if (q_.size() >= max_capacity_) {
            return false;
        }
        q_.push(value);
        return true;
    }

    void pop() {
        if (!q_.empty()) {
            q_.pop();
        }
    }

    [[nodiscard]] T front() const {
        if (q_.empty()) throw std::underflow_error("Queue is empty");
        return q_.front();
    }

    [[nodiscard]] bool empty() const { return q_.empty(); }
    [[nodiscard]] bool full() const { return q_.size() >= max_capacity_; }
    [[nodiscard]] size_t size() const { return q_.size(); }

private:
    std::queue<T> q_;
    size_t max_capacity_;
};

} // namespace avisos::util
