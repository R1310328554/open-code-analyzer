package demo.command;

import java.util.Collections;
import java.util.List;

import com.taobao.arthas.core.shell.command.Command;
import com.taobao.arthas.core.shell.command.CommandResolver;

/**
 * 外部命令解析器，向 Arthas Shell 注册 {@link DemoExternalCommand}。
 * <p>Arthas 启动时会扫描 {@code arthas.home/commands} 下的 {@link CommandResolver} 实现并合并命令列表。</p>
 */
public class DemoExternalCommandResolver implements CommandResolver {

    @Override
    public List<Command> commands() {
        return Collections.singletonList(Command.create(DemoExternalCommand.class));
    }
}
