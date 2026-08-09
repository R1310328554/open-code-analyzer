/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.misc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步信号量：基于 {@link CompletableFuture} 的 acquire/release，
 * 支持可选 {@link java.util.concurrent.ExecutorService} 在栈过深时分叉执行，
 * 避免高并发 acquire 链导致栈溢出。等待队列使用 {@link FastRemovalQueue}。
 *
 * @author Nikita Koksharov
 *
 */
public final class AsyncSemaphore {

    /** 可选线程池，栈深度超阈值时将 tryRun 提交到池内执行。 */
    private final ExecutorService executorService;
    /** 当前分叉到线程池的任务计数。 */
    private final AtomicInteger tasksLatch = new AtomicInteger(1);
    /** 当前栈上 complete 嵌套深度。 */
    private final AtomicInteger stackSize = new AtomicInteger();

    /** 可用许可计数（acquire 减、release 增）。 */
    private final AtomicInteger counter;
    /** 等待 acquire 的 CompletableFuture 队列。 */
    private final FastRemovalQueue<CompletableFuture<Void>> listeners = new FastRemovalQueue<>();

    /** 创建指定许可数的信号量（无线程池分叉）。 */
    public AsyncSemaphore(int permits) {
        this(permits, null);
    }

    /** 创建信号量并绑定可选 ExecutorService 用于栈过深时分叉。 */
    public AsyncSemaphore(int permits, ExecutorService executorService) {
        counter = new AtomicInteger(permits);
        this.executorService = executorService;
    }

    public int queueSize() {
        return listeners.size();
    }
    
    public void removeListeners() {
        listeners.clear();
    }

    /** 异步获取许可，无可用许可时 Future 挂起直至 release。 */
    public CompletableFuture<Void> acquire() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        listeners.add(future);
        future.whenComplete((r, e) -> {
            if (e != null) {
                listeners.remove(future);
            }
        });
        tryForkAndRun();
        return future;
    }

    /** 栈深度超 25×tasksLatch 时提交到线程池，否则直接 tryRun。 */
    private void tryForkAndRun() {
        if (executorService != null) {
            int val = tasksLatch.get();
            if (stackSize.get() > 25 * val
                    && tasksLatch.compareAndSet(val, val+1)) {
                executorService.submit(() -> {
                    tasksLatch.decrementAndGet();
                    tryRun();
                });
                return;
            }
        }

        tryRun();
    }

    /** 循环消耗许可并 complete 队首等待者；处理竞态与已取消 Future。 */
    private void tryRun() {
        while (true) {
            if (counter.decrementAndGet() >= 0) {
                CompletableFuture<Void> future = listeners.poll();
                if (future == null) {
                    counter.incrementAndGet();
                    if (listeners.isEmpty()) {
                        return;
                    }
                    continue;
                }

                boolean complete;
                if (executorService != null) {
                    stackSize.incrementAndGet();
                    complete = future.complete(null);
                    stackSize.decrementAndGet();
                } else {
                    complete = future.complete(null);
                }
                if (complete) {
                    return;
                } else {
                    counter.incrementAndGet();
                    // 已取消的等待者已在上方归还许可，继续循环避免重复 increment
                    continue;
                }
            }

            if (counter.incrementAndGet() <= 0) {
                return;
            }
        }
    }

    public int getCounter() {
        return counter.get();
    }

    /** 释放一个许可并尝试唤醒等待队列。 */
    public void release() {
        counter.incrementAndGet();
        tryForkAndRun();
    }

    @Override
    public String toString() {
        return "value:" + counter + ":queue:" + queueSize();
    }
    
    
    
}
