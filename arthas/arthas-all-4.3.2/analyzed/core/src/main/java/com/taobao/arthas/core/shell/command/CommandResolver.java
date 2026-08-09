package com.taobao.arthas.core.shell.command;

import java.util.List;

/**
 * 命令解析器：供 Shell 发现可用命令集合。
 * <p>
 * {@link CommandRegistry} 为可变的注册表实现；{@link ShellInternalCommandResolver}
 * 标记仅 Shell 内部使用的解析器。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface CommandResolver {
    /**
     * @return 当前解析器提供的全部命令
     */
    List<Command> commands();
}
