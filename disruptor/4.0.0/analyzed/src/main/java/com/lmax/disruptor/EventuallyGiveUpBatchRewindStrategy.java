package com.lmax.disruptor;

/**
 * <p>处理 {@link RewindableException} 的回退策略：在达到指定重试次数后，
 * 将异常委托给 {@link ExceptionHandler}。</p>
 */
public class EventuallyGiveUpBatchRewindStrategy implements BatchRewindStrategy
{
    private final long maxAttempts;

    /**
     * @param maxAttempts 在将异常委托给处理器之前，允许抛出的可回退异常次数
     */
    public EventuallyGiveUpBatchRewindStrategy(final long maxAttempts)
    {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public RewindAction handleRewindException(final RewindableException e, final int retriesAttempted)
    {
        if (retriesAttempted == maxAttempts)
        {
            return RewindAction.THROW;
        }
        return RewindAction.REWIND;
    }
}
