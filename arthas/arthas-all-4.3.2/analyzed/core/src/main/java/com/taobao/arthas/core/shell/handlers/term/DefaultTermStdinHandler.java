package com.taobao.arthas.core.shell.handlers.term;

import com.taobao.arthas.core.shell.term.impl.TermImpl;
import io.termd.core.function.Consumer;

/**
 * 默认终端标准输入 Handler：回显按键并将 Unicode 码点送入 readline 队列。
 * <p>
 * termd 底层以 {@code int[]} 码点数组传递键盘输入；
 * 本 Handler 先 {@link TermImpl#echo} 回显，再 {@link com.taobao.arthas.core.shell.term.impl.helper.Readline#queueEvent} 供行编辑器处理。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class DefaultTermStdinHandler implements Consumer<int[]> {
    /** 关联的 Term 实现，提供 echo 与 readline */
    private TermImpl term;

    /** @param term 接收 stdin 的 Term 实例 */
    public DefaultTermStdinHandler(TermImpl term) {
        this.term = term;
    }

    @Override
    /** 回显用户按键并排队到 readline 事件循环 */
    public void accept(int[] codePoints) {
        // 本地回显，便于 Telnet 客户端看到输入
        term.echo(codePoints);
        term.getReadline().queueEvent(codePoints);
    }
}
