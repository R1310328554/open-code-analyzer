package com.lmax.disruptor;

/**
 * 始终选择回卷重放的批次回卷策略。
 */
public class SimpleBatchRewindStrategy implements BatchRewindStrategy
{
    @Override
    public RewindAction handleRewindException(final RewindableException e, final int retriesAttempted)
    {
        return RewindAction.REWIND;
    }
}
