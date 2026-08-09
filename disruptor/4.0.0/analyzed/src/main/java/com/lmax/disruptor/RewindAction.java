package com.lmax.disruptor;

/**
 * {@link BatchRewindStrategy} 的返回结果，决定是回卷重放还是向上抛出异常。
 */
public enum RewindAction
{
    /**
     * 回卷并从批次开头重新播放
     */
    REWIND,

    /**
     * 重新抛出异常，交由已配置的 {@link ExceptionHandler} 处理
     */
    THROW
}
