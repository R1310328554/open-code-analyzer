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

/**
 * {@link EventProcessor} 的实现需作为 {@link Runnable}，按所选 {@link WaitStrategy} 从 {@link RingBuffer} 轮询事件。
 * 通常无需自行实现此接口，优先使用 {@link EventHandler} 配合内置的 {@link BatchEventProcessor}。
 *
 * <p>事件处理器一般会绑定到专用线程上运行。
 */
public interface EventProcessor extends Runnable
{
    /**
     * 获取本 {@link EventProcessor} 正在使用的 {@link Sequence}。
     *
     * @return 本处理器的 {@link Sequence} 引用
     */
    Sequence getSequence();

    /**
     * 通知处理器在下一个安全断点停止消费。
     * 内部会调用 {@link SequenceBarrier#alert()} 唤醒等待中的线程以检查状态。
     */
    void halt();

    /**
     * @return 处理器是否仍在运行；理想情况下仅在线程空闲时返回 {@code false}
     */
    boolean isRunning();
}
