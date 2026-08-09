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
 * 事件处理回调接口：当 {@link RingBuffer} 中有新事件可供消费时由框架调用。
 *
 * @param <T> 事件实现类型，在交换或并行协调过程中承载共享数据
 * @see BatchEventProcessor#setExceptionHandler(ExceptionHandler) 若需处理从处理器传播出的异常
 */
public interface EventHandler<T> extends EventHandlerBase<T>
{
    /**
     * 发布者向 {@link RingBuffer} 写入事件后，{@link BatchEventProcessor} 会批量读取并回调此方法。
     * 一批次指在不等待新事件到达的前提下，当前可处理的所有连续事件。
     * 适合需要将多个事件的 I/O 等慢操作合并的场景。
     * 实现方应确保在 {@code endOfBatch} 为 {@code true} 时执行必要的收尾操作，
     * 因为距下一事件到达的时间不确定。
     *
     * @param event      来自 {@link RingBuffer} 的事件对象
     * @param sequence   当前处理事件的序号
     * @param endOfBatch 是否为当前批次的最后一个事件
     * @throws Exception 若希望由上层继续处理该异常
     */
    @Override
    void onEvent(T event, long sequence, boolean endOfBatch) throws Exception;

    /**
     * 由 {@link BatchEventProcessor} 调用，允许处理器在 {@link EventHandler#onEvent(Object, long, boolean)}
     * 返回之后异步推进消费序号（例如批量写盘完成后）。
     *
     * <p>典型场景：处理器执行批量 I/O，操作完成后再调用 {@link Sequence#set} 更新序号，
     * 使依赖该处理器的下游得以继续推进。
     *
     * @param sequenceCallback 用于通知 {@link BatchEventProcessor} 序号已推进的回调
     */
    default void setSequenceCallback(Sequence sequenceCallback)
    {
    }
}
