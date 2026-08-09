/**
 * 使用 EventPoller 单条拉取事件的示例。
 */

package com.lmax.disruptor.examples;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventPoller;
import com.lmax.disruptor.RingBuffer;

public class PullWithPoller
{
    public static class DataEvent<T>
    {
        T data;

        public static <T> EventFactory<DataEvent<T>> factory()
        {
            return DataEvent::new;
        }

        public T copyOfData()
        {
            // 此处拷贝数据；单引用对象传引用即可，复用 byte[] 时需深拷贝内容。
            return data;
        }
    }

    public static void main(final String[] args) throws Exception
    {
        RingBuffer<DataEvent<Object>> ringBuffer = RingBuffer.createMultiProducer(DataEvent.factory(), 1024);

        final EventPoller<DataEvent<Object>> poller = ringBuffer.newPoller();

        Object value = getNextValue(poller);

        // 无可用事件时 value 可能为 null
        if (null != value)
        {
            // 处理取出的值
        }
    }

    private static Object getNextValue(final EventPoller<DataEvent<Object>> poller) throws Exception
    {
        final Object[] out = new Object[1];

        poller.poll(
                (event, sequence, endOfBatch) ->
                {
                    out[0] = event.copyOfData();

                    // 返回 false 表示每次 poll 只处理一条事件
                    return false;
                });

        return out[0];
    }
}
