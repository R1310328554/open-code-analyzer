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

import java.util.concurrent.TimeUnit;

/**
 * 供 {@link EventProcessor} 在屏障上等待的分阶段退避等待策略。
 *
 * <p>当吞吐量与低延迟不如 CPU 资源重要时可选用本策略。
 * 先忙等自旋，再 {@link Thread#yield()}，最后回退到配置的备用 {@link WaitStrategy}。
 */
public final class PhasedBackoffWaitStrategy implements WaitStrategy
{
    private static final int SPIN_TRIES = 10000;
    private final long spinTimeoutNanos;
    private final long yieldTimeoutNanos;
    private final WaitStrategy fallbackStrategy;

    /**
     * @param spinTimeout 忙等自旋的最长时间
     * @param yieldTimeout 让出 CPU 的最长时间
     * @param units 上述超时值的时间单位
     * @param fallbackStrategy 自旋与让出结束后使用的回退策略
     */
    public PhasedBackoffWaitStrategy(
        final long spinTimeout,
        final long yieldTimeout,
        final TimeUnit units,
        final WaitStrategy fallbackStrategy)
    {
        this.spinTimeoutNanos = units.toNanos(spinTimeout);
        this.yieldTimeoutNanos = spinTimeoutNanos + units.toNanos(yieldTimeout);
        this.fallbackStrategy = fallbackStrategy;
    }

    /**
     * 构造以 {@link BlockingWaitStrategy} 为回退的 {@link PhasedBackoffWaitStrategy}。
     *
     * @param spinTimeout 忙等自旋的最长时间
     * @param yieldTimeout 让出 CPU 的最长时间
     * @param units 上述超时值的时间单位
     * @return 构造完成的等待策略
     */
    public static PhasedBackoffWaitStrategy withLock(
        final long spinTimeout,
        final long yieldTimeout,
        final TimeUnit units)
    {
        return new PhasedBackoffWaitStrategy(
            spinTimeout, yieldTimeout,
            units, new BlockingWaitStrategy());
    }

    /**
     * 构造以 {@link LiteBlockingWaitStrategy} 为回退的 {@link PhasedBackoffWaitStrategy}。
     *
     * @param spinTimeout 忙等自旋的最长时间
     * @param yieldTimeout 让出 CPU 的最长时间
     * @param units 上述超时值的时间单位
     * @return 构造完成的等待策略
     */
    public static PhasedBackoffWaitStrategy withLiteLock(
        final long spinTimeout,
        final long yieldTimeout,
        final TimeUnit units)
    {
        return new PhasedBackoffWaitStrategy(
            spinTimeout, yieldTimeout,
            units, new LiteBlockingWaitStrategy());
    }

    /**
     * 构造以 {@link SleepingWaitStrategy} 为回退的 {@link PhasedBackoffWaitStrategy}。
     *
     * @param spinTimeout 忙等自旋的最长时间
     * @param yieldTimeout 让出 CPU 的最长时间
     * @param units 上述超时值的时间单位
     * @return 构造完成的等待策略
     */
    public static PhasedBackoffWaitStrategy withSleep(
        final long spinTimeout,
        final long yieldTimeout,
        final TimeUnit units)
    {
        return new PhasedBackoffWaitStrategy(
            spinTimeout, yieldTimeout,
            units, new SleepingWaitStrategy(0));
    }

    @Override
    public long waitFor(final long sequence, final Sequence cursor, final Sequence dependentSequence, final SequenceBarrier barrier)
        throws AlertException, InterruptedException, TimeoutException
    {
        long availableSequence;
        long startTime = 0;
        int counter = SPIN_TRIES;

        do
        {
            // 步骤 1：依赖序号已到达则立即返回
            if ((availableSequence = dependentSequence.get()) >= sequence)
            {
                return availableSequence;
            }

            // 步骤 2：自旋计数耗尽后按已耗时选择让出或回退策略
            if (0 == --counter)
            {
                if (0 == startTime)
                {
                    startTime = System.nanoTime();
                }
                else
                {
                    long timeDelta = System.nanoTime() - startTime;
                    if (timeDelta > yieldTimeoutNanos)
                    {
                        return fallbackStrategy.waitFor(sequence, cursor, dependentSequence, barrier);
                    }
                    else if (timeDelta > spinTimeoutNanos)
                    {
                        Thread.yield();
                    }
                }
                counter = SPIN_TRIES;
            }
        }
        while (true);
    }

    @Override
    public void signalAllWhenBlocking()
    {
        fallbackStrategy.signalAllWhenBlocking();
    }
}
