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
package com.lmax.disruptor.translator;

import com.lmax.disruptor.AbstractPerfTestDisruptor;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.PerfTestContext;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.support.PerfTestUtil;
import com.lmax.disruptor.support.ValueAdditionEventHandler;
import com.lmax.disruptor.support.ValueEvent;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.lmax.disruptor.util.MutableLong;

import java.util.concurrent.CountDownLatch;

import static com.lmax.disruptor.support.PerfTestUtil.failIfNot;

/**
 * <pre>
 * 单播：1 个发布者与 1 个事件处理器之间通过 EventTranslator API 传递一系列事件。
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
public final class OneToOneTranslatorThroughputTest extends AbstractPerfTestDisruptor
{
    private static final int BUFFER_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 1000L * 100L;
    private final long expectedResult = PerfTestUtil.accumulatedAddition(ITERATIONS);
    private final ValueAdditionEventHandler handler = new ValueAdditionEventHandler();
    private final RingBuffer<ValueEvent> ringBuffer;
    private final MutableLong value = new MutableLong(0);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public OneToOneTranslatorThroughputTest()
    {
        Disruptor<ValueEvent> disruptor =
                new Disruptor<>(
                        ValueEvent.EVENT_FACTORY,
                        BUFFER_SIZE, DaemonThreadFactory.INSTANCE,
                        ProducerType.SINGLE,
                        new YieldingWaitStrategy());
        disruptor.handleEventsWith(handler);
        this.ringBuffer = disruptor.start();
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
        MutableLong value = this.value;

        final CountDownLatch latch = new CountDownLatch(1);
        long expectedCount = ringBuffer.getMinimumGatingSequence() + ITERATIONS;

        handler.reset(latch, expectedCount);
        long start = System.currentTimeMillis();

        final RingBuffer<ValueEvent> rb = ringBuffer;

        for (long l = 0; l < ITERATIONS; l++)
        {
            value.set(l);
            // 步骤：通过 EventTranslatorOneArg 发布事件
            rb.publishEvent(Translator.INSTANCE, value);
        }

        latch.await();
        perfTestContext.setDisruptorOps((ITERATIONS * 1000L) / (System.currentTimeMillis() - start));
        perfTestContext.setBatchData(handler.getBatchesProcessed(), ITERATIONS);
        waitForEventProcessorSequence(expectedCount);

        failIfNot(expectedResult, handler.getValue());

        return perfTestContext;
    }

    private static class Translator implements EventTranslatorOneArg<ValueEvent, MutableLong>
    {
        private static final Translator INSTANCE = new Translator();

        @Override
        public void translateTo(final ValueEvent event, final long sequence, final MutableLong arg0)
        {
            // 步骤：从 MutableLong 参数写入事件值
            event.setValue(arg0.get());
        }
    }

    private void waitForEventProcessorSequence(final long expectedCount) throws InterruptedException
    {
        while (ringBuffer.getMinimumGatingSequence() != expectedCount)
        {
            Thread.sleep(1);
        }
    }

    public static void main(final String[] args) throws Exception
    {
        OneToOneTranslatorThroughputTest test = new OneToOneTranslatorThroughputTest();
        test.testImplementations();
    }
}
