package com.taobao.arthas.core.shell.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shell 命令注册表：维护命令名到 {@link Command} 的可变映射。
 * <p>
 * 实现 {@link CommandResolver}，供 Shell 查找、注册与注销诊断命令。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CommandRegistry implements CommandResolver {
    /** 命令名 → 命令实例，线程安全 */
    final ConcurrentHashMap<String, Command> commandMap = new ConcurrentHashMap<String, Command>();

    /**
     * 创建空注册表。
     *
     * @return 新的 CommandRegistry
     */
    public static CommandRegistry create() {
        return new CommandRegistry();
    }

    /**
     * 从 {@link AnnotatedCommand} 类注册单个命令。
     *
     * @param command 带 CLI 注解的命令类
     * @return 当前注册表，支持链式调用
     */
    public CommandRegistry registerCommand(Class<? extends AnnotatedCommand> command) {
        return registerCommand(Command.create(command));
    }

    /**
     * 注册已构建的 {@link Command} 实例。
     *
     * @param command 待注册命令
     * @return 当前注册表，支持链式调用
     */
    public CommandRegistry registerCommand(Command command) {
        return registerCommands(Collections.singletonList(command));
    }

    /**
     * 批量注册命令列表。
     *
     * @param commands 命令集合
     * @return 当前注册表，支持链式调用
     */
    public CommandRegistry registerCommands(List<Command> commands) {
        for (Command command : commands) {
            commandMap.put(command.name(), command);
        }
        return this;
    }


    /**
     * 按名称注销命令。
     *
     * @param commandName 命令名
     * @return 当前注册表，支持链式调用
     */
    public CommandRegistry unregisterCommand(String commandName) {
        commandMap.remove(commandName);
        return this;
    }

    /**
     * @return 当前注册表中的全部命令副本列表
     */
    @Override
    public List<Command> commands() {
        return new ArrayList<Command>(commandMap.values());
    }
}
