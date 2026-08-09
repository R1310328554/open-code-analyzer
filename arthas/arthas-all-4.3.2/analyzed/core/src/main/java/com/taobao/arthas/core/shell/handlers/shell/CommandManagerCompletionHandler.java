package com.taobao.arthas.core.shell.handlers.shell;

import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.system.impl.InternalCommandManager;

/**
 * Shell 命令行补全 Handler：将 Tab 补全请求委托给 {@link InternalCommandManager}。
 * <p>
 * readline 在输入过程中触发 {@link Completion} 事件，
 * 由命令管理器根据已注册命令与选项生成候选列表。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class CommandManagerCompletionHandler implements Handler<Completion> {
    /** 提供命令注册与补全逻辑的命令管理器 */
    private InternalCommandManager commandManager;

    /** @param commandManager 内置命令管理器实例 */
    public CommandManagerCompletionHandler(InternalCommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    /** 根据当前输入上下文生成补全候选 */
    public void handle(Completion completion) {
        commandManager.complete(completion);
    }
}
