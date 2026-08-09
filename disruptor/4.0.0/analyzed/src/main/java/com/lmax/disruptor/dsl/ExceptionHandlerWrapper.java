package com.lmax.disruptor.dsl;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.ExceptionHandlers;

/**
 * 可切换委托的可变异常处理器包装器。
 *
 * @param <T> 底层 {@link com.lmax.disruptor.RingBuffer} 的数据类型
 */
public class ExceptionHandlerWrapper<T> implements ExceptionHandler<T>
{
    private ExceptionHandler<? super T> delegate;

    /**
     * 切换到不同的异常处理器。
     *
     * @param exceptionHandler 此后使用的异常处理器
     */
    public void switchTo(final ExceptionHandler<? super T> exceptionHandler)
    {
        this.delegate = exceptionHandler;
    }

    @Override
    public void handleEventException(final Throwable ex, final long sequence, final T event)
    {
        getExceptionHandler().handleEventException(ex, sequence, event);
    }

    @Override
    public void handleOnStartException(final Throwable ex)
    {
        getExceptionHandler().handleOnStartException(ex);
    }

    @Override
    public void handleOnShutdownException(final Throwable ex)
    {
        getExceptionHandler() .handleOnShutdownException(ex);
    }

    private ExceptionHandler<? super T> getExceptionHandler()
    {
        ExceptionHandler<? super T> handler = delegate;
        return handler == null ? ExceptionHandlers.defaultHandler() : handler;
    }
}
