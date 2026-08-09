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
 * 聚合多个 {@link EventHandler}，对每个事件按注册顺序依次调用。
 *
 * @param <T> 事件实现类型，在交换或并行协调过程中承载共享数据
 */
public final class AggregateEventHandler<T>
    implements EventHandler<T>
{
    private final EventHandler<T>[] eventHandlers;

    /**
     * 构造按序调用的 {@link EventHandler} 聚合器。
     *
     * @param eventHandlers 将按顺序被调用的处理器数组
     */
    @SafeVarargs
    public AggregateEventHandler(final EventHandler<T>... eventHandlers)
    {
        this.eventHandlers = eventHandlers;
    }

    @Override
    public void onEvent(final T event, final long sequence, final boolean endOfBatch)
        throws Exception
    {
        for (final EventHandler<T> eventHandler : eventHandlers)
        {
            eventHandler.onEvent(event, sequence, endOfBatch);
        }
    }

    @Override
    public void onStart()
    {
        for (final EventHandler<T> eventHandler : eventHandlers)
        {
            eventHandler.onStart();
        }
    }

    @Override
    public void onShutdown()
    {
        for (final EventHandler<T> eventHandler : eventHandlers)
        {
            eventHandler.onShutdown();
        }
    }
}
