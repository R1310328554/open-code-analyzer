package org.keycloak.client.admin.cli.v2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;

import org.keycloak.client.admin.cli.KcAdmMain;
import org.keycloak.client.admin.cli.commands.AbstractTargetAuthOptionsCmd;
import org.keycloak.client.admin.cli.commands.ConfigCmd;
import org.keycloak.client.cli.config.ConfigData;
import org.keycloak.util.JsonSerialization;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Spec;

import static org.keycloak.client.admin.cli.KcAdmMain.CMD;
import static org.keycloak.client.admin.cli.KcAdmMain.V2_FLAG;
import static org.keycloak.client.cli.util.OsUtil.PROMPT;

/**
 * Keycloak Admin CLI v2（实验性）根命令。
 * <p>
 * 基于 OpenAPI 描述动态生成子命令，支持命令描述符缓存、Tab 补全及 JSON 输出高亮。
 * 通过 {@code kcadm --v2} 启用。
 */
@Command(name = "kcadm",
        description = "%nCOMMAND [ARGUMENTS]",
        footer = {"%nEnable tab completion:%n  source <(kcadm.sh --v2 completion)"}
)
public class KcAdmV2Cmd extends AbstractTargetAuthOptionsCmd {

    /**  classpath 内 bundled 命令描述符资源路径。 */
    private static final String BUNDLED_DESCRIPTOR = "/kcadm-v2-commands.json";
    /** 配置文件名（不含目录）。 */
    private static final String CONFIG_FILE_NAME = Path.of(KcAdmMain.DEFAULT_CONFIG_FILE_PATH).getFileName().toString();
    /** 命令描述符默认缓存目录。 */
    private static final Path DEFAULT_CACHE_DIR =
            Path.of(KcAdmMain.DEFAULT_CONFIG_FILE_PATH).getParent().resolve("command-descriptors").resolve("v2");
    /** 根命令 synopsis 中的连接选项占位提示。 */
    static final String CONNECTION_OPTIONS_HINT = "[CONNECTION OPTIONS]";
    /** {@code --config} 选项名常量。 */
    static final String CONFIG_OPTION = "--config";

    /** 命令描述符缓存目录实例。 */
    private final Path cacheDir;
    /** 从命令行参数解析出的配置文件路径。 */
    private final String configFilePath;

    /** Picocli 命令规格。 */
    @Spec
    CommandSpec spec;

    /** 使用默认缓存目录构造 v2 根命令。 */
    public KcAdmV2Cmd() {
        this(DEFAULT_CACHE_DIR);
    }

    /** 指定缓存目录构造 v2 根命令。 */
    public KcAdmV2Cmd(Path cacheDir) {
        this(cacheDir, null);
    }

    /** 从原始命令行参数构造，并解析 {@code --config} 路径。 */
    public KcAdmV2Cmd(String[] args) {
        this(DEFAULT_CACHE_DIR, args);
    }

    /** 完整构造器：指定缓存目录、命令行参数并解析配置路径。 */
    public KcAdmV2Cmd(Path cacheDir, String[] args) {
        this.cacheDir = cacheDir;
        this.configFilePath = findConfigPath(args);
    }

    @Override
    protected boolean nothingToDo() {
        return true;
    }

    @Override
    protected String help() {
        return "";
    }

    /** 输出 v2 根命令帮助并正常退出。 */
    @Override
    protected void printHelpIfNeeded() {
        PrintWriter out = new PrintWriter(System.out, true);
        out.println("Keycloak Admin CLI v2 (experimental)");
        out.println();
        out.println("Use '" + CMD + " " + V2_FLAG + " config credentials' to start a session.");
        out.println();
        out.println("For example:");
        out.println();
        out.println("  " + PROMPT + " " + CMD + " " + V2_FLAG + " config credentials --server http://localhost:8080 --realm master --user admin");
        out.println();
        spec.name(CMD + " " + V2_FLAG);
        spec.commandLine().usage(out);
        out.println();
        out.println("Use '" + CMD + " " + V2_FLAG + " <command> --help' for more information about a command.");
        out.println("Find more information at: https://www.keycloak.org/docs/latest");
        System.exit(CommandLine.ExitCode.OK);
    }

