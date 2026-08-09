package com.taobao.arthas.core.shell.handlers.shell;

import com.taobao.arthas.core.shell.impl.ShellImpl;
import com.taobao.arthas.core.shell.system.ExecStatus;
import com.taobao.arthas.core.shell.system.Job;
import com.taobao.arthas.core.shell.term.SignalHandler;
import com.taobao.arthas.core.shell.term.Term;

/**
 * Shell 挂起信号（Ctrl+Z / SUSP）处理器。
 * <p>
 * 将当前前台 Job 置为 {@link ExecStatus#STOPPED} 并输出状态行，
 * 使用户可在暂停命令后继续输入其他 shell 内置命令（fg/bg/jobs 等）。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class SuspendHandler implements SignalHandler {

    /** 所属 Shell 会话 */
    private ShellImpl shell;

    /** @param shell 注册挂起信号的 Shell 实例 */
    public SuspendHandler(ShellImpl shell) {
        this.shell = shell;
    }

    @Override
    /**
     * 处理挂起按键，暂停前台 Job。
     * @param key 终端键码
     * @return 始终 true，表示信号已消费
     */
    public boolean deliver(int key) {
        Term term = shell.term();

        Job job = shell.getForegroundJob();
        if (job != null) {
            term.echo(shell.statusLine(job, ExecStatus.STOPPED));
            job.suspend();
        }

        return true;
    }
}
