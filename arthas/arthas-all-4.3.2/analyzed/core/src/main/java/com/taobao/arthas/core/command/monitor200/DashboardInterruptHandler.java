package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.handlers.command.CommandInterruptHandler;

import java.util.Timer;

/**
 * {@code dashboard} 的 Ctrl+C 中断处理器：先取消采样 Timer 再结束命令进程。
 * <p>
 * 避免 Timer 在命令结束后仍触发 {@link DashboardCommand.DashboardTimerTask} 写已关闭的 session。
 *
 * @author ralf0131 2017-01-09 13:37.
 */
public class DashboardInterruptHandler extends CommandInterruptHandler {

    /** 待取消的 dashboard 定时器引用 */
    private volatile Timer timer;

    /** @param timer 与 dashboard 命令共享的采样 Timer */
    public DashboardInterruptHandler(CommandProcess process, Timer timer) {
        super(process);
        this.timer = timer;
    }

    /** 取消定时采样后调用父类逻辑结束 CommandProcess */
    @Override
    public void handle(Void event) {
        timer.cancel();
        super.handle(event);
    }
}
