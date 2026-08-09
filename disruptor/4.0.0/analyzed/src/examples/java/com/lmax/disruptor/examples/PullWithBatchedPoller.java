package com.lmax.disruptor.examples;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventPoller;
import com.lmax.disruptor.RingBuffer;

/**
 * EventPoller 的批量拉取用法：本地缓冲一批事件以加速轮询消费。
 */
public class PullWithBatchedPoller
{
    public static void main(final String[] args) throws Exception
    {
        int batchSize = 40;
        RingBuffer<BatchedPoller.DataEvent<Object>> ringBuffer =
                RingBuffer.createMultiProducer(BatchedPoller.DataEvent.factory(), 1024);

        BatchedPoller<Object> poller = new BatchedPoller<>(ringBuffer, batchSize);

        Object value = poller.poll();

        // 无可用事件时 value 可能为 null
        if (null != value)
        {
            // 处理取出的值
        }
    }

    static class BatchedPoller<T>
    {
        private final EventPoller<DataEvent<T>> poller;
        private final BatchedData<T> polledData;

        BatchedPoller(final RingBuffer<DataEvent<T>> ringBuffer, final int batchSize)
        {
            this.poller = ringBuffer.newPoller();
            ringBuffer.addGatingSequences(poller.getSequence());
            this.polledData = new BatchedData<>(batchSize);
        }

        public T poll() throws Exception
        {
            if (polledData.getMsgCount() > 0)
            {
                return polledData.pollMessage(); // 步骤：优先从本地缓冲取
            }

            loadNextValues(poller, polledData); // 步骤：本地为空时从 RingBuffer 批量加载
            return polledData.getMsgCount() > 0 ? polledData.pollMessage() : null;
        }

        private EventPoller.PollState loadNextValues(final EventPoller<DataEvent<T>> poller, final BatchedData<T> batch)
                throws Exception
        {
            return poller.poll((event, sequence, endOfBatch) ->
            {
                T item = event.copyOfData();
                return item != null ? batch.addDataItem(item) : false;
            });
        }

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

            void set(final T d)
            {
                data = d;
            }
        }

        private static class BatchedData<T>
        {
            private int msgHighBound;
            private final int capacity;
            private final T[] data;
            private int cursor;

            @SuppressWarnings("unchecked")
            BatchedData(final int size)
            {
                this.capacity = size;
                data = (T[]) new Object[this.capacity];
            }

            private void clearCount()
            {
                msgHighBound = 0;
                cursor = 0;
            }

            public int getMsgCount()
            {
                return msgHighBound - cursor;
            }

            public boolean addDataItem(final T item) throws IndexOutOfBoundsException
            {
                if (msgHighBound >= capacity)
                {
                    throw new IndexOutOfBoundsException("Attempting to add item to full batch");
                }

                data[msgHighBound++] = item;
                return msgHighBound < capacity;
            }

            public T pollMessage()
            {
                T rtVal = null;
                if (cursor < msgHighBound)
                {
                    rtVal = data[cursor++];
                }
                if (cursor > 0 && cursor >= msgHighBound)
                {
                    clearCount();
                }
                return rtVal;
            }
        }
    }
}
