/**
 * 事件处理异常时通过 ExceptionHandler 决定是否终止 Disruptor 的示例。
 */

package com.lmax.disruptor.examples;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class ShutdownOnError
{
    private static class Event
    {
        public long value;

        public static final EventFactory<Event> FACTORY = Event::new;
    }

    private static class Handler implements EventHandler<Event>
    {
        @Override
        public void onEvent(final Event event, final long sequence, final boolean endOfBatch)
        {
            // 执行业务逻辑，失败时抛出异常
        }
    }

    private static final class ErrorHandler implements ExceptionHandler<Event>
    {
        private final AtomicBoolean running;

        private ErrorHandler(final AtomicBoolean running)
        {
            this.running = running;
        }

        @Override
        public void handleEventException(final Throwable ex, final long sequence, final Event event)
        {
            // 步骤：致命异常重新抛出以触发 Disruptor 关闭
            if (execeptionIsFatal(ex))
            {
                throw new RuntimeException(ex);
            }
        }

        private boolean execeptionIsFatal(final Throwable ex)
        {
            // 按业务规则判定异常是否致命
            return true;
        }

        @Override
        public void handleOnStartException(final Throwable ex)
        {

        }

        @Override
        public void handleOnShutdownException(final Throwable ex)
        {

        }
    }

    public static void main(final String[] args)
    {
        Disruptor<Event> disruptor = new Disruptor<>(Event.FACTORY, 1024, DaemonThreadFactory.INSTANCE);

        AtomicBoolean running = new AtomicBoolean(true);

        ErrorHandler errorHandler = new ErrorHandler(running);

        final Handler handler = new Handler();
        disruptor.handleEventsWith(handler);
        disruptor.handleExceptionsFor(handler).with(errorHandler);

        simplePublish(disruptor, running);
    }

    private static void simplePublish(final Disruptor<Event> disruptor, final AtomicBoolean running)
    {
        while (running.get())
        {
            disruptor.publishEvent((event, sequence) -> event.value = sequence);
        }
    }

    private static void smarterPublish(final Disruptor<Event> disruptor, final AtomicBoolean running)
    {
        final RingBuffer<Event> ringBuffer = disruptor.getRingBuffer();

        boolean publishOk;
        do
        {
            publishOk = ringBuffer.tryPublishEvent((event, sequence) -> event.value = sequence);
        }
        while (publishOk && running.get());
    }
}
