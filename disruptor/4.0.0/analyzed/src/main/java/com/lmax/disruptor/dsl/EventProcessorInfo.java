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

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;

import java.util.concurrent.ThreadFactory;

/**
 * 包装特定事件处理阶段的辅助类。
 *
 * <p>跟踪事件处理器实例、事件处理器实例，以及该阶段所附着的序号屏障。</p>
 */
class EventProcessorInfo implements ConsumerInfo
{
    private final EventProcessor eventprocessor;
    private final SequenceBarrier barrier;
    private boolean endOfChain = true;

    EventProcessorInfo(final EventProcessor eventprocessor, final SequenceBarrier barrier)
    {
        this.eventprocessor = eventprocessor;
        this.barrier = barrier;
    }

    public EventProcessor getEventProcessor()
    {
        return eventprocessor;
    }

    @Override
    public Sequence[] getSequences()
    {
        return new Sequence[]{eventprocessor.getSequence()};
    }

    @Override
    public SequenceBarrier getBarrier()
    {
        return barrier;
    }

    @Override
    public boolean isEndOfChain()
    {
        return endOfChain;
    }

    @Override
    public void start(final ThreadFactory threadFactory)
    {
        final Thread thread = threadFactory.newThread(eventprocessor);
        if (null == thread)
        {
            throw new RuntimeException("Failed to create thread to run: " + eventprocessor);
        }

        thread.start();
    }

    @Override
    public void halt()
    {
        eventprocessor.halt();
    }

    /**
     * 标记本处理器已被其他屏障引用，不再视为依赖链末端。
     */
    @Override
    public void markAsUsedInBarrier()
    {
        endOfChain = false;
    }

    @Override
    public boolean isRunning()
    {
        return eventprocessor.isRunning();
    }
}
