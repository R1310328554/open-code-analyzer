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

import com.lmax.disruptor.util.Util;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * 可动态增删 {@link Sequence} 且线程安全的 {@link Sequence} 组。
 *
 * <p>{@link SequenceGroup#get()} 与 {@link SequenceGroup#set(long)} 为无锁实现，
 * 可与 {@link SequenceGroup#add(Sequence)}、{@link SequenceGroup#remove(Sequence)} 并发调用。
 */
public final class SequenceGroup extends Sequence
{
    private static final AtomicReferenceFieldUpdater<SequenceGroup, Sequence[]> SEQUENCE_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(SequenceGroup.class, Sequence[].class, "sequences");
    private volatile Sequence[] sequences = new Sequence[0];

    /**
     * 默认构造器
     */
    public SequenceGroup()
    {
        super(-1);
    }

    /**
     * 获取组内最小序号值。
     *
     * @return 组内最小序号
     */
    @Override
    public long get()
    {
        return Util.getMinimumSequence(sequences);
    }

    /**
     * 将组内所有 {@link Sequence} 设为给定值。
     *
     * @param value 要设置的序号值
     */
    @Override
    public void set(final long value)
    {
        final Sequence[] sequences = this.sequences;
        for (Sequence sequence : sequences)
        {
            sequence.set(value);
        }
    }

    /**
     * 向本聚合体添加 {@link Sequence}，仅应在初始化阶段使用。
     * 运行期请使用 {@link SequenceGroup#addWhileRunning(Cursored, Sequence)}。
     *
     * @param sequence 要添加的序号
     * @see SequenceGroup#addWhileRunning(Cursored, Sequence)
     */
    public void add(final Sequence sequence)
    {
        Sequence[] oldSequences;
        Sequence[] newSequences;
        do
        {
            oldSequences = sequences;
            final int oldSize = oldSequences.length;
            newSequences = new Sequence[oldSize + 1];
            System.arraycopy(oldSequences, 0, newSequences, 0, oldSize);
            newSequences[oldSize] = sequence;
        }
        while (!SEQUENCE_UPDATER.compareAndSet(this, oldSequences, newSequences));
    }

    /**
     * 从本聚合体移除 {@link Sequence} 的首次出现。
     *
     * @param sequence 要移除的序号
     * @return 成功移除返回 {@code true}，否则返回 {@code false}
     */
    public boolean remove(final Sequence sequence)
    {
        return SequenceGroups.removeSequence(this, SEQUENCE_UPDATER, sequence);
    }

    /**
     * 获取组的大小。
     *
     * @return 组内序号数量
     */
    public int size()
    {
        return sequences.length;
    }

    /**
     * 在 Disruptor 已开始发布后向序号组添加序号。
     * 添加后会将新序号设为 RingBuffer 当前游标，以避免回卷/环绕带来的副作用。
     *
     * @param cursored 本序号组所属方从中拉取事件的数据结构
     * @param sequence 要添加的序号
     */
    public void addWhileRunning(final Cursored cursored, final Sequence sequence)
    {
        SequenceGroups.addSequences(this, SEQUENCE_UPDATER, cursored, sequence);
    }
}
