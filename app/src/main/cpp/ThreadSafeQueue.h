//
// Created by thehepta on 2024/5/26.
//

#ifndef FGUM_THREADSAFEQUEUE_H
#define FGUM_THREADSAFEQUEUE_H


#include <queue>
#include <memory>
#include <iostream>
#include <string>
#include <condition_variable>
#include <mutex>
class ThreadSafeQueue {
public:
    ThreadSafeQueue() = default;
    ~ThreadSafeQueue() = default;

    // 禁止复制和赋值
    ThreadSafeQueue(const ThreadSafeQueue&) = delete;
    ThreadSafeQueue& operator=(const ThreadSafeQueue&) = delete;


    void enqueue(std::string value) {
        std::lock_guard<std::mutex> lock(mtx);
        queue.push(std::move(value));
        cv.notify_one();
    }

    // 从队列中移除并返回字符串
    std::string dequeue() {
        std::unique_lock<std::mutex> lock(mtx);
        cv.wait(lock, [this] { return !queue.empty(); });
        std::string value = std::move(queue.front());
        queue.pop();
        return value;
    }

    // 尝试从队列中移除并返回字符串，如果队列为空则返回false
    bool try_dequeue(std::string& value) {
        std::lock_guard<std::mutex> lock(mtx);
        if (queue.empty()) {
            return false;
        }
        value = std::move(queue.front());
        queue.pop();
        return true;
    }

    // 检查队列是否为空
    bool empty() const {
        std::lock_guard<std::mutex> lock(mtx);
        return queue.empty();
    }

    // 获取队列大小
    size_t size() const {
        std::lock_guard<std::mutex> lock(mtx);
        return queue.size();
    }


private:
    std::queue<std::string> queue;
    std::condition_variable cv;
    mutable std::mutex mtx;
};

#endif //FGUM_THREADSAFEQUEUE_H
