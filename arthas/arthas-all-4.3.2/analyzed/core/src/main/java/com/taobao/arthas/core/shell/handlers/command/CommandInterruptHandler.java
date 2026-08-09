package com.taobao.arthas.core.shell.handlers.command;

import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.handlers.Handler;

/**
 * 用户中断（Ctrl+C 等）时结束当前 {@link CommandProcess} 并释放会话锁。
 * <p>
 * 注册到终端 interrupt 信号，防止长时间命令阻塞 Shell 输入。
 *
 * @author ralf0131 2017-01-09 13:23.
 */
public class CommandInterruptHandler implements Handler<Void> {

    private CommandProcess process;

    /** @param process 当前正在执行的命令进程 */
    public CommandInterruptHandler(CommandProcess process) {
        this.process = process;
    }

    @Override
    /** 结束命令并 unlock 会话，允许接收下一条输入 */
    public void handle(Void event) {
        process.end();
        process.session().unLock();
    }
}
