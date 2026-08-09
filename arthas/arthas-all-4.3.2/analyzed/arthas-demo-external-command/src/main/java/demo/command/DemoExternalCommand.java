package demo.command;

import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Argument;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Summary;

/**
 * 演示从 {@code arthas.home/commands} 目录动态加载的外部命令。
 * <p>用于验证 Arthas 插件式命令扩展机制：将 JAR 放入 commands 目录即可注册新命令。</p>
 */
@Name("demo-external")
@Summary("Demo external command loaded from arthas.home/commands")
@Description("Examples:\n"
        + "  demo-external\n"
        + "  demo-external Codex\n")
public class DemoExternalCommand extends AnnotatedCommand {

    private String message;

    /**
     * 设置要输出的消息；未指定时默认输出 hello。
     */
    @Argument(index = 0, argName = "message", required = false)
    @Description("message printed by the demo external command")
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void process(CommandProcess process) {
        String value = message;
        if (value == null || value.trim().isEmpty()) {
            value = "hello";
        }
        // 向终端写入加载成功提示，并结束命令
        process.write("demo external command loaded: " + value + "\n");
        process.end();
    }
}
