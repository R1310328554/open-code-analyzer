/**
 * 等待特定消费者或 RingBuffer 空闲的处理同步示例。
 */

package com.lmax.disruptor.examples;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.examples.support.LongEvent;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class WaitForProcessing
{
    /** 占位消费者，用于演示序号等待。 */
    public static class Consumer implements EventHandler<LongEvent>
    {
        @Override
        public void onEvent(final LongEvent event, final long sequence, final boolean endOfBatch)
        {

        }
    }

    public static void main(final String[] args)
    {
        final Disruptor<LongEvent> disruptor = new Disruptor<>(
            LongEvent.FACTORY, 1024, DaemonThreadFactory.INSTANCE);

        Consumer firstConsumer = new Consumer();
        Consumer lastConsumer = new Consumer();
        disruptor.handleEventsWith(firstConsumer).then(lastConsumer);
        final RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        // 步骤：发布一条带偏移赋值的事件
        EventTranslator<LongEvent> translator = (event, sequence) -> event.set(sequence - 4);

        ringBuffer.tryPublishEvent(translator);

        waitForSpecificConsumer(disruptor, lastConsumer, ringBuffer);
        waitForRingBufferToBeIdle(ringBuffer);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static void waitForRingBufferToBeIdle(final RingBuffer<LongEvent> ringBuffer)
    {
        while (ringBuffer.getBufferSize() - ringBuffer.remainingCapacity() != 0)
        {
            // 等待 RingBuffer 中事件全部被消费
        }
    }

    private static void waitForSpecificConsumer(
        final Disruptor<LongEvent> disruptor,
        final Consumer lastConsumer,
        final RingBuffer<LongEvent> ringBuffer)
    {
        long lastPublishedValue;
        long sequenceValueFor;
        // 步骤：轮询直到 lastConsumer 追上已发布游标
        do
        {
            lastPublishedValue = ringBuffer.getCursor();
            sequenceValueFor = disruptor.getSequenceValueFor(lastConsumer);
        }
        while (sequenceValueFor < lastPublishedValue);
    }
}
