package com.lmax.disruptor;

import java.util.concurrent.locks.LockSupport;

/**
 * <p>处理 {@link RewindableException} 的回退策略：在重试前暂停指定纳秒数。</p>
 */
public class NanosecondPauseBatchRewindStrategy implements BatchRewindStrategy
{

    private final long nanoSecondPauseTime;

    /**
     * <p>处理 {@link RewindableException} 的回退策略：在重试前暂停指定纳秒数。</p>
     *
     * @param nanoSecondPauseTime 抛出可回退异常时的暂停时长（纳秒）
     */
    public NanosecondPauseBatchRewindStrategy(final long nanoSecondPauseTime)
    {
        this.nanoSecondPauseTime = nanoSecondPauseTime;
    }

    @Override
    public RewindAction handleRewindException(final RewindableException e, final int retriesAttempted)
    {
        LockSupport.parkNanos(nanoSecondPauseTime);
        return RewindAction.REWIND;
    }
}
