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
 * 处理 {@link RingBuffer} 中可用事件的回调接口，支持在暂时无法处理时抛出 {@link RewindableException} 以触发重试。
 *
 * @param <T> 事件中承载的数据类型
 * @see BatchEventProcessor#setExceptionHandler(ExceptionHandler) 若需处理从处理器传播出的其他异常
 */
public interface RewindableEventHandler<T> extends EventHandlerBase<T>
{
    /**
     * 当发布者向 {@link RingBuffer} 发布事件时调用。{@link BatchEventProcessor} 以批次方式读取事件：
     * 一批指在不等待新事件到达的前提下，当前可处理的所有事件。
     * 对需要较慢操作（如 I/O）的处理器，可将多个事件合并为一次操作。
     * 实现应确保在 {@code endOfBatch} 为 {@code true} 时执行该操作，因为与下一条消息之间的间隔不确定。
     *
     * @param event 发布到 {@link RingBuffer} 的事件
     * @param sequence 正在处理的事件序号
     * @param endOfBatch 是否为当前批次的最后一个事件
     * @throws RewindableException 若希望批次处理器重新处理整个批次
     * @throws Exception 若希望将异常交由上层链处理
     */
    @Override
    void onEvent(T event, long sequence, boolean endOfBatch) throws RewindableException, Exception;
}
