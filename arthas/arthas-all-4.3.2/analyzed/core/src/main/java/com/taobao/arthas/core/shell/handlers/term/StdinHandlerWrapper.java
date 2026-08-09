package com.taobao.arthas.core.shell.handlers.term;

import com.taobao.arthas.core.shell.handlers.Handler;
import io.termd.core.function.Consumer;
import io.termd.core.util.Helper;

/**
 * termd 标准输入码点数组到 Java 字符串的适配器。
 * <p>
 * 将 termd {@link Consumer} 接收的 Unicode 码点序列经 {@link Helper#fromCodePoints} 转为
 * {@link String} 后交给 Arthas {@link Handler}，供 Shell 行编辑与命令输入使用。
 *
 * @author beiwei30 on 22/11/2016.
 */
public class StdinHandlerWrapper implements Consumer<int[]> {
    /** 标准输入字符到达时的 Arthas 回调 */
    private final Handler<String> handler;

    /** @param handler 接收解码后输入字符串的处理器 */
    public StdinHandlerWrapper(Handler<String> handler) {
        this.handler = handler;
    }

    @Override
    /** 将 Unicode 码点数组解码为字符串并转发给 handler */
    public void accept(int[] codePoints) {
        handler.handle(Helper.fromCodePoints(codePoints));
    }
}
