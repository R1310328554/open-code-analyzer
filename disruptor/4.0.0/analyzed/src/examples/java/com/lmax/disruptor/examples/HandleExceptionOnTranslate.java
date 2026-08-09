/**
 * 翻译阶段抛出异常时的处理示例：publishEvent 捕获异常，handler 识别丢弃事件。
 */

package com.lmax.disruptor.examples;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.examples.support.LongEvent;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class HandleExceptionOnTranslate
{
    private static final int NO_VALUE_SPECIFIED = -1;

    /** 识别 NO_VALUE_SPECIFIED 标记的已丢弃事件。 */
    private static class MyHandler implements EventHandler<LongEvent>
    {

        @Override
        public void onEvent(final LongEvent event, final long sequence, final boolean endOfBatch)
        {
            if (event.get() == NO_VALUE_SPECIFIED)
            {
                System.out.printf("Discarded%n");
            }
            else
            {
                System.out.printf("Processed: %s%n", event.get() == sequence);
            }
        }
    }

    public static void main(final String[] args) throws InterruptedException
    {
        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent.FACTORY, 1024, DaemonThreadFactory.INSTANCE);

        disruptor.handleEventsWith(new MyHandler());

        disruptor.start();

        EventTranslator<LongEvent> t = (event, sequence) ->
        {
            event.set(NO_VALUE_SPECIFIED);

            // 步骤：每第三条序号在翻译阶段抛异常，事件被标记为丢弃
            if (sequence % 3 == 0)
            {
                throw new RuntimeException("Skipping");
            }

            event.set(sequence);
        };

        for (int i = 0; i < 10; i++)
        {
            try
            {
                disruptor.publishEvent(t);
            }
            catch (RuntimeException e)
            {
                // 翻译失败，跳过本次发布
            }
        }

        Thread.sleep(5000);
    }
}
