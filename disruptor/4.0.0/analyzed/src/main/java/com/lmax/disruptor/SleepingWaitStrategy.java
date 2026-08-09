/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmax.disruptor;

import java.util.concurrent.locks.LockSupport;

/**
 * 睡眠等待策略：先自旋，再 {@link Thread#yield()}，最终在 {@link EventProcessor}
 * 等待屏障时使用 {@code LockSupport.parkNanos(n)} 睡眠操作系统允许的最小纳秒数。
 *
 * <p>在性能与 CPU 占用之间取得较好平衡。空闲一段时间后可能出现延迟尖峰。
 * 对发布线程影响较小，因其无需唤醒条件变量上的事件处理线程。
 */
public final class SleepingWaitStrategy implements WaitStrategy
{
    private static final int SPIN_THRESHOLD = 100;
    private static final int DEFAULT_RETRIES = 200;
    private static final long DEFAULT_SLEEP = 100;

    private final int retries;
    private final long sleepTimeNs;

    /**
     * 使用默认重试次数与睡眠时长的睡眠等待策略。
     */
    public SleepingWaitStrategy()
    {
        this(DEFAULT_RETRIES, DEFAULT_SLEEP);
    }

    /**
     * @param retries 进入睡眠前的重试次数
     */
    public SleepingWaitStrategy(final int retries)
    {
        this(retries, DEFAULT_SLEEP);
    }

    /**
     * @param retries 进入睡眠前的重试次数
     * @param sleepTimeNs 每次睡眠时长（纳秒）
     */
    public SleepingWaitStrategy(final int retries, final long sleepTimeNs)
    {
        this.retries = retries;
        this.sleepTimeNs = sleepTimeNs;
    }

    @Override
    public long waitFor(
        final long sequence, final Sequence cursor, final Sequence dependentSequence, final SequenceBarrier barrier)
        throws AlertException
    {
        long availableSequence;
        int counter = retries;

        while ((availableSequence = dependentSequence.get()) < sequence)
        {
            counter = applyWaitMethod(barrier, counter);
        }

        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking()
    {
    }

    private int applyWaitMethod(final SequenceBarrier barrier, final int counter)
        throws AlertException
    {
        barrier.checkAlert();

        if (counter > SPIN_THRESHOLD)
        {
            return counter - 1;
        }
        else if (counter > 0)
        {
            Thread.yield();
            return counter - 1;
        }
        else
        {
            LockSupport.parkNanos(sleepTimeNs);
        }

        return counter;
    }
}
