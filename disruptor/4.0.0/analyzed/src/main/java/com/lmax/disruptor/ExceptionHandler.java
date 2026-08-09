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
 * {@link BatchEventProcessor} 事件处理循环中未捕获异常的回调处理器。
 *
 * @param <T> 事件实现类型，在交换或并行协调过程中承载共享数据
 */
public interface ExceptionHandler<T>
{
    /**
     * <p>处理事件时未捕获异常的策略。</p>
     *
     * <p>若希望终止 {@link BatchEventProcessor} 的后续处理，应抛出 {@link RuntimeException}。</p>
     *
     * @param ex       从 {@link EventHandler} 传播出的异常
     * @param sequence 引发异常的事件序号
     * @param event    异常发生时正在处理的事件，可能为 null
     */
    void handleEventException(Throwable ex, long sequence, T event);

    /**
     * 在 {@link EventHandler#onStart()} 执行期间发生异常时的回调。
     *
     * @param ex 启动过程中抛出的异常
     */
    void handleOnStartException(Throwable ex);

    /**
     * 在 {@link EventHandler#onShutdown()} 执行期间发生异常时的回调。
     *
     * @param ex 关闭过程中抛出的异常
     */
    void handleOnShutdownException(Throwable ex);
}
