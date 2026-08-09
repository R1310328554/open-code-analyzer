package com.lmax.disruptor;

/**
 * 处理事件时遇到可回退异常（{@link RewindableException}）时的策略接口。
 */
public interface BatchRewindStrategy
{

    /**
     * 当 {@link RewindableException} 从 {@link EventHandler} 抛出时调用。
     *
     * @param e        从 {@link EventHandler} 传播出来的异常
     * @param attempts 当前批次已尝试处理的次数
     * @return 决定是回退重试该批次，还是向上抛出异常
     */
    RewindAction handleRewindException(RewindableException e, int attempts);
}
