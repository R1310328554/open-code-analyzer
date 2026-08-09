package com.lmax.disruptor;

import java.util.concurrent.TimeUnit;

import static com.lmax.disruptor.util.Util.awaitNanos;

/**
 * 使用锁与条件变量让 {@link EventProcessor} 在屏障上阻塞等待的等待策略。
 * 若在指定空闲时间后仍未等到事件，则抛出 {@link TimeoutException}。
 * 事件处理器可覆盖 {@link EventHandler#onTimeout(long)}，{@link BatchEventProcessor} 会在超时时调用它。
 *
 * <p>当吞吐量与低延迟不如 CPU 资源重要时可选用本策略。
 */
public class TimeoutBlockingWaitStrategy implements WaitStrategy
{
    private final Object mutex = new Object();
    private final long timeoutInNanos;

    /**
     * @param timeout 唤醒前的最长等待时间
     * @param units 超时时间单位
     */
    public TimeoutBlockingWaitStrategy(final long timeout, final TimeUnit units)
    {
        timeoutInNanos = units.toNanos(timeout);
    }

    @Override
    public long waitFor(
        final long sequence,
        final Sequence cursorSequence,
        final Sequence dependentSequence,
        final SequenceBarrier barrier)
        throws AlertException, InterruptedException, TimeoutException
    {
        long timeoutNanos = timeoutInNanos;

        long availableSequence;
        // 步骤 1：若主游标尚未到达目标序号，在互斥锁上带超时地等待
        if (cursorSequence.get() < sequence)
        {
            synchronized (mutex)
            {
                while (cursorSequence.get() < sequence)
                {
                    barrier.checkAlert();
                    timeoutNanos = awaitNanos(mutex, timeoutNanos);
                    if (timeoutNanos <= 0)
                    {
                        throw TimeoutException.INSTANCE;
                    }
                }
            }
        }

        // 步骤 2：主游标已推进后，等待依赖序号追上目标
        while ((availableSequence = dependentSequence.get()) < sequence)
        {
            barrier.checkAlert();
        }

        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking()
    {
        synchronized (mutex)
        {
            mutex.notifyAll();
        }
    }

    @Override
    public String toString()
    {
        return "TimeoutBlockingWaitStrategy{" +
            "mutex=" + mutex +
            ", timeoutInNanos=" + timeoutInNanos +
            '}';
    }
}
