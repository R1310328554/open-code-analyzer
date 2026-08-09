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
package com.lmax.disruptor.support;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.util.PaddedLong;

import java.util.concurrent.CountDownLatch;

/**
 * long 数组事件处理器：累加数组元素并统计已处理批次数。
 */
public final class LongArrayEventHandler implements EventHandler<long[]>
{
    private final PaddedLong value = new PaddedLong();
    private final PaddedLong batchesProcessed = new PaddedLong();
    private long count;
    private CountDownLatch latch;

    public long getValue()
    {
        return value.get();
    }

    public long getBatchesProcessed()
    {
        return batchesProcessed.get();
    }

    /** 重置累加值与批次计数，并绑定完成 latch。 */
    public void reset(final CountDownLatch latch, final long expectedCount)
    {
        value.set(0L);
        this.latch = latch;
        count = expectedCount;
        batchesProcessed.set(0);
    }

    @Override
    public void onEvent(final long[] event, final long sequence, final boolean endOfBatch) throws Exception
    {
        // 步骤：遍历数组并累加每个元素
        for (int i = 0; i < event.length; i++)
        {
            value.set(value.get() + event[i]);
        }

        if (--count == 0)
        {
            latch.countDown();
        }
    }

    @Override
    public void onBatchStart(final long batchSize, final long queueDepth)
    {
        batchesProcessed.increment();
    }
}
