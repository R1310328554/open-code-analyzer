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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 不执行实际业务逻辑的 {@link EventProcessor} 实现，仅跟踪一个 {@link Sequence}。
 *
 * <p>适用于测试场景，或发布者预填充 {@link RingBuffer} 时作为占位消费者。
 */
public final class NoOpEventProcessor implements EventProcessor
{
    private final SequencerFollowingSequence sequence;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 构造仅跟踪 {@link Sequence} 的 {@link EventProcessor}。
     *
     * @param sequencer 要跟踪的序号器
     */
    public NoOpEventProcessor(final RingBuffer<?> sequencer)
    {
        sequence = new SequencerFollowingSequence(sequencer);
    }

    @Override
    public Sequence getSequence()
    {
        return sequence;
    }

    @Override
    public void halt()
    {
        running.set(false);
    }

    @Override
    public boolean isRunning()
    {
        return running.get();
    }

    @Override
    public void run()
    {
        if (!running.compareAndSet(false, true))
        {
            throw new IllegalStateException("Thread is already running");
        }
    }

    /**
     * 包装并跟随另一序号源的 {@link Sequence} 实现
     */
    private static final class SequencerFollowingSequence extends Sequence
    {
        private final RingBuffer<?> sequencer;

        private SequencerFollowingSequence(final RingBuffer<?> sequencer)
        {
            super(Sequencer.INITIAL_CURSOR_VALUE);
            this.sequencer = sequencer;
        }

        @Override
        public long get()
        {
            return sequencer.getCursor();
        }
    }
}
