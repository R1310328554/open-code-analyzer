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
package com.lmax.disruptor.dsl;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.EventHandlerIdentity;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.ExceptionHandler;

/**
 * 为特定事件处理器配置异常处理器的辅助类。
 * 例如：
 * <pre><code>disruptorWizard.handleExceptionsIn(eventHandler).with(exceptionHandler);</code></pre>
 *
 * @param <T> 正在处理的事件类型
 */
public class ExceptionHandlerSetting<T>
{
    private final EventHandlerIdentity handlerIdentity;
    private final ConsumerRepository consumerRepository;

    ExceptionHandlerSetting(
        final EventHandlerIdentity handlerIdentity,
        final ConsumerRepository consumerRepository)
    {
        this.handlerIdentity = handlerIdentity;
        this.consumerRepository = consumerRepository;
    }

    /**
     * 为事件处理器指定 {@link ExceptionHandler}。
     *
     * @param exceptionHandler 要使用的异常处理器
     */
    @SuppressWarnings("unchecked")
    public void with(final ExceptionHandler<? super T> exceptionHandler)
    {
        final EventProcessor eventProcessor = consumerRepository.getEventProcessorFor(handlerIdentity);
        if (eventProcessor instanceof BatchEventProcessor)
        {
            ((BatchEventProcessor<T>) eventProcessor).setExceptionHandler(exceptionHandler);
            consumerRepository.getBarrierFor(handlerIdentity).alert();
        }
        else
        {
            throw new RuntimeException(
                "EventProcessor: " + eventProcessor + " is not a BatchEventProcessor " +
                "and does not support exception handlers");
        }
    }
}
