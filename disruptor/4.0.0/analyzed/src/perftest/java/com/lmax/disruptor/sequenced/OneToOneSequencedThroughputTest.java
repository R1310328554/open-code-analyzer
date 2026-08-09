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
package com.lmax.disruptor.sequenced;

import com.lmax.disruptor.AbstractPerfTestDisruptor;
import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BatchEventProcessorBuilder;
import com.lmax.disruptor.PerfTestContext;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.support.PerfTestUtil;
import com.lmax.disruptor.support.ValueAdditionEventHandler;
import com.lmax.disruptor.support.ValueEvent;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;
import static com.lmax.disruptor.support.PerfTestUtil.failIfNot;

/**
 * <pre>
 * 单播：1 个发布者与 1 个事件处理器之间传递一系列事件。
 *
 * +----+    +-----+
 * | P1 |--->| EP1 |
 * +----+    +-----+
 *
 * Disruptor：
 * ==========
 *              跟踪序号以防环绕
 *              +------------------+
 *              |                  |
 *              |                  v
 * +----+    +====+    +====+   +-----+
 * | P1 |--->| RB |<---| SB |   | EP1 |
 * +----+    +====+    +====+   +-----+
 *      claim      get    ^        |
 *                        |        |
 *                        +--------+
 *                          waitFor
 *
 * P1  - 发布者 1
 * RB  - 环形缓冲区
 * SB  - 序号屏障
 * EP1 - 事件处理器 1
 *
 * </pre>
 */
public final class OneToOneSequencedThroughputTest extends AbstractPerfTestDisruptor
{
    private static final int BUFFER_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 100L;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(DaemonThreadFactory.INSTANCE);
    private final long expectedResult = PerfTestUtil.accumulatedAddition(ITERATIONS);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final RingBuffer<ValueEvent> ringBuffer =
        createSingleProducer(ValueEvent.EVENT_FACTORY, BUFFER_SIZE, new YieldingWaitStrategy());
    private final SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();
    private final ValueAdditionEventHandler handler = new ValueAdditionEventHandler();
    private final BatchEventProcessor<ValueEvent> batchEventProcessor =
            new BatchEventProcessorBuilder().build(ringBuffer, sequenceBarrier, handler);

    {
        ringBuffer.addGatingSequences(batchEventProcessor.getSequence());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected int getRequiredProcessorCount()
    {
        return 2;
    }

    @Override
    protected PerfTestContext runDisruptorPass() throws InterruptedException
    {
        PerfTestContext perfTestContext = new PerfTestContext();
        final CountDownLatch latch = new CountDownLatch(1);
        long expectedCount = batchEventProcessor.getSequence().get() + ITERATIONS;
        handler.reset(latch, expectedCount);
        executor.submit(batchEventProcessor);
        long start = System.currentTimeMillis();

        final RingBuffer<ValueEvent> rb = ringBuffer;

        for (long i = 0; i < ITERATIONS; i++)
        {
            long next = rb.next();
            rb.get(next).setValue(i);
            rb.publish(next);
        }

        latch.await();
        perfTestContext.setDisruptorOps((ITERATIONS * 1000L) / (System.currentTimeMillis() - start));
        perfTestContext.setBatchData(handler.getBatchesProcessed(), ITERATIONS);
        waitForEventProcessorSequence(expectedCount);
        batchEventProcessor.halt();

        failIfNot(expectedResult, handler.getValue());

        return perfTestContext;
    }

    private void waitForEventProcessorSequence(final long expectedCount) throws InterruptedException
    {
        while (batchEventProcessor.getSequence().get() != expectedCount)
        {
            Thread.sleep(1);
        }
    }

    public static void main(final String[] args) throws Exception
    {
        OneToOneSequencedThroughputTest test = new OneToOneSequencedThroughputTest();
        test.testImplementations();
    }
}
