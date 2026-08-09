package com.taobao.arthas.core.shell;

import com.taobao.arthas.core.shell.command.CommandResolver;
import com.taobao.arthas.core.shell.future.Future;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.handlers.NoOpHandler;
import com.taobao.arthas.core.shell.impl.ShellServerImpl;
import com.taobao.arthas.core.shell.system.impl.InternalCommandManager;
import com.taobao.arthas.core.shell.system.impl.JobControllerImpl;
import com.taobao.arthas.core.shell.term.Term;
import com.taobao.arthas.core.shell.term.TermServer;

/**
 * Arthas Shell 服务端抽象：聚合多个 {@link TermServer}（Telnet/HTTP 等）。
 * <p>
 * 注册 {@link CommandResolver} 与 TermServer 后调用 {@link #listen} 异步启动；
 * 每个入站连接会创建独立的 {@link Shell} 与 {@link com.taobao.arthas.core.shell.system.JobController}。
 * {@link #createShell()} 可用于单元测试构造 Shell 实例。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class ShellServer {

    /**
     * 使用指定 {@link ShellServerOptions} 创建 Shell 服务端实现。
     *
     * @param options the options
     * @return the created shell server
     */
    public static ShellServer create(ShellServerOptions options) {
        return new ShellServerImpl(options);
    }

    /** 使用默认 {@link ShellServerOptions} 创建 Shell 服务端 */

     * @return the created shell server
     */
    public static ShellServer create() {
        return new ShellServerImpl(new ShellServerOptions());
    }

    /** 注册命令解析器（内置或扩展命令），支持链式调用 */

     * @param resolver the resolver
     * @return a reference to this, so the API can be used fluently
     */
    public abstract ShellServer registerCommandResolver(CommandResolver resolver);

    /** 注册终端服务器；其生命周期由本 ShellServer 统一管理 */

     * @param termServer the term server to add
     * @return a reference to this, so the API can be used fluently
     */
    public abstract ShellServer registerTermServer(TermServer termServer);

    /**
     * Create a new shell, the returned shell should be closed explicitly.
     *
     * @param term the shell associated terminal
     * @return the created shell
     */
    public abstract Shell createShell(Term term);

    /**
     * Create a new shell, the returned shell should be closed explicitly.
     *
     * @return the created shell
     */
    public abstract Shell createShell();

    /** 异步启动所有已注册 TermServer，完成后回调 NoOpHandler */

    public ShellServer listen() {
        return listen(new NoOpHandler<Future<Void>>());
    }

    /**
     * Start the shell service, this is an asynchronous start.
     *
     * @param listenHandler handler for getting notified when service is started
     */
    public abstract ShellServer listen(Handler<Future<Void>> listenHandler);

    /** 异步关闭 Shell 服务 */

    public void close() {
        close(new NoOpHandler<Future<Void>>());
    }

    /**
     * Close the shell server, this is an asynchronous close.
     *
     * @param completionHandler handler for getting notified when service is stopped
     */
    public abstract void close(Handler<Future<Void>> completionHandler);

    /** @return 全局 JobController 单例（跨 Shell 共享） */

    public abstract JobControllerImpl getJobController();

    /** @return 内置与已注册解析器聚合的命令管理器 */

    public abstract InternalCommandManager getCommandManager();
}
