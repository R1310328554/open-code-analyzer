package com.lmax.disruptor.support;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.DataProvider;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.TimeoutException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 多 RingBuffer 批处理事件处理器：轮询各缓冲区的可用序号并批量派发事件。
 */
public class MultiBufferBatchEventProcessor<T>
    implements EventProcessor
{
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final DataProvider<T>[] providers;
    private final SequenceBarrier[] barriers;
    private final EventHandler<T> handler;
    private final Sequence[] sequences;
    private long count;

    public MultiBufferBatchEventProcessor(
        final DataProvider<T>[] providers,
        final SequenceBarrier[] barriers,
        final EventHandler<T> handler)
    {
        if (providers.length != barriers.length)
        {
            throw new IllegalArgumentException();
        }

        this.providers = providers;
        this.barriers = barriers;
        this.handler = handler;

        this.sequences = new Sequence[providers.length];
        for (int i = 0; i < sequences.length; i++)
        {
            sequences[i] = new Sequence(-1);
        }
    }

    @Override
    public void run()
    {
        if (!isRunning.compareAndSet(false, true))
        {
            throw new RuntimeException("Already running");
        }

        for (SequenceBarrier barrier : barriers)
        {
            barrier.clearAlert();
        }

        final int barrierLength = barriers.length;

        while (true)
        {
            try
            {
                for (int i = 0; i < barrierLength; i++)
                {
                    long available = barriers[i].waitFor(-1);
                    Sequence sequence = sequences[i];

                    long nextSequence = sequence.get() + 1;

                    // 步骤：从上次消费位置起批量派发至 available
                    for (long l = nextSequence; l <= available; l++)
                    {
                        handler.onEvent(providers[i].get(l), l, nextSequence == available);
                    }

                    sequence.set(available);

                    count += available - nextSequence + 1;
                }

                Thread.yield();
            }
            catch (AlertException e)
            {
                if (!isRunning())
                {
                    break;
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (TimeoutException e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                break;
            }
        }
    }

    @Override
    public Sequence getSequence()
    {
        throw new UnsupportedOperationException();
    }

    /** 返回已处理的事件总数。 */
    public long getCount()
    {
        return count;
    }

    /** 返回各缓冲区对应的消费序号。 */
    public Sequence[] getSequences()
    {
        return sequences;
    }

    @Override
    public void halt()
    {
        isRunning.set(false);
        barriers[0].alert();
    }

    @Override
    public boolean isRunning()
    {
        return isRunning.get();
    }
}
