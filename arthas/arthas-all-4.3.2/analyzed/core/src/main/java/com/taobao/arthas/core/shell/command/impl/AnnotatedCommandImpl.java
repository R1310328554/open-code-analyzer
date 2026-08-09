package com.taobao.arthas.core.shell.command.impl;

import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.Command;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.util.UserStatUtil;
import com.taobao.middleware.cli.CLI;
import com.taobao.middleware.cli.Option;
import com.taobao.middleware.cli.annotations.CLIConfigurator;

import java.util.Collections;

/**
 * 将 {@link AnnotatedCommand} 子类包装为可注册的 {@link Command}。
 * <p>
 * 启动时通过 {@link CLIConfigurator#define} 解析注解生成 CLI 描述并注入 {@code -h/--help}；
 * 执行时实例化命令类、注入 {@link CommandProcess#commandLine()} 并上报使用统计。
 *
 * @author beiwei30 on 10/11/2016.
 */
public class AnnotatedCommandImpl extends Command {

    private CLI cli;
    private Class<? extends AnnotatedCommand> clazz;
    private Handler<CommandProcess> processHandler = new ProcessHandler();

    /** 解析 clazz 上的 CLI 注解并注册 help 选项 */
    public AnnotatedCommandImpl(Class<? extends AnnotatedCommand> clazz) {
        this.clazz = clazz;
        cli = CLIConfigurator.define(clazz, true);
        cli.addOption(new Option().setArgName("help").setFlag(true).setShortName("h").setLongName("help")
                .setDescription("this help").setHelp(true));
    }

    /** 子类是否显式声明了 name() 方法（需优先于注解名） */
    private boolean shouldOverridesName(Class<? extends AnnotatedCommand> clazz) {
        try {
            clazz.getDeclaredMethod("name");
            return true;
        } catch (NoSuchMethodException ignore) {
            return false;
        }
    }

    /** 子类是否显式声明了 cli() 方法 */
    private boolean shouldOverrideCli(Class<? extends AnnotatedCommand> clazz) {
        try {
            clazz.getDeclaredMethod("cli");
            return true;
        } catch (NoSuchMethodException ignore) {
            return false;
        }
    }

    @Override
    public String name() {
        if (shouldOverridesName(clazz)) {
            try {
                return clazz.newInstance().name();
            } catch (Exception ignore) {
                // 实例化失败时回退到 cli.getName()
            }
        }
        return cli.getName();
    }

    @Override
    public CLI cli() {
        if (shouldOverrideCli(clazz)) {
            try {
                return clazz.newInstance().cli();
            } catch (Exception ignore) {
                // 实例化失败时使用构造期生成的 cli
            }
        }
        return cli;
    }

    /** 实例化命令、注入参数、执行业务并上报成功统计 */
    private void process(CommandProcess process) {
        AnnotatedCommand instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            process.end();
            return;
        }
        CLIConfigurator.inject(process.commandLine(), instance);
        instance.process(process);
        // 从 session 取 userId 用于用量上报
        String userId = process.session() != null ? process.session().getUserId() : null;
        UserStatUtil.arthasUsageSuccess(name(), process.args(), userId);
    }

    @Override
    public Handler<CommandProcess> processHandler() {
        return processHandler;
    }

    /** 实例化命令类并委派其 complete；异常时返回空候选 */
    @Override
    public void complete(final Completion completion) {
        final AnnotatedCommand instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            super.complete(completion);
            return;
        }

        try {
            instance.complete(completion);
        } catch (Throwable t) {
            completion.complete(Collections.<String>emptyList());
        }
    }

    /** 将 processHandler 桥接到 {@link #process(CommandProcess)} */
    private class ProcessHandler implements Handler<CommandProcess> {
        @Override
        public void handle(CommandProcess process) {
            process(process);
        }
    }

}
