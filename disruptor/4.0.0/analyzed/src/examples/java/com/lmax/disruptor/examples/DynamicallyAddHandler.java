/**
 * 运行时动态添加与移除 BatchEventProcessor 的示例。
 */

package com.lmax.disruptor.examples;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BatchEventProcessorBuilder;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.examples.support.StubEvent;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DynamicallyAddHandler
{
    /** 带关闭 latch 的动态处理器，onShutdown 时通知等待方。 */
    private static class DynamicHandler implements EventHandler<StubEvent>
    {
        private final CountDownLatch shutdownLatch = new CountDownLatch(1);

        @Override
        public void onEvent(final StubEvent event, final long sequence, final boolean endOfBatch)
        {
        }

        @Override
        public void onStart()
        {

        }

        @Override
        public void onShutdown()
        {
            shutdownLatch.countDown();
        }

        public void awaitShutdown() throws InterruptedException
        {
            shutdownLatch.await();
        }
    }

    public static void main(final String[] args) throws InterruptedException
    {
        ExecutorService executor = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);

        // 步骤：构建 Disruptor 并启动，获取 RingBuffer
        Disruptor<StubEvent> disruptor = new Disruptor<>(
                StubEvent.EVENT_FACTORY, 1024, DaemonThreadFactory.INSTANCE);
        RingBuffer<StubEvent> ringBuffer = disruptor.start();

        // 步骤：手动构造两个 BatchEventProcessor
        DynamicHandler handler1 = new DynamicHandler();
        BatchEventProcessor<StubEvent> processor1 =
                new BatchEventProcessorBuilder().build(ringBuffer, ringBuffer.newBarrier(), handler1);

        DynamicHandler handler2 = new DynamicHandler();
        BatchEventProcessor<StubEvent> processor2 =
                new BatchEventProcessorBuilder().build(ringBuffer, ringBuffer.newBarrier(processor1.getSequence()), handler2);

        // 步骤：将两个处理器的序号动态注册为门控序号
        ringBuffer.addGatingSequences(processor1.getSequence(), processor2.getSequence());

        // 步骤：在线程池中启动处理器
        executor.execute(processor1);
        executor.execute(processor2);

        // 步骤：移除其中一个处理器

        // 停止 processor2
        processor2.halt();
        // 等待 onShutdown 完成
        handler2.awaitShutdown();
        // 从 RingBuffer 移除对应门控序号
        ringBuffer.removeGatingSequence(processor2.getSequence());
    }
}
