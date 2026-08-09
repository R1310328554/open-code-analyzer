package com.taobao.arthas.core.shell.system;

import java.util.Date;

import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.term.Tty;

/**
 * Shell 管理的单个命令进程抽象，对应一条 Arthas 诊断命令的执行单元。
 * <p>
 * 生命周期涵盖 READY → RUNNING ↔ STOPPED → TERMINATED，支持前台/后台切换、
 * 中断、挂起与恢复，并通过 {@link Tty} 与终端交互。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface Process {
    /** @return 当前进程执行状态 {@link ExecStatus} */
    ExecStatus status();

    /**
     * @return 进程退出码；仅当 status 为 {@link ExecStatus#TERMINATED} 时有值，否则为 {@code null}
     */
    Integer exitCode();

    /**
     * 绑定进程使用的 TTY 终端。
     *
     * @param tty 进程 tty
     * @return this，支持链式调用
     */
    Process setTty(Tty tty);

    /** @return 进程关联的 tty，未绑定时可能为 null */
    Tty getTty();

    /**
     * 绑定 Shell 会话上下文。
     *
     * @param session 进程 session
     * @return this
     */
    Process setSession(Session session);

    /** @return 进程所属 {@link Session} */
    Session getSession();

    /**
     * 注册进程终止回调。
     *
     * @param handler 终止时调用，参数为 exitCode
     * @return this
     */
    Process terminatedHandler(Handler<Integer> handler);

    /** 以默认前台方式启动进程 */
    void run();

    /**
     * 启动进程。
     *
     * @param foreground true 表示前台运行并占用 stdin
     */
    void run(boolean foreground);

    /**
     * 尝试向进程发送中断信号（Ctrl+C 语义）。
     *
     * @return 进程是否成功处理中断
     */
    boolean interrupt();

    /**
     * 中断进程并在完成后回调。
     *
     * @param completionHandler 中断处理完成后的回调
     * @return 进程是否成功处理中断
     */
    boolean interrupt(Handler<Void> completionHandler);

    /** 恢复已挂起的进程（默认前台） */
    void resume();

    /**
     * 恢复进程并指定是否切到前台。
     *
     * @param foreground 是否占用前台 TTY
     */
    void resume(boolean foreground);

    /**
     * 恢复进程并在完成后回调。
     *
     * @param completionHandler resume 完成回调
     */
    void resume(Handler<Void> completionHandler);

    /**
     * 恢复进程，可指定前台/后台及完成回调。
     *
     * @param foreground 是否前台
     * @param completionHandler resume 完成回调
     */
    void resume(boolean foreground, Handler<Void> completionHandler);

    /** 挂起正在运行的进程（Ctrl+Z 语义） */
    void suspend();

    /**
     * 挂起进程并在完成后回调。
     *
     * @param completionHandler suspend 完成回调
     */
    void suspend(Handler<Void> completionHandler);

    /** 强制终止进程 */
    void terminate();

    /**
     * 终止进程并在完成后回调。
     *
     * @param completionHandler terminate 完成回调
     */
    void terminate(Handler<Void> completionHandler);

    /** 将运行中的进程切换到后台 */
    void toBackground();

    /**
     * 切到后台并在完成后回调。
     *
     * @param completionHandler 切换完成回调
     */
    void toBackground(Handler<Void> completionHandler);

    /** 将后台进程切换到前台 */
    void toForeground();

    /**
     * 切到前台并在完成后回调。
     *
     * @param completionHandler 切换完成回调
     */
    void toForeground(Handler<Void> completionHandler);

    /** @return 命令已执行次数（如 watch/trace 的采样轮次） */
    int times();

    /** @return 进程启动时间 */
    Date startTime();

    /** @return 重定向输出缓存文件路径，无重定向时为 null */
    String cacheLocation();

    /**
     * 设置所属 Job 的 id，用于结果分发与日志关联。
     *
     * @param jobId job id
     */
    void setJobId(int jobId);
}
