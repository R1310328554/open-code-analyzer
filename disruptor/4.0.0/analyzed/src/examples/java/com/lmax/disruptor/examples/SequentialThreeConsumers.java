/**
 * 三个顺序消费者链式传递字段的示例：a → b → c → d。
 */

package com.lmax.disruptor.examples;

import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class SequentialThreeConsumers
{
    /** 在流水线各阶段间传递的四个字段槽位。 */
    private static class MyEvent
    {
        private Object a;
        private Object b;
        private Object c;
        private Object d;
    }

    public static void main(final String[] args)
    {
        Disruptor<MyEvent> disruptor = new Disruptor<>(MyEvent::new, 1024, DaemonThreadFactory.INSTANCE);

        // 步骤：三阶段顺序消费者，字段逐级传递
        disruptor.handleEventsWith((event, sequence, endOfBatch) -> event.b = event.a)
                .then((event, sequence, endOfBatch) -> event.c = event.b)
                .then((event, sequence, endOfBatch) -> event.d = event.c);

        disruptor.start();
    }
}
