package com.lmax.disruptor;

/**
 * 使用 {@link BatchEventProcessor} 时可抛出的特殊异常。
 * 抛出后 {@link BatchEventProcessor} 根据 {@link BatchRewindStrategy} 决定回卷重放或向上抛出。
 */
public class RewindableException extends Throwable
{
    /**
     * @param cause 异常的底层原因
     */
    public RewindableException(final Throwable cause)
    {
        super("REWINDING BATCH", cause);
    }
}
