package com.taobao.arthas.core.shell.system.impl;

import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.cli.CompletionUtils;
import com.taobao.arthas.core.shell.command.Command;
import com.taobao.arthas.core.shell.command.CommandResolver;
import com.taobao.arthas.core.shell.command.ShellInternalCommandResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * 内部命令管理器：聚合多个 {@link CommandResolver}，负责命令查找与 Tab 补全。
 * <p>
 * 补全逻辑区分「命令名补全」与「单命令参数补全」；管道 {@code |} 之后按新段处理。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class InternalCommandManager {

    /** 按注册顺序排列的命令解析器链 */
    private final List<CommandResolver> resolvers;

    /** @param resolvers 可变参数形式的解析器列表 */
    public InternalCommandManager(CommandResolver... resolvers) {
        this.resolvers = Arrays.asList(resolvers);
    }

    /** @param resolvers 解析器列表 */
    public InternalCommandManager(List<CommandResolver> resolvers) {
        this.resolvers = resolvers;
    }

    /** @return 已注册的 CommandResolver 列表 */
    public List<CommandResolver> getResolvers() {
        return resolvers;
    }

    /**
     * 按名称查找命令，跳过 {@link ShellInternalCommandResolver}（Shell 内置命令单独处理）。
     *
     * @param commandName 命令名
     * @return 匹配的 Command，未找到返回 null
     */
    public Command getCommand(String commandName) {
        for (CommandResolver resolver : resolvers) {
            if (resolver instanceof ShellInternalCommandResolver) {
                continue;
            }
            Command command = getCommand(resolver, commandName);
            if (command != null) {
                return command;
            }
        }
        return null;
    }

    /**
     * 执行 Tab 补全：根据当前 token 判断补全命令名还是命令参数。
     *
     * @param completion 补全上下文，候选通过其 complete 方法回写
     */
    public void complete(final Completion completion) {
        List<CliToken> lineTokens = completion.lineTokens();
        int index = findLastPipe(lineTokens);
        LinkedList<CliToken> tokens = new LinkedList<CliToken>(lineTokens.subList(index + 1, lineTokens.size()));

        // 去掉行首空白 token
        while (tokens.size() > 0 && tokens.getFirst().isBlank()) {
            tokens.removeFirst();
        }

        // token 数 > 1 表示已有命令名，进入参数补全
        if (tokens.size() > 1) {
            completeSingleCommand(completion, tokens);
        } else {
            completeCommands(completion, tokens);
        }
    }

    /** 补全命令名：遍历所有 resolver 的非 hidden 命令，匹配前缀 */
    private void completeCommands(Completion completion, LinkedList<CliToken> tokens) {
        String prefix = tokens.size() > 0 ? tokens.getFirst().value() : "";
        List<String> names = new LinkedList<String>();
        for (CommandResolver resolver : resolvers) {
            for (Command command : resolver.commands()) {
                String name = command.name();
                boolean hidden = command.cli() != null && command.cli().isHidden();
                if (name.startsWith(prefix) && !names.contains(name) && !hidden) {
                    names.add(name);
                }
            }
        }
        if (names.size() == 1) {
            completion.complete(names.get(0).substring(prefix.length()), true);
        } else {
            String commonPrefix = CompletionUtils.findLongestCommonPrefix(names);
            if (commonPrefix.length() > prefix.length()) {
                completion.complete(commonPrefix.substring(prefix.length()), false);
            } else {
                completion.complete(names);
            }
        }
    }

    /** 补全单个命令的参数：定位命令后构造 {@link CommandCompletion} 委托 */
    private void completeSingleCommand(Completion completion, LinkedList<CliToken> tokens) {
        ListIterator<CliToken> it = tokens.listIterator();
        while (it.hasNext()) {
            CliToken ct = it.next();
            it.remove();
            if (ct.isText()) {
                final List<CliToken> newTokens = new ArrayList<CliToken>();
                while (it.hasNext()) {
                    newTokens.add(it.next());
                }
                StringBuilder tmp = new StringBuilder();
                for (CliToken token : newTokens) {
                    tmp.append(token.raw());
                }
                final String line = tmp.toString();
                for (CommandResolver resolver : resolvers) {
                    Command command = getCommand(resolver, ct.value());
                    if (command != null) {
                        command.complete(new CommandCompletion(completion, line, newTokens));
                        return;
                    }
                }
                completion.complete(Collections.<String>emptyList());
            }
        }
    }

    /** 在指定 resolver 中按名称查找 Command */
    private static Command getCommand(CommandResolver commandResolver, String name) {
        List<Command> commands = commandResolver.commands();
        if (commands == null || commands.isEmpty()) {
            return null;
        }

        for (Command command : commands) {
            if (name.equals(command.name())) {
                return command;
            }
        }
        return null;
    }

    /** 查找行中最后一个管道符 {@code |} 的位置，用于分段补全 */
    private static int findLastPipe(List<CliToken> lineTokens) {
        int index = -1;
        for (int i = 0; i < lineTokens.size(); i++) {
            if ("|".equals(lineTokens.get(i).value())) {
                index = i;
            }
        }
        return index;
    }
}
