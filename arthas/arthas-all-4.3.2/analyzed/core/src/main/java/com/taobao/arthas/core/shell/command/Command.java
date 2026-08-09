package com.taobao.arthas.core.shell.command;

import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.command.impl.AnnotatedCommandImpl;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.middleware.cli.CLI;

import java.util.Collections;
import java.util.List;

/**
 * Shell 可执行命令的抽象表示。
 * <p>
 * 可通过 {@link #create(Class)} 从 {@link AnnotatedCommand} 子类生成，
 * 或通过 {@link CommandBuilder} 编程式构建。
 */
public abstract class Command {

    /**
     * 从带 CLI 注解的 Java 类创建命令包装。
     *
     * @param clazz {@link AnnotatedCommand} 子类
     * @return 可注册到 {@link CommandRegistry} 的命令实例
     */
    public static Command create(final Class<? extends AnnotatedCommand> clazz) {
        return new AnnotatedCommandImpl(clazz);
    }

    /**
     * @return 命令名称
     */
    public String name() {
        return null;
    }

    /**
     * @return 命令行描述，可为 null
     */
    public CLI cli() {
        return null;
    }

    /**
     * 返回命令执行时的进程处理器。
     *
     * @return 处理 {@link CommandProcess} 的 {@link Handler}
     */
    public abstract Handler<CommandProcess> processHandler();

    /**
     * Tab 补全；默认返回空候选，子类或 {@link AnnotatedCommandImpl} 可覆盖。
     *
     * @param completion 补全上下文
     */
    public void complete(Completion completion) {
        completion.complete(Collections.<String>emptyList());
    }
}
