/*
 * Copyright 2012 LMAX Ltd.
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

import com.lmax.disruptor.util.Util;

import java.util.Arrays;

/**
 * 将一组 {@link Sequence} 聚合为单个 {@link Sequence}，对外暴露组内最小序号。
 */
public final class FixedSequenceGroup extends Sequence
{
    private final Sequence[] sequences;

    /**
     * 构造固定序号组。
     *
     * @param sequences 本组需要跟踪的序号列表
     */
    public FixedSequenceGroup(final Sequence[] sequences)
    {
        this.sequences = Arrays.copyOf(sequences, sequences.length);
    }

    /**
     * 获取组内最小序号值。
     *
     * @return 组内最小序号值
     */
    @Override
    public long get()
    {
        return Util.getMinimumSequence(sequences);
    }

    @Override
    public String toString()
    {
        return Arrays.toString(sequences);
    }

    /**
     * 不支持写操作。
     */
    @Override
    public void set(final long value)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 不支持写操作。
     */
    @Override
    public boolean compareAndSet(final long expectedValue, final long newValue)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 不支持写操作。
     */
    @Override
    public long incrementAndGet()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * 不支持写操作。
     */
    @Override
    public long addAndGet(final long increment)
    {
        throw new UnsupportedOperationException();
    }
}
