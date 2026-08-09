package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code trace -p} 路径追踪监听器：继承 {@link AbstractTraceAdviceListener}，
 * 在标准 trace 树基础上按 {@link TraceCommand} 的 path 模式过滤输出调用链。
 * 本身无额外逻辑，行为由父类与 {@link TraceCommand#getPathMatcher()} 决定。
 *
 * @author ralf0131 2017-01-05 13:59.
 */
public class PathTraceAdviceListener extends AbstractTraceAdviceListener {

    /** 绑定 trace 命令与输出进程，委托父类构建 TraceTree */
    public PathTraceAdviceListener(TraceCommand command, CommandProcess process) {
        super(command, process);
    }
}
