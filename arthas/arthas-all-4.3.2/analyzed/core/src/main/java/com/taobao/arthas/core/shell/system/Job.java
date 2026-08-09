package com.taobao.arthas.core.shell.system;

import java.util.Date;

import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.term.Tty;

/**
 * 在 {@link JobController} 中执行的任务，可包含一个或多个 {@link Process} 管道阶段。
 * <p>
 * 生命周期由 {@link #run}、{@link #resume}、{@link #suspend}、{@link #interrupt} 控制，
 * 支持前台/后台切换及超时设置。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface Job {

    /** @return 任务在控制器内的数字 id */
    int id();

    /** @return 当前 {@link ExecStatus} 执行状态 */
    ExecStatus status();

    /** @return 启动该 Job 的完整 Shell 命令行 */
    String line();


    /** 以前台方式运行 Job（须先绑定 {@link Tty}） */
    Job run();

    /** 运行 Job；foreground 为 false 时在后台执行 */
    Job run(boolean foreground);

    /** 尝试中断 Job；真正中断时返回 true */
    boolean interrupt();

    /** 恢复 Job 并切换到前台 */
    Job resume();

    /** @return 是否在后台运行 */
    boolean isRunInBackground();

    /** 将 Job 送入后台（Ctrl+Z 语义） */
    Job toBackground();

    /** 将 Job 切换到前台 */
    Job toForeground();

    /** 恢复已挂起的 Job；foreground 控制是否占前台 */
    Job resume(boolean foreground);

    /** 恢复 Job（默认前台） */
    Job suspend();

    /** 终止 Job 并释放资源 */
    void terminate();

    /** @return Job 管道中的首个 Process */
    Process process();

    /** @return Job 超时时间点，未设置时为 null */
    Date timeoutDate();

    /** 设置 Job 超时时间 */
    void setTimeoutDate(Date date);

    /** @return 所属 {@link Session} */
    Session getSession();
}
