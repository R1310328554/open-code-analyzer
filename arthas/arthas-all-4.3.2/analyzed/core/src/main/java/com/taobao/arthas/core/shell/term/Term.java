package com.taobao.arthas.core.shell.term;

import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.session.Session;
import io.termd.core.function.Function;

/**
 * Arthas Shell 终端抽象：在 {@link Tty} 之上提供 readline、补全、信号与会话管理。
 * <p>
 * 每个 Telnet/WebSocket 连接对应一个 {@link Term} 实例，由 {@link TermServer} 创建并交给 Shell 处理。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface Term extends Tty {

    @Override
    Term resizehandler(Handler<Void> handler);

    @Override
    Term stdinHandler(Handler<String> handler);

    /** 注册 stdout 输出链上的变换函数（如管道、重定向 handler） */
    Term stdoutHandler(Function<String, String> handler);

    @Override
    Term write(String data);

    /** @return 终端最后一次收到用户输入的时间戳（毫秒） */
    long lastAccessedTime();

    /**
     * 向终端回显文本，必要时做转义处理。
     *
     * @param text 回显内容
     * @return this
     */
    Term echo(String text);

    /**
     * 将终端与 Shell {@link Session} 关联（认证状态、变量等）。
     *
     * @param session 会话对象
     * @return this
     */
    Term setSession(Session session);

    /**
     * 注册中断信号处理器（通常映射 Ctrl+C）。
     *
     * @param handler 中断 handler
     * @return this
     */
    Term interruptHandler(SignalHandler handler);

    /**
     * 注册挂起信号处理器（通常映射 Ctrl+Z）。
     *
     * @param handler 挂起 handler
     * @return this
     */
    Term suspendHandler(SignalHandler handler);

    /**
     * 启动 readline 读取一行用户输入。
     *
     * @param prompt 提示符字符串
     * @param lineHandler 用户提交整行后的回调
     */
    void readline(String prompt, Handler<String> lineHandler);

    /**
     * 带 Tab 补全的 readline。
     *
     * @param prompt 提示符
     * @param lineHandler 行提交回调
     * @param completionHandler Tab 补全回调
     */
    void readline(String prompt, Handler<String> lineHandler, Handler<Completion> completionHandler);

    /**
     * 注册终端关闭回调（连接断开或主动 close）。
     *
     * @param handler 关闭事件 handler
     * @return this
     */
    Term closeHandler(Handler<Void> handler);

    /** 关闭终端连接并释放 readline 等资源 */
    void close();
}
