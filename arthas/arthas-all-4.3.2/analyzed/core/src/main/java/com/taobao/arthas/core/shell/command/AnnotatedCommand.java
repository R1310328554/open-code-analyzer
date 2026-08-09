package com.taobao.arthas.core.shell.command;

import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.cli.CompletionUtils;
import com.taobao.middleware.cli.CLI;

import java.util.List;

/**
 * 基于 Java 注解声明 CLI 的命令基类，具体诊断命令应继承此类。
 * <p>
 * 子类实现 {@link #process(CommandProcess)} 完成业务逻辑；默认补全通过
 * {@link CompletionUtils} 解析类上的 CLI 注解生成选项提示。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class AnnotatedCommand {

    /**
     * @return 命令名称；默认 null，由 {@link com.taobao.middleware.cli.annotations.CLIConfigurator} 从注解推断
     */
    public String name() {
        return null;
    }

    /**
     * @return 命令行描述对象，可为 null 表示使用注解自动生成的 {@link CLI}
     */
    public CLI cli() {
        return null;
    }

    /**
     * 执行命令逻辑；处理完毕后应调用 {@link CommandProcess#end()} 结束进程。
     *
     * @param process 命令进程上下文，提供参数、会话与输出通道
     */
    public abstract void process(CommandProcess process);

    /**
     * Tab 补全入口；完成后须调用 {@link Completion#complete(List)}
     * 或 {@link Completion#complete(String, boolean)} 通知 Shell。
     *
     * @param completion 补全上下文
     */
    public void complete(Completion completion) {
        CompletionUtils.complete(completion, this.getClass());
    }

}
