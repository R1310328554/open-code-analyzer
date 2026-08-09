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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * 分布式锁 Pub/Sub 订阅条目（{@link PubSubEntry} 实现）。
 * <p>维护重入计数 {@code counter}、{@link Semaphore} 唤醒 latch 及
 * 锁释放/续期等 {@link Runnable} 监听器队列。
 *
 * @author Nikita Koksharov
 */
public class RedissonLockEntry implements PubSubEntry<RedissonLockEntry> {

    private volatile int counter;

    private final Semaphore latch;
    private final CompletableFuture<RedissonLockEntry> promise;
    private final ConcurrentLinkedQueue<Runnable> listeners = new ConcurrentLinkedQueue<Runnable>();

    /** @param promise 订阅成功时完成的 future */
    public RedissonLockEntry(CompletableFuture<RedissonLockEntry> promise) {
        super();
        this.latch = new Semaphore(0);
        this.promise = promise;
    }

    /** 返回当前重入持有计数。 */
    public int acquired() {
        return counter;
    }

    /** 重入计数加一。 */
    public void acquire() {
        counter++;
    }
    public void acquire(int permits) {
        counter+=permits;
    }

    /** 重入计数减一并返回新值。 */
    public int release() {
        return --counter;
    }

    public CompletableFuture<RedissonLockEntry> getPromise() {
        return promise;
    }

    /** 注册监听器；若 latch 已释放则立即执行。 */
    public void addListener(Runnable listener) {
        listeners.add(listener);

        if (latch.tryAcquire()) {
            tryRunListener();
        }
    }

    /** 从队列取出一个监听器并执行。 */
    public void tryRunListener() {
        Runnable runnableToExecute = listeners.poll();
        if (runnableToExecute != null) {
            runnableToExecute.run();
        }
    }

    public void tryRunAllListeners() {
        while (true) {
            Runnable runnableToExecute = listeners.poll();
            if (runnableToExecute == null) {
                break;
            }
            runnableToExecute.run();
        }
    }

    public boolean removeListener(Runnable listener) {
        return listeners.remove(listener);
    }

    public Semaphore getLatch() {
        return latch;
    }

    @Override
    public String toString() {
        return "RedissonLockEntry{" +
                "counter=" + counter +
                '}';
    }
}
