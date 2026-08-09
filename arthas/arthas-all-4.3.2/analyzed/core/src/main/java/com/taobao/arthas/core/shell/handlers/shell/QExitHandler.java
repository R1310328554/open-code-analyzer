package com.taobao.arthas.core.shell.handlers.shell;

import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.handlers.Handler;

/**
 * 交互式命令的「q 退出」Handler：用户输入 q 时结束当前 {@link CommandProcess}。
 * <p>
 * 用于 watch/trace 等持续输出命令，允许用户在提示符下输入 q 优雅退出，
 * 而不必 Ctrl+C 强制中断。
 *
 * @author hengyunabc 2019-02-09
 *
 */
public class QExitHandler implements Handler<String> {
    /** 当前正在执行的命令进程 */
    private CommandProcess process;

    /** @param process 可响应 q 退出的命令进程 */
    public QExitHandler(CommandProcess process) {
        this.process = process;
    }

    @Override
    /** 输入为 q（不区分大小写）时调用 {@link CommandProcess#end()} */
    public void handle(String event) {
        if ("q".equalsIgnoreCase(event)) {
            process.end();
        }
    }
}
