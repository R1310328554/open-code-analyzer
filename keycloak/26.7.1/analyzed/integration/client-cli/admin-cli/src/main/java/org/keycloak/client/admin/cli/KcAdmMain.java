/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.client.admin.cli;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;

import org.keycloak.client.admin.cli.commands.KcAdmCmd;
import org.keycloak.client.admin.cli.v2.KcAdmV2Cmd;
import org.keycloak.client.admin.cli.v2.KcAdmV2Completer;
import org.keycloak.client.cli.common.BaseGlobalOptionsCmd;
import org.keycloak.client.cli.common.CommandState;
import org.keycloak.client.cli.common.Globals;
import org.keycloak.client.cli.util.OsUtil;

/**
 * Keycloak 管理 CLI（{@code kcadm.sh}/{@code kcadm.bat}）的入口类。
 * <p>
 * 根据是否携带 {@link #V2_FLAG} 分发到 v1 或 v2 命令树，并处理 shell 补全
 * 与叶子命令帮助显示逻辑。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class KcAdmMain {

    /** 默认配置文件绝对路径（用户主目录下）。 */
    public static final String DEFAULT_CONFIG_FILE_PATH = System.getProperty("user.home") + "/.keycloak/kcadm.config";

    /** 帮助文本中展示的默认配置文件路径占位符。 */
    public static final String DEFAULT_CONFIG_FILE_STRING = OsUtil.OS_ARCH.isWindows() ? "%HOMEDRIVE%%HOMEPATH%\\.keycloak\\kcadm.config" : "~/.keycloak/kcadm.config";

    /** 当前平台的 CLI 可执行文件名。 */
    public static final String CMD = OsUtil.OS_ARCH.isWindows() ? "kcadm.bat" : "kcadm.sh";

    /** 供子命令共享的全局命令状态（配置路径、令牌作用域等）。 */
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
            return true;
        };

    };

    /** 启用 v2 命令模式的命令行标志。 */
    public static final String V2_FLAG = "--v2";

    /** picocli 内部补全触发标志。 */
    private static final String COMPLETE_FLAG = "__complete";

    /** CLI 主入口：路由到 v1/v2 命令树或执行补全。 */
    public static void main(String[] args) {
        if (!containsArg(args, V2_FLAG)) {
            Globals.main(args, new KcAdmCmd(), CMD, DEFAULT_CONFIG_FILE_STRING);
            return;
        }

        String[] v2Args = stripArgs(args, V2_FLAG);

        if (containsArg(v2Args, COMPLETE_FLAG)) {
            KcAdmV2Completer.complete(stripArgs(v2Args, COMPLETE_FLAG),
                    new PrintWriter(System.out, true));
        } else {
            showHelpForLeafCommand(v2Args);
            Globals.main(v2Args, new KcAdmV2Cmd(v2Args), CMD, DEFAULT_CONFIG_FILE_STRING);
        }
    }

    /**
     * 将 {@code --help}/{@code -h} 标志移至参数末尾，使帮助显示针对叶子命令。
     * <p>
     * 例如用户输入 {@code kcadm.sh --v2 --help client list} 时，
     * 应展示 {@code list} 子命令的帮助而非根命令帮助。
     * 根命令与 {@code config} 等继承 {@link BaseGlobalOptionsCmd} 的命令需此特殊处理，
     * 与 OpenAPI 生成的 v2 子命令行为保持一致。
     * <pre>{@code
     * kcadm.sh --v2 --help client list
     * kcadm.sh --v2 client --help list
     * }</pre>
     */
    public static void showHelpForLeafCommand(String[] v2Args) {
        if (v2Args.length > 1) {
            for (int i = 0; i < v2Args.length - 1; i++) {
                if ("--help".equals(v2Args[i]) || "-h".equals(v2Args[i])) {
                    String helpFlag = v2Args[i];
                    System.arraycopy(v2Args, i + 1, v2Args, i, v2Args.length - i - 1);
                    v2Args[v2Args.length - 1] = helpFlag;
                    break;
                }
            }
        }
    }

    private static boolean containsArg(String[] args, String arg) {
        return Arrays.stream(args).anyMatch(arg::equalsIgnoreCase);
    }

    private static String[] stripArgs(String[] args, String... argsToStrip) {
        Set<String> toStrip = Set.of(argsToStrip);
        return Arrays.stream(args)
                .filter(a -> !toStrip.contains(a.toLowerCase()))
                .toArray(String[]::new);
    }
}
