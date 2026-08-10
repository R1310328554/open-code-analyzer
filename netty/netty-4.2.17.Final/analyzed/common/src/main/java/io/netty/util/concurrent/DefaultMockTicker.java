/*
 * Copyright 2025 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.concurrent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;
import static java.util.Objects.requireNonNull;

/**
 * The default {@link MockTicker} implementation.
 *
 * <p>可手动推进虚拟时间的 {@link MockTicker} 实现，供单元测试控制 {@link Ticker} 行为。
 * 使用公平锁保证多个 {@link #sleep} 等待者按排队顺序被唤醒。</p>
 */
final class DefaultMockTicker implements MockTicker {

    // 公平锁：等待 sleep 的线程按入队顺序处理 condition 信号
    // The lock is fair, so waiters get to process condition signals in the order they (the waiters) queued up.
    private final ReentrantLock lock = new ReentrantLock(true);
    /** sleep 线程等待时间推进的条件变量。 */
    private final Condition tickCondition = lock.newCondition();
    /** 测试代码等待某线程进入 sleep 的条件变量。 */
    private final Condition sleeperCondition = lock.newCondition();
    /** 模拟的纳秒时钟，由 {@link #advance} 推进。 */
    private final AtomicLong nanoTime = new AtomicLong();
    /** 当前处于 {@link #sleep} 阻塞中的线程集合。 */
    private final Set<Thread> sleepers = Collections.newSetFromMap(new IdentityHashMap<>());

    @Override
    public long nanoTime() {
        return nanoTime.get();
    }

    /**
     * 阻塞直到虚拟时间流逝 {@code delay}；{@code delay == 0} 时立即返回。
     * 实际唤醒依赖外部调用 {@link #advance}。
     */
    @Override
    public void sleep(long delay, TimeUnit unit) throws InterruptedException {
        checkPositiveOrZero(delay, "delay");
        requireNonNull(unit, "unit");

        if (delay == 0) {
            return;
        }

        final long delayNanos = unit.toNanos(delay);
        lock.lockInterruptibly();
        try {
            final long startTimeNanos = nanoTime();
            sleepers.add(Thread.currentThread());
            sleeperCondition.signalAll();
            do {
                tickCondition.await();
            } while (nanoTime() - startTimeNanos < delayNanos);
        } finally {
            sleepers.remove(Thread.currentThread());
            lock.unlock();
        }
    }

    /**
     * Wait for the given thread to enter the {@link #sleep(long, TimeUnit)} method, and block.
     *
     * <p>测试辅助：阻塞直到指定线程进入 {@link #sleep} 并开始等待。</p>
     */
    public void awaitSleepingThread(Thread thread) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (!sleepers.contains(thread)) {
                sleeperCondition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将虚拟时钟向前推进 {@code amount}，并唤醒所有等待中的 sleep 线程。
     */
    @Override
    public void advance(long amount, TimeUnit unit) {
        checkPositiveOrZero(amount, "amount");
        requireNonNull(unit, "unit");

        if (amount == 0) {
            return;
        }

        final long amountNanos = unit.toNanos(amount);
        lock.lock();
        try {
            nanoTime.addAndGet(amountNanos);
            tickCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
