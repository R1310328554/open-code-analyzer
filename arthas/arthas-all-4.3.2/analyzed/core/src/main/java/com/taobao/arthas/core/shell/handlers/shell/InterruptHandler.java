package com.taobao.arthas.core.shell.handlers.shell;

import com.taobao.arthas.core.shell.impl.ShellImpl;
import com.taobao.arthas.core.shell.term.SignalHandler;

/**
 * Shell 中断信号（Ctrl+C / INTR）处理器。
 * <p>
 * 若存在前台 Job 则调用 {@link com.taobao.arthas.core.shell.system.Job#interrupt()} 尝试中断命令；
 * 无前台 Job 时返回 true 表示信号已消费，避免传播到 JVM 默认处理。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class InterruptHandler implements SignalHandler {

    /** 所属 Shell 会话，用于获取前台 Job */
    private ShellImpl shell;

    /** @param shell 注册信号的 Shell 实例 */
    public InterruptHandler(ShellImpl shell) {
        this.shell = shell;
    }

    @Override
    /**
     * 处理中断按键。
     * @param key 终端键码
     * @return 是否已消费该信号
     */
    public boolean deliver(int key) {
        if (shell.getForegroundJob() != null) {
            return shell.getForegroundJob().interrupt();
        }
        return true;
    }
}
