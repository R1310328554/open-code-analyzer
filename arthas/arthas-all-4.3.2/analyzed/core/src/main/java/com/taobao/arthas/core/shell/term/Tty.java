package com.taobao.arthas.core.shell.term;

import com.taobao.arthas.core.shell.handlers.Handler;

/**
 * Shell TTY 底层交互接口：尺寸、标准输入/输出与窗口 resize 事件。
 * <p>
 * {@link Term} 在其上扩展 readline、信号处理与会话绑定等高级能力。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface Tty {

    /**
     * @return 终端类型声明，如 {@literal vt100}、{@literal xterm-256}；
     *         未声明时为 null
     */
    String type();

    /** @return 终端宽度（列数），未知时为 {@literal -1} */
    int width();

    /** @return 终端高度（行数），未知时为 {@literal -1} */
    int height();

    /**
     * 注册标准输入流处理器；仅前台 Job 会激活 stdin 回调。
     *
     * @param handler 读取用户按键/输入的 handler
     * @return this
     */
    Tty stdinHandler(Handler<String> handler);

    /**
     * 向标准输出写入文本（通常经 termd 编码后发送到客户端）。
     *
     * @param data 待写入数据
     * @return this
     */
    Tty write(String data);

    /**
     * 注册终端尺寸变化回调（SIGWINCH 语义）。
     *
     * @param handler resize 事件 handler
     * @return this
     */
    Tty resizehandler(Handler<Void> handler);

}
