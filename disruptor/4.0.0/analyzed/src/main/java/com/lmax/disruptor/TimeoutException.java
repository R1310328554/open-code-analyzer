package com.lmax.disruptor;

/**
 * 等待策略可在指定时间窗口内未检测到消息时抛出本异常以通知调用方。
 * 为节省开销，提供单例实例。
 */
@SuppressWarnings({"serial", "lgtm[java/non-sync-override]"})
public final class TimeoutException extends Exception
{
    /**
     * 单例实例，避免重复分配
     */
    public static final TimeoutException INSTANCE = new TimeoutException();

    private TimeoutException()
    {
        // Singleton
    }

    @Override
    public Throwable fillInStackTrace()
    {
        return this;
    }
}
