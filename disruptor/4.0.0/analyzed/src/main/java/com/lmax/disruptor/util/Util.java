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
package com.lmax.disruptor.util;

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;

/**
 * Disruptor 通用工具方法集合。
 */
public final class Util
{
    private static final int ONE_MILLISECOND_IN_NANOSECONDS = 1_000_000;

    /**
     * 计算大于等于 x 的下一个 2 的幂。
     *
     * <p>算法出自 Hacker's Delight 第 3 章, Harry S. Warren Jr.
     *
     * @param x 待向上取整的值
     * @return 大于等于 x 的最小 2 的幂
     */
    public static int ceilingNextPowerOfTwo(final int x)
    {
        return 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(x - 1));
    }

    /**
     * 从序号数组中获取最小 {@link com.lmax.disruptor.Sequence}s.
     *
     * @param 待比较的序号数组
     * @return 最小序号；数组为空时返回 Long.MAX_VALUE
     */
    public static long getMinimumSequence(final Sequence[] sequences)
    {
        return getMinimumSequence(sequences, Long.MAX_VALUE);
    }

    /**
     * 从序号数组中获取最小 {@link com.lmax.disruptor.Sequence}s.
     *
     * @param 待比较的序号数组
     * @param minimum   an initial default minimum.  If the array is empty this value will be
     *                  returned.
     * @return the smaller of minimum sequence value found in {@code sequences} and {@code minimum};
     * {@code minimum} if {@code sequences} is empty
     */
    public static long getMinimumSequence(final Sequence[] sequences, final long minimum)
    {
        long minimumSequence = minimum;
        for (int i = 0, n = sequences.length; i < n; i++)
        {
            long value = sequences[i].get();
            minimumSequence = Math.min(minimumSequence, value);
        }

        return minimumSequence;
    }

    /**
     * Get an array of {@link Sequence}s for the passed {@link EventProcessor}s.
     *
     * @param 事件处理器
     * @return the array of {@link Sequence}s
     */
    public static Sequence[] getSequencesFor(final EventProcessor... processors)
    {
        Sequence[] sequences = new Sequence[processors.length];
        for (int i = 0; i < sequences.length; i++)
        {
            sequences[i] = processors[i].getSequence();
        }

        return sequences;
    }

    /**
     * 计算以 2 为底的对数, essentially reports the location
     * of the highest bit.
     *
     * @param value 待计算 log2 的正整数
     * @return log2 值
     */
    public static int log2(final int value)
    {
        if (value < 1)
        {
            throw new IllegalArgumentException("value must be a positive number");
        }
        return Integer.SIZE - Integer.numberOfLeadingZeros(value) - 1;
    }

    /**
     * @param mutex 等待对象
     * @param timeoutNanos 最长等待纳秒数
     * @return 剩余未等待的纳秒数（近似）
     * @throws InterruptedException 等待被中断时抛出
     */
    public static long awaitNanos(final Object mutex, final long timeoutNanos) throws InterruptedException
    {
        long millis = timeoutNanos / ONE_MILLISECOND_IN_NANOSECONDS;
        long nanos = timeoutNanos % ONE_MILLISECOND_IN_NANOSECONDS;

        long t0 = System.nanoTime();
        mutex.wait(millis, (int) nanos);
        long t1 = System.nanoTime();

        return timeoutNanos - (t1 - t0);
    }
}
