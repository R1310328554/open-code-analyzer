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
 * 让出 CPU 的等待策略：{@link EventProcessor} 在屏障上先忙等自旋，随后使用 {@link Thread#yield()}。
 *
 * <p>会占用较高 CPU，但相比纯忙等策略，在其他线程需要 CPU 时更易让出资源。
 */
public final class YieldingWaitStrategy implements WaitStrategy
{
    private static final int SPIN_TRIES = 100;

    @Override
    public long waitFor(
        final long sequence, final Sequence cursor, final Sequence dependentSequence, final SequenceBarrier barrier)
        throws AlertException, InterruptedException
    {
        long availableSequence;
        int counter = SPIN_TRIES;

        while ((availableSequence = dependentSequence.get()) < sequence)
        {
            counter = applyWaitMethod(barrier, counter);
        }

        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking()
    {
    }

    private int applyWaitMethod(final SequenceBarrier barrier, final int counter)
        throws AlertException
    {
        barrier.checkAlert();

        if (0 == counter)
        {
            Thread.yield();
        }
        else
        {
            return counter - 1;
        }

        return counter;
    }
}
