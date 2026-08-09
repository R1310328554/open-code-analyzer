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

/**
 * 协调对数据结构的序号申领，并跟踪依赖的 {@link Sequence}。
 */
public interface Sequencer extends Cursored, Sequenced
{
    /**
     * 序号起始值，设为 -1。
     */
    long INITIAL_CURSOR_VALUE = -1L;

    /**
     * 申领指定序号。仅用于将环形缓冲区初始化到特定位置。
     *
     * @param sequence 要初始化到的序号
     */
    void claim(long sequence);

    /**
     * 非阻塞地确认某序号是否已发布、事件是否可读。
     *
     * @param sequence 要检查的缓冲区序号
     * @return 若该序号可用则返回 {@code true}，否则 {@code false}
     */
    boolean isAvailable(long sequence);

    /**
     * 将门控序号安全、原子地加入本 Disruptor 实例。
     *
     * @param gatingSequences 要添加的序号
     */
    void addGatingSequences(Sequence... gatingSequences);

    /**
     * 从本序号器中移除指定序号。
     *
     * @param sequence 要移除的序号
     * @return 若找到并移除则返回 <code>true</code>，否则 <code>false</code>
     */
    boolean removeGatingSequence(Sequence sequence);

    /**
     * 创建供 {@link EventProcessor} 使用的 {@link SequenceBarrier}，
     * 用于判断环形缓冲区中哪些消息已可读。
     *
     * @param sequencesToTrack 新屏障需要等待的全部序号
     * @return 跟踪指定序号的序列屏障
     * @see SequenceBarrier
     */
    SequenceBarrier newBarrier(Sequence... sequencesToTrack);

    /**
     * 获取已添加的全部门控序号中的最小值。
     *
     * @return 最小门控序号；若尚未添加门控序号则返回游标序号
     */
    long getMinimumSequence();

    /**
     * 获取环形缓冲区中可安全读取的最高序号。
     * 具体实现可能需要在 {@code nextSequence} 到 {@code availableSequence} 范围内扫描。
     * 若不存在 {@code >= nextSequence} 的可用值，则返回 {@code nextSequence - 1}。
     * 消费者应传入「上次成功处理序号 + 1」作为 {@code nextSequence}。
     *
     * @param nextSequence      扫描起始序号
     * @param availableSequence 扫描上界序号
     * @return 可安全读取的最高序号，至少为 <code>nextSequence - 1</code>
     */
    long getHighestPublishedSequence(long nextSequence, long availableSequence);

    /**
     * 基于本序号器创建事件轮询器。
     *
     * @param provider         事件数据来源
     * @param gatingSequences  门控序号
     * @param <T>              事件类型
     * @return 事件轮询器
     */
    <T> EventPoller<T> newPoller(DataProvider<T> provider, Sequence... gatingSequences);
}
