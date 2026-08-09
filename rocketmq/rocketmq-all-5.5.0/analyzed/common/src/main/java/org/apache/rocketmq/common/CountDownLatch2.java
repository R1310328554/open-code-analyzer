/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 在 {@link java.util.concurrent.CountDownLatch} 基础上增加 reset 能力。
 */
public class CountDownLatch2 {
    /** AQS 同步器。 */
    private final Sync sync;

    /**
     * 以给定计数初始化 CountDownLatch2。
     *
     * @param count 需调用 {@link #countDown} 的次数，归零后等待线程可通过 {@link #await}
     * @throws IllegalArgumentException count 为负时
     */
    public CountDownLatch2(int count) {
        if (count < 0)
            throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    /**
     * 阻塞直到计数归零，除非当前线程被中断。
     * 计数已为 0 则立即返回；大于 0 则挂起直至 countDown 归零或被中断。
     *
     * @throws InterruptedException 等待过程中被中断
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * 限时等待计数归零。
     *
     * @param timeout 最长等待时间
     * @param unit 时间单位
     * @return 计数归零返回 true，超时返回 false
     * @throws InterruptedException 等待过程中被中断
     */
    public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /** 计数减一，归零时唤醒所有等待线程。 */
    public void countDown() {
        sync.releaseShared(1);
    }

    /**
     * 返回当前计数（常用于调试与测试）。
     *
     * @return 当前计数
     */
    public long getCount() {
        return sync.getCount();
    }

    /** 将计数重置为构造时的初始值。 */
    public void reset() {
        sync.reset();
    }

    /**
     * 返回含当前计数的字符串表示。
     *
     * @return 标识与状态字符串
     */
    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }

    /** CountDownLatch2 的 AQS 同步实现，用 state 表示计数。 */
    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        /** 构造时的初始计数，供 reset 使用。 */
        private final int startCount;

        Sync(int count) {
            this.startCount = count;
            setState(count);
        }

        /** 读取当前 AQS state 作为计数。 */
        int getCount() {
            return getState();
        }

        /** 共享获取：state 为 0 时成功。 */
        @Override
        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        /** 共享释放：CAS 递减 state，减至 0 时唤醒等待者。 */
        @Override
        protected boolean tryReleaseShared(int releases) {
            // 递减计数，归零时发信号
            for (; ; ) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c - 1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }

        /** 将 state 恢复为 startCount。 */
        protected void reset() {
            setState(startCount);
        }
    }
}
