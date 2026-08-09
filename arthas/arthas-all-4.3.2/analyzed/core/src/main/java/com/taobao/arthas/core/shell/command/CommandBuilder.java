package com.taobao.arthas.core.shell.command;

import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.command.impl.CommandBuilderImpl;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.middleware.cli.CLI;

/**
 * 编程式构建 {@link Command} 的流式 API。
 * <p>
 * 适用于无法使用 {@link AnnotatedCommand} 注解的内置命令（如管道、grep 等），
 * 通过 {@link #processHandler} 与 {@link #completionHandler} 分别绑定执行与补全逻辑。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class CommandBuilder {

    /**
     * 按命令名创建 Builder；执行时由处理器自行解析 {@link CommandProcess#args()}。
     *
     * @param name 命令名称
     * @return Builder 实例
     */
    public static CommandBuilder command(String name) {
        return new CommandBuilderImpl(name, null);
    }

    /**
     * 绑定已有 {@link CLI} 描述创建 Builder。
     * <p>
     * 执行时可通过 {@link CommandProcess#commandLine()} 读取已解析的选项与参数。
     *
     * @param cli CLI 描述对象
     * @return Builder 实例
     */
    public static CommandBuilder command(CLI cli) {
        return new CommandBuilderImpl(cli.getName(), cli);
    }

    /**
     * 设置命令执行处理器，Shell 调度命令时调用。
     *
     * @param handler 进程处理器
     * @return 当前 Builder，支持链式调用
     */
    public abstract CommandBuilder processHandler(Handler<CommandProcess> handler);

    /**
     * 设置 Tab 补全处理器（用户按 Tab 键时触发）。
     *
     * @param handler 补全处理器
     * @return 当前 Builder，支持链式调用
     */
    public abstract CommandBuilder completionHandler(Handler<Completion> handler);

    /**
     * 构建不可变的 {@link Command} 实例。
     *
     * @return 已配置的命令对象
     */
    public abstract Command build();

}
