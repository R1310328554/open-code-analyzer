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
package org.redisson;

import org.redisson.misc.ReclosableLatch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * {@link RedissonCountDownLatch} 的 Pub/Sub 订阅条目。
 * <p>维护引用计数、{@link org.redisson.misc.ReclosableLatch} 唤醒信号
 * 及计数变化时的监听器队列。
 */
public class RedissonCountDownLatchEntry implements PubSubEntry<RedissonCountDownLatchEntry> {

    private int counter;

    private final ReclosableLatch latch;
    private final CompletableFuture<RedissonCountDownLatchEntry> promise;
    private final ConcurrentLinkedQueue<Runnable> listeners = new ConcurrentLinkedQueue<>();

    public RedissonCountDownLatchEntry(CompletableFuture<RedissonCountDownLatchEntry> promise) {
        super();
        this.latch = new ReclosableLatch();
        this.promise = promise;
    }

    /** 增加一次订阅引用。 */
    public void acquire() {
        counter++;
    }
    public void acquire(int permits) {
        counter+=permits;
    }

    public int release() {
        return --counter;
    }

    public CompletableFuture<RedissonCountDownLatchEntry> getPromise() {
        return promise;
    }

    /** 注册计数变化或 latch 打开时的回调。 */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public boolean removeListener(Runnable listener) {
        return listeners.remove(listener);
    }

    public ConcurrentLinkedQueue<Runnable> getListeners() {
        return listeners;
    }

    /** 返回用于阻塞/唤醒等待线程的可重闭 latch。 */
    public ReclosableLatch getLatch() {
        return latch;
    }

}
