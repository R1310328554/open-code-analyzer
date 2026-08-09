package com.taobao.arthas.core.shell.command.impl;

import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.command.Command;
import com.taobao.arthas.core.shell.command.CommandBuilder;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.middleware.cli.CLI;

import java.util.Collections;

/**
 * {@link CommandBuilder} 的默认实现。
 * <p>
 * 内部类 {@link CommandImpl} 持有名称、CLI 描述及执行/补全处理器，
 * 补全异常时回退为空候选列表。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CommandBuilderImpl extends CommandBuilder {

    private final String name;
    private final CLI cli;
    private Handler<CommandProcess> processHandler;
    private Handler<Completion> completeHandler;

    /** @param name 命令名；@param cli 可选 CLI 描述，null 表示无结构化选项 */
    public CommandBuilderImpl(String name, CLI cli) {
        this.name = name;
        this.cli = cli;
    }

    @Override
    public CommandBuilderImpl processHandler(Handler<CommandProcess> handler) {
        processHandler = handler;
        return this;
    }

    @Override
    public CommandBuilderImpl completionHandler(Handler<Completion> handler) {
        completeHandler = handler;
        return this;
    }

    @Override
    public Command build() {
        return new CommandImpl();
    }

    /** 由 Builder 配置参数 materialize 出的 Command 实例 */
    private class CommandImpl extends Command {
        @Override
        public String name() {
            return name;
        }

        @Override
        public CLI cli() {
            return cli;
        }

        @Override
        public Handler<CommandProcess> processHandler() {
            return processHandler;
        }

        /** 委派 completeHandler；未设置或异常时返回空列表 */
        @Override
        public void complete(final Completion completion) {
            if (completeHandler != null) {
                try {
                    completeHandler.handle(completion);
                } catch (Throwable t) {
                    completion.complete(Collections.<String>emptyList());
                }
            } else {
                super.complete(completion);
            }
        }
    }
}
