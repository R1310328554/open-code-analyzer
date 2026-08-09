package com.taobao.arthas.core.util;

import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.command.ExitStatus;

/**
 * 命令进程收尾工具：统一根据 {@link ExitStatus} 结束 Shell 命令执行。
 */
public class CommandUtils {

    /**
     * 检查退出状态并结束命令处理流程。
     * <p>
     * status 非 null 时使用其状态码与消息；为 null 时以 -1 和默认错误信息结束。
     *
     * @param process CommandProcess 实例
     * @param status 命令的 ExitStatus
     */
    public static void end(CommandProcess process, ExitStatus status) {
        if (status != null) {
            process.end(status.getStatusCode(), status.getMessage());
        } else {
            process.end(-1, "process error, exit status is null");
        }
    }

}
