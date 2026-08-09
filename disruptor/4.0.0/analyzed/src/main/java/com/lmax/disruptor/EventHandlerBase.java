/*
 * Copyright 2022 LMAX Ltd.
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
 * {@link EventHandler} 与 {@link RewindableEventHandler} 的公共基类接口。
 */
interface EventHandlerBase<T> extends EventHandlerIdentity
{
    /**
     * 发布者向 {@link RingBuffer} 写入事件后，{@link BatchEventProcessor} 会批量读取并回调此方法。
     * 一批次指在不等待新事件到达的前提下，当前可处理的所有连续事件。
     * 适合需要将多个事件的 I/O 等慢操作合并的场景。
     * 实现方应确保在 {@code endOfBatch} 为 {@code true} 时执行必要的收尾操作。
     *
     * @param event      来自 {@link RingBuffer} 的事件对象
     * @param sequence   当前处理事件的序号
     * @param endOfBatch 是否为当前批次的最后一个事件
     * @throws Throwable 若希望由上层处理异常；抛出 {@link RewindableException} 可触发批次回退
     */
    void onEvent(T event, long sequence, boolean endOfBatch) throws Throwable;

    /**
     * 在 {@link BatchEventProcessor} 开始处理一批事件之前调用。
     *
     * @param batchSize  即将处理的批次大小
     * @param queueDepth 队列中待处理事件总数（含本批次）
     */
    default void onBatchStart(long batchSize, long queueDepth)
    {
    }

    /**
     * 处理线程启动后、首个事件到达前调用一次。
     */
    default void onStart()
    {
    }

    /**
     * 事件处理线程即将关闭前调用一次。
     *
     * <p>调用时序号处理已停止，此后不会再有事件被处理。
     */
    default void onShutdown()
    {
    }

    /**
     * 当 {@link BatchEventProcessor} 的 {@link WaitStrategy} 抛出 {@link TimeoutException} 时调用。
     *
     * @param sequence 上次成功处理的序号
     * @throws Exception 若实现方无法处理该超时
     */
    default void onTimeout(long sequence) throws Exception
    {
    }
}
