package org.keycloak.client.admin.cli.v2;

import org.keycloak.client.admin.cli.KcAdmMain;
import org.keycloak.client.cli.common.BaseAuthOptionsCmd;
import org.keycloak.client.cli.config.FileConfigHandler;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;


/**
 * {@code config editor} 子命令：配置 {@code edit} 命令使用的文本编辑器。
 * <p>
 * 写入配置文件中的 {@code editor} 字段，优先级高于环境变量。
 */
@Command(name = "editor", description = "Configure the text editor for the edit command")
class KcAdmV2ConfigEditorCmd implements Runnable {

    /** 编辑器命令（如 vi、nano、{@code code --wait}）。 */
    @Parameters(index = "0", paramLabel = "<editor>",
            description = "Editor command (e.g., vi, nano, 'code --wait')")
    String editorValue;

    @Option(names = "--config", description = "Path to the config file (${sys:" + BaseAuthOptionsCmd.DEFAULT_CONFIG_PATH_STRING_KEY + "} by default)")
    String config;

    @Option(names = {"-h", "--help"}, usageHelp = true, hidden = true)
    boolean help;

    @Spec
    CommandSpec spec;

    @Override
    public void run() {
        FileConfigHandler.setConfigFile(resolveConfigPath());
        try {
            new FileConfigHandler().saveMergeConfig(c -> c.setEditor(editorValue));
        } finally {
            // TODO: this must be dropped when we move away from the static config file pattern
            FileConfigHandler.setConfigFile(null);
        }

        spec.commandLine().getErr().println("Editor configured: " + editorValue);
    }

    /** 解析配置文件路径：命令行 {@code --config} 或根选项。 */
    private String resolveConfigPath() {
        if (config != null) {
            return config;
        }
        String rootConfig = spec.commandLine().getParseResult().commandSpec()
                .root().findOption(KcAdmV2Cmd.CONFIG_OPTION).getValue();
        return rootConfig != null ? rootConfig : KcAdmMain.DEFAULT_CONFIG_FILE_PATH;
    }

}
