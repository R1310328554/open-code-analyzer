package com.taobao.arthas.core.shell.handlers.shell;

import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.impl.ShellImpl;
import com.taobao.arthas.core.shell.system.Job;

/**
 * 前台 Job 变更回调：无前台 Job 时重新显示 readline 提示符。
 * <p>
 * Job 结束或转入后台后 {@link JobListener} 通知本 Handler；
 * job 为 null 表示当前无占用终端的前台任务，可安全恢复命令行输入。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class ShellForegroundUpdateHandler implements Handler<Job> {
    /** 所属 Shell 会话 */
    private ShellImpl shell;

    /** @param shell 监听前台 Job 变化的 Shell 实例 */
    public ShellForegroundUpdateHandler(ShellImpl shell) {
        this.shell = shell;
    }

    @Override
    /** 前台 Job 清空后重新进入 readline 等待用户输入 */
    public void handle(Job job) {
        if (job == null) {
            shell.readline();
        }
    }
}
