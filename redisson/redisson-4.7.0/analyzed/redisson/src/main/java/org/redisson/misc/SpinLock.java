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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 轻量级自旋锁：基于 {@link AtomicReference} 实现线程互斥，
 * 可选可重入模式。持锁线程通过 {@link #execute(Runnable)} 等模板方法
 * 自动加锁/解锁，避免遗漏 release。
 * <p>
 * 自旋超过 {@link #spinLimit} 次后调用 {@link Thread#yield()} 让出 CPU。
 *
 * @author Nikita Koksharov
 *
 */
public final class SpinLock {

    /** 当前持锁线程，null 表示空闲。 */
    private final AtomicReference<Thread> acquired = new AtomicReference<>();

    /** 纯自旋次数上限，超过后 yield。 */
    private final int spinLimit = 7000;
    /** 可重入嵌套层数。 */
    private int nestedLevel;

    /** 是否允许同线程重复加锁。 */
    private final boolean reentrant;

    /** 默认可重入自旋锁。 */
    public SpinLock() {
        this(true);
    }

    /** @param reentrant 是否启用可重入 */
    public SpinLock(boolean reentrant) {
        this.reentrant = reentrant;
    }

    /** 可中断加锁：CAS 获取或自旋等待，可重入时递增 nestedLevel。 */
    private void lockInterruptibly() throws InterruptedException {
        int spins = 0;
        while (true) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            // 可重入：同线程直接嵌套计数
            if (reentrant
                    && acquired.get() == Thread.currentThread()) {
                nestedLevel++;
                return;
            // 空闲时 CAS 抢占
            } else if (acquired.get() == null
                    && acquired.compareAndSet(null, Thread.currentThread())) {
                nestedLevel = 1;
                return;
            // 自旋过多则让出 CPU
            } else if (spins >= spinLimit) {
                Thread.yield();
            } else {
                spins++;
            }
        }
    }

    /** 仅持锁线程可解锁；嵌套归零时释放锁。 */
    private void unlock() {
        if (acquired.get() == Thread.currentThread()) {
            nestedLevel--;
            if (nestedLevel == 0) {
                acquired.set(null);
            }
        }
    }

    /** 加锁执行 Runnable；中断时恢复中断标志并跳过执行。 */
    public void execute(Runnable r) {
        try {
            lockInterruptibly();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        try {
            r.run();
        } finally {
            unlock();
        }
    }

    /** 加锁执行 Supplier；中断时返回 null。 */
    public <T> T execute(Supplier<T> r) {
        try {
            lockInterruptibly();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        try {
            return r.get();
        } finally {
            unlock();
        }
    }

    /** 可中断加锁并执行；中断异常向上抛出。 */
    public void executeInterruptibly(Runnable r) throws InterruptedException {
        lockInterruptibly();
        try {
            r.run();
        } finally {
            unlock();
        }
    }

}
