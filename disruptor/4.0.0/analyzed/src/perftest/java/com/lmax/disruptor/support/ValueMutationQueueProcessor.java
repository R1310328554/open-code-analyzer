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
package com.lmax.disruptor.support;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * 基于阻塞队列的数值变异处理器：用 {@link Operation} 对累加器逐条施加二元运算。
 */
public final class ValueMutationQueueProcessor implements Runnable
{
    private volatile boolean running;
    private long value;
    private long sequence;
    private CountDownLatch latch;

    private final BlockingQueue<Long> blockingQueue;
    private final Operation operation;
    private final long count;

    public ValueMutationQueueProcessor(
        final BlockingQueue<Long> blockingQueue, final Operation operation, final long count)
    {
        this.blockingQueue = blockingQueue;
        this.operation = operation;
        this.count = count;
    }

    public long getValue()
    {
        return value;
    }

    /** 重置累加值与完成 latch。 */
    public void reset(final CountDownLatch latch)
    {
        value = 0L;
        sequence = 0L;
        this.latch = latch;
    }

    /** 请求停止消费循环。 */
    public void halt()
    {
        running = false;
    }

    @Override
    public void run()
    {
        running = true;
        while (true)
        {
            try
            {
                long value = blockingQueue.take().longValue();
                this.value = operation.op(this.value, value);

                if (sequence++ == count)
                {
                    latch.countDown();
                }
            }
            catch (InterruptedException ex)
            {
                if (!running)
                {
                    break;
                }
            }
        }
    }
}
