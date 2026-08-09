package com.taobao.arthas.core.shell.command.internal;

import io.termd.core.function.Function;

/**
 * 管道处理器关闭回调接口，继承 termd {@link Function}。
 * <p>
 * 实现类（如 {@link RedirectHandler}、{@link TeeHandler}）在 Job 结束或 Shell 关闭时
 * 通过 {@link #close()} 释放文件句柄等资源。
 *
 * @author diecui1202 on 2017/11/2.
 */
public interface CloseFunction extends Function<String, String> {

    /** 关闭底层资源（文件流、缓冲区等） */
    void close();
}
