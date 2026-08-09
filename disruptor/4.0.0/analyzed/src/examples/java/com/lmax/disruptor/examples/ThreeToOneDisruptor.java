/**
 * 三并行转换后汇聚到单一 CollatingHandler 的示例。
 */

package com.lmax.disruptor.examples;


import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class ThreeToOneDisruptor
{
    /** 输入与一个固定长度 output 数组的事件载体。 */
    public static class DataEvent
    {
        Object input;
        Object[] output;

        public DataEvent(final int size)
        {
            output = new Object[size];
        }

        public static final EventFactory<DataEvent> FACTORY = () -> new DataEvent(3);
    }

    /** 并行转换处理器，写入 output 的指定索引。 */
    public static class TransformingHandler implements EventHandler<DataEvent>
    {
        private final int outputIndex;

        public TransformingHandler(final int outputIndex)
        {
            this.outputIndex = outputIndex;
        }

        @Override
        public void onEvent(final DataEvent event, final long sequence, final boolean endOfBatch)
        {
            // 步骤：将 input 转换后写入 output[outputIndex]
            event.output[outputIndex] = doSomething(event.input);
        }

        private Object doSomething(final Object input)
        {
            // 在此执行具体转换
            return input;
        }
    }

    /** 汇聚处理器：三路 output 齐备后执行 collate。 */
    public static class CollatingHandler implements EventHandler<DataEvent>
    {
        @Override
        public void onEvent(final DataEvent event, final long sequence, final boolean endOfBatch)
        {
            collate(event.output);
        }

        private void collate(final Object[] output)
        {
            // 在此执行汇聚逻辑
        }
    }

    public static void main(final String[] args)
    {
        Disruptor<DataEvent> disruptor = new Disruptor<>(
                DataEvent.FACTORY, 1024, DaemonThreadFactory.INSTANCE);

        TransformingHandler handler1 = new TransformingHandler(0);
        TransformingHandler handler2 = new TransformingHandler(1);
        TransformingHandler handler3 = new TransformingHandler(2);
        CollatingHandler collator = new CollatingHandler();

        disruptor.handleEventsWith(handler1, handler2, handler3).then(collator);

        disruptor.start();
    }
}
