package com.taobao.arthas.core.shell.command;

import com.taobao.arthas.core.advisor.AdviceListener;
import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.term.Tty;
import com.taobao.middleware.cli.CommandLine;

import java.lang.instrument.ClassFileTransformer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命令执行进程上下文：贯穿单次命令生命周期的交互 API。
 * <p>
 * 继承 {@link Tty} 提供终端读写；支持前后台切换、中断/挂起/恢复、
 * 字节码增强监听器注册及分阶段 {@link ResultModel} 输出。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface CommandProcess extends Tty {
    /**
     * @return 未解析的参数字符串 Token 列表
     */
    List<CliToken> argsTokens();

    /**
     * @return 已展开的字符串参数列表
     */
    List<String> args();

    /**
     * @return 结构化命令行对象；无 CLI 描述时为 null
     */
    CommandLine commandLine();

    /**
     * @return 当前 Shell 会话
     */
    Session session();

    /**
     * @return 命令是否在前台运行
     */
    boolean isForeground();

    /** 注册标准输入行处理器 */
    CommandProcess stdinHandler(Handler<String> handler);

    /**
     * 设置中断处理器（如用户按 <code>Ctrl-C</code>）。
     *
     * @param handler 中断回调
     * @return 当前进程，支持链式调用
     */
    CommandProcess interruptHandler(Handler<Void> handler);

    /**
     * 设置挂起处理器（如用户按 <code>Ctrl-Z</code>）。
     *
     * @param handler 挂起回调
     * @return 当前进程，支持链式调用
     */
    CommandProcess suspendHandler(Handler<Void> handler);

    /**
     * 设置恢复处理器（如用户输入 <code>bg</code> / <code>fg</code>）。
     *
     * @param handler 恢复回调
     * @return 当前进程，支持链式调用
     */
    CommandProcess resumeHandler(Handler<Void> handler);

    /**
     * 设置结束处理器（如 Shell 关闭时命令仍在运行）。
     *
     * @param handler 结束回调
     * @return 当前进程，支持链式调用
     */
    CommandProcess endHandler(Handler<Void> handler);

    /**
     * 向标准输出写入文本。
     *
     * @param data 输出内容
     * @return 当前进程，支持链式调用
     */
    CommandProcess write(String data);

    /**
     * 设置转入后台时的回调。
     *
     * @param handler 后台回调
     * @return 当前进程，支持链式调用
     */
    CommandProcess backgroundHandler(Handler<Void> handler);

    /**
     * 设置转回前台时的回调。
     *
     * @param handler 前台回调
     * @return 当前进程，支持链式调用
     */
    CommandProcess foregroundHandler(Handler<Void> handler);

    /** 终端尺寸变化回调 */
    @Override
    CommandProcess resizehandler(Handler<Void> handler);

    /** 以退出码 0 正常结束命令 */
    void end();

    /**
     * 以指定退出码结束命令。
     *
     * @param status 退出码
     */
    void end(int status);

    /**
     * 以指定退出码与消息结束命令。
     *
     * @param status 退出码
     * @param message 结束说明
     */
    void end(int status, String message);


    /**
     * 注册 Advice 监听器与 ClassFileTransformer（增强类命令使用）。
     *
     * @param listener 方法 Advice 回调
     * @param transformer 字节码转换器
     */
    void register(AdviceListener listener, ClassFileTransformer transformer);

    /** 注销已注册的监听器与转换器 */
    void unregister();

    /**
     * @return 命令已执行次数（如 watch/trace 的触发计数）
     */
    AtomicInteger times();

    /** 恢复已挂起的命令进程 */
    void resume();

    /** 挂起当前命令进程 */
    void suspend();

    /**
     * 在终端回显提示信息（如后台任务状态）。
     *
     * @param tips 提示文本
     */
    void echoTips(String tips);

    /**
     * @return 命令结果缓存文件路径（如 tt 命令）
     */
    String cacheLocation();

    /** @return 命令是否仍在运行 */
    boolean isRunning();

    /**
     * 将阶段性结果追加到输出队列（异步渲染）。
     *
     * @param result 单条结果模型
     */
    void appendResult(ResultModel result);

}