    /** 注册 config 子命令、加载命令描述符并动态挂载 OpenAPI 衍生的子命令树。 */
    @Override
    protected void configureCommandLine(CommandLine cli) {
        CommandSpec rootSpec = cli.getCommandSpec();
        rootSpec.name(CMD + " " + V2_FLAG + " " + CONNECTION_OPTIONS_HINT);

        // 根命令仅展示连接选项；若将来根级选项扩展，需同步更新 KcAdmV2CommandBuilder.connectionOptions
        rootSpec.usageMessage().optionListHeading("%nConnection options:%n");

        OptionSpec help = rootSpec.findOption("--help");
        if (help != null) {
            // 根命令帮助中隐藏 --help，避免出现在“连接选项”下；用户应对子命令使用 --help
            rootSpec.remove(help);
            rootSpec.add(help.toBuilder().hidden(true).build());
        }
        CommandLine configCmd = new CommandLine(new ConfigCmd(true));
        configCmd.getCommandSpec().usageMessage().description("Configuration management");
        configCmd.getCommandSpec().removeSubcommand("credentials");
        configCmd.addSubcommand("credentials", new CommandLine(new KcAdmV2ConfigCredentialsCmd(cacheDir)));
        CommandLine openApiCli = new CommandLine(new KcAdmV2ConfigOpenApiCmd(cacheDir));
        CommandLine editorCli = new CommandLine(new KcAdmV2ConfigEditorCmd());
        configCmd.addSubcommand("openapi", openApiCli);
        configCmd.addSubcommand("editor", editorCli);
        setConfigSubcommandSynopsis(openApiCli);
        setConfigSubcommandSynopsis(editorCli);
        cli.addSubcommand(configCmd);
        KcAdmV2CommandDescriptor descriptor = loadDescriptor();
        new KcAdmV2CommandBuilder(this, rootSpec).addCommands(cli, descriptor);
    }

    // config 子命令（openapi/editor）需自定义 synopsis，避免显示 [CONNECTION OPTIONS] 占位符
    private static void setConfigSubcommandSynopsis(CommandLine subCli) {
        CommandSpec spec = subCli.getCommandSpec();
        StringBuilder synopsis = new StringBuilder();
        synopsis.append(CMD).append(" ").append(V2_FLAG)
                .append(" ").append(ConfigCmd.NAME).append(" ").append(spec.name());
        for (OptionSpec opt : spec.options()) {
            if (!opt.hidden()) {
                synopsis.append(" [").append(opt.longestName());
                if (opt.arity().max() > 0) {
                    synopsis.append("=").append(opt.paramLabel());
                }
                synopsis.append("]");
            }
        }
        for (PositionalParamSpec pos : spec.positionalParameters()) {
            synopsis.append(" ").append(pos.paramLabel());
        }
        spec.usageMessage().customSynopsis(synopsis.toString());
    }

    /** 优先从服务器 URL 对应的缓存加载描述符，否则回退到 bundled 资源。 */
    private KcAdmV2CommandDescriptor loadDescriptor() {
        KcAdmV2DescriptorCache cache = new KcAdmV2DescriptorCache(cacheDir);

        String serverUrl = readServerUrlFromConfig();
        if (serverUrl != null) {
            KcAdmV2CommandDescriptor cached = cache.loadForServer(serverUrl);
            if (cached != null) {
                return cached;
            }
        }

        return loadBundledDescriptor();
    }

    /** 从命令行指定路径、缓存目录或默认位置读取 server URL。 */
    private String readServerUrlFromConfig() {
        if (configFilePath != null) {
            String fromConfig = readServerUrlFrom(configFilePath);
            if (fromConfig != null) {
                return fromConfig;
            }
        }
        String fromCacheDir = readServerUrlFrom(cacheDir.resolve(CONFIG_FILE_NAME).toString());
        if (fromCacheDir != null) {
            return fromCacheDir;
        }
        return readServerUrlFrom(KcAdmMain.DEFAULT_CONFIG_FILE_PATH);
    }

    /** 从配置文件读取 {@code serverUrl} 字段；文件不存在或解析失败时返回 null。 */
    static String readServerUrlFrom(String configFilePath) {
        try {
            File configFile = new File(configFilePath);
            if (!configFile.isFile()) {
                return null;
            }
            try (FileInputStream is = new FileInputStream(configFile)) {
                ConfigData config = JsonSerialization.readValue(is, ConfigData.class);
                return config.getServerUrl();
            }
        } catch (Exception e) {
            return null;
        }
    }

    /** 扫描命令行参数，提取 {@code --config} 后的文件路径。 */
    private static String findConfigPath(String[] args) {
        if (args != null) {
            for (int i = 0; i < args.length - 1; i++) {
                if (CONFIG_OPTION.equals(args[i])) {
                    return args[i + 1];
                }
            }
        }
        return null;
    }

    /** 从 classpath bundled JSON 加载默认命令描述符。 */
    public static KcAdmV2CommandDescriptor loadBundledDescriptor() {
        try (InputStream is = KcAdmV2Cmd.class.getResourceAsStream(BUNDLED_DESCRIPTOR)) {
            if (is == null) {
                throw new RuntimeException("Bundled command descriptor not found: " + BUNDLED_DESCRIPTOR);
            }
            return KcAdmV2DescriptorBuilder.readDescriptor(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load command descriptor", e);
        }
    }
}
