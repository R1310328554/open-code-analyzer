package com.taobao.arthas.core.shell.impl;

import com.taobao.arthas.core.shell.command.Command;
import com.taobao.arthas.core.shell.command.CommandBuilder;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.command.CommandResolver;
import com.taobao.arthas.core.shell.command.ShellInternalCommandResolver;
import com.taobao.arthas.core.shell.command.internal.GrepHandler;
import com.taobao.arthas.core.shell.command.internal.PlainTextHandler;
import com.taobao.arthas.core.shell.command.internal.WordCountHandler;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.handlers.NoOpHandler;

import java.util.Arrays;
import java.util.List;

/**
 * 内置 Shell 命令解析器，注册 exit/quit/jobs 及管道子命令等。
 * <p>
 * 实现 {@link ShellInternalCommandResolver}，在 {@link ShellServerImpl} 构造时加入
 * {@link InternalCommandManager}，使 help 能列出这些命令；实际处理由 Shell 层接管。
 *
 * @author beiwei30 on 23/11/2016.
 */
class BuiltinCommandResolver implements ShellInternalCommandResolver {

    /** 内置命令的统一占位处理器，实际逻辑在 Shell 中分发 */
    private Handler<CommandProcess> handler;

    public BuiltinCommandResolver() {
        // 默认空操作处理器，避免未绑定时的 NPE
        this.handler = new NoOpHandler<CommandProcess>();
    }

    @Override
    /** 返回所有内置命令定义（exit、quit、jobs、fg、bg、kill 及管道命令） */
    public List<Command> commands() {
        return Arrays.asList(CommandBuilder.command("exit").processHandler(handler).build(),
                             CommandBuilder.command("quit").processHandler(handler).build(),
                             CommandBuilder.command("jobs").processHandler(handler).build(),
                             CommandBuilder.command("fg").processHandler(handler).build(),
                             CommandBuilder.command("bg").processHandler(handler).build(),
                             CommandBuilder.command("kill").processHandler(handler).build(),
                             CommandBuilder.command(PlainTextHandler.NAME).processHandler(handler).build(),
                             CommandBuilder.command(GrepHandler.NAME).processHandler(handler).build(),
                             CommandBuilder.command(WordCountHandler.NAME).processHandler(handler).build());
    }
}
