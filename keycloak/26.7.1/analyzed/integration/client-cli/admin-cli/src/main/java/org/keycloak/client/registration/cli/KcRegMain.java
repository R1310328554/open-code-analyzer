package org.keycloak.client.registration.cli;

import org.keycloak.client.cli.common.CommandState;
import org.keycloak.client.cli.common.Globals;
import org.keycloak.client.cli.util.OsUtil;
import org.keycloak.client.registration.cli.commands.KcRegCmd;

/**
 * Keycloak 客户端注册 CLI（{@code kcreg}）入口类。
 * <p>
 * 定义默认配置文件路径、启动脚本名及 {@link CommandState}，并委托 {@link Globals#main} 启动 Picocli 命令树。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class KcRegMain {

    /** 默认配置文件绝对路径（{@code ~/.keycloak/kcreg.config}）。 */
    public static final String DEFAULT_CONFIG_FILE_PATH = System.getProperty("user.home") + "/.keycloak/kcreg.config";

    /** 帮助文本中显示的默认配置文件路径占位符。 */
    public static final String DEFAULT_CONFIG_FILE_STRING = OsUtil.OS_ARCH.isWindows() ? "%HOMEDRIVE%%HOMEPATH%\\.keycloak\\kcreg.config" : "~/.keycloak/kcreg.config";

    /** 当前平台的启动脚本名（{@code kcreg.sh} 或 {@code kcreg.bat}）。 */
    public static final String CMD = OsUtil.OS_ARCH.isWindows() ? "kcreg.bat" : "kcreg.sh";

    /** 注册 CLI 专用的 {@link CommandState} 实现。 */
    public static final CommandState COMMAND_STATE = new CommandState() {

        @Override
        public String getCommand() {
            return CMD;
        }

        @Override
        public String getDefaultConfigFilePath() {
            return DEFAULT_CONFIG_FILE_PATH;
        }

        @Override
        public boolean isTokenGlobal() {
            return false;
        };

    };

    /** 程序入口：解析参数并执行 {@link KcRegCmd}。 */
    public static void main(String [] args) {
        Globals.main(args, new KcRegCmd(), CMD, DEFAULT_CONFIG_FILE_STRING);
    }
}
