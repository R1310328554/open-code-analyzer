package org.keycloak.client.admin.cli.v2;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.client.admin.cli.v2.KcAdmV2CommandDescriptor.CommandDescriptor;
import org.keycloak.client.admin.cli.v2.KcAdmV2CommandDescriptor.OptionDescriptor;
import org.keycloak.client.admin.cli.v2.KcAdmV2CommandDescriptor.ResourceDescriptor;
import org.keycloak.client.admin.cli.v2.KcAdmV2CommandDescriptor.VariantDescriptor;

import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Model.PositionalParamSpec;
import picocli.CommandLine.Model.UsageMessageSpec;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

import static org.keycloak.client.admin.cli.KcAdmMain.CMD;
import static org.keycloak.client.admin.cli.KcAdmMain.V2_FLAG;
import static org.keycloak.common.util.ObjectUtil.capitalize;

/**
 * 将 {@link KcAdmV2CommandDescriptor} 转换为 PicoCLI 命令树。
 * <p>
 * 按资源分组注册子命令，处理变体（如 oidc/saml）、连接选项分区及 {@code edit} 合成命令。
 */
final class KcAdmV2CommandBuilder {

    private static final String OPT_HELP = "--help";
    /** 请求体 JSON 文件选项短名。 */
    static final String OPT_FILE = "-f";
    /** 压缩 JSON 输出（不美化）选项。 */
    static final String OPT_COMPRESSED = "--compressed";
    private static final String CMD_EDIT = "edit";
    private static final String CONNECTION_OPTIONS_SECTION = "connectionOptions";
    private static final String CONNECTION_OPTIONS_HEADING = "%nConnection options (must precede the subcommand):%n";

    private final KcAdmV2Cmd root;
    /** 根命令上的连接/认证选项，用于在帮助中单独展示。 */
    private final List<OptionSpec> connectionOptions;

    /** 从根命令规格中提取可见连接选项。 */
    KcAdmV2CommandBuilder(KcAdmV2Cmd root, CommandSpec rootSpec) {
        this.root = root;
        this.connectionOptions = rootSpec.options().stream().filter(o -> !o.hidden()).toList();
    }

    /** 将描述符中的全部资源与子命令挂载到给定 CLI 实例。 */
    void addCommands(CommandLine cli, KcAdmV2CommandDescriptor descriptor) {
        for (ResourceDescriptor resource : descriptor.getResources()) {
            GroupCommand groupCommand = new GroupCommand(resource.getName());
            CommandSpec groupSpec = CommandSpec.wrapWithoutInspection(groupCommand);
            groupSpec.name(resource.getName());
            groupSpec.usageMessage().header(capitalize(resource.getName()) + " operations");
            addHelpOption(groupSpec);
            addConnectionOptionsSection(groupSpec);

            CommandLine groupCli = new CommandLine(groupSpec);
            groupCommand.setSpec(groupSpec);

            CommandDescriptor getDescriptor = null;
            CommandDescriptor putDescriptor = null;

            for (CommandDescriptor cmd : resource.getCommands()) {
                groupCli.addSubcommand(cmd.getName(), buildSubcommand(cmd));
                if (KcAdmV2DescriptorBuilder.CMD_NAME_GET.equals(cmd.getName())) {
                    getDescriptor = cmd;
                } else if (KcAdmV2DescriptorBuilder.CMD_NAME_APPLY.equals(cmd.getName())) {
                    putDescriptor = cmd;
                }
            }

            if (getDescriptor != null && putDescriptor != null) {
                groupCli.addSubcommand(CMD_EDIT, buildEditCommand(getDescriptor, putDescriptor));
            }

            cli.addSubcommand(resource.getName(), groupCli);
        }
    }

    private CommandLine buildSubcommand(CommandDescriptor cmd) {
        if (cmd.hasVariants()) {
            return buildVariantParentCommand(cmd);
        }

        return buildLeafCommand(cmd, cmd.getOptions(), null);
    }

    private CommandLine buildVariantParentCommand(CommandDescriptor cmd) {
        CommandLine parentCli = buildLeafCommand(cmd, null, null);

        for (VariantDescriptor variant : cmd.getVariants()) {
            parentCli.addSubcommand(variant.getName(),
                    buildLeafCommand(cmd, variant.getOptions(), variant));
        }

        // -f 与变体子命令互斥：要么提供 JSON 文件，要么选择变体（如 oidc/saml）以获取协议专属选项
        String variants = parentCli.getSubcommands().keySet().stream().sorted().collect(joining(" | "));
        parentCli.getCommandSpec().usageMessage().customSynopsis(
                CMD + " " + V2_FLAG + " " + KcAdmV2Cmd.CONNECTION_OPTIONS_HINT + " "
                        + cmd.getResourceName() + " " + cmd.getName()
                        + " [" + OPT_FILE + " <file> | " + variants + "]");

        return parentCli;
    }

    private CommandLine buildLeafCommand(CommandDescriptor cmd,
            List<OptionDescriptor> options, VariantDescriptor variant) {
        boolean isVariantParent = variant == null && cmd.hasVariants();

        CommandSpec spec = new KcAdmV2RequestExecutor(root, cmd, variant).getSpec();
        spec.name(variant != null ? variant.getName() : cmd.getName());
        spec.usageMessage().description(cmd.getDescription());
        addHelpOption(spec);
        addDumpTraceOption(spec);

        if (cmd.isHasResponseBody()) {
            addOutputGroup(spec);
        }

        String idField = KcAdmV2RequestExecutor.RESOURCE_ID_FIELDS.get(cmd.getResourceName());
        boolean hasIdOption = idField != null && options != null
                && options.stream().anyMatch(o -> !o.isQueryParam() && idField.equals(o.getFieldName()));
        boolean isCreate = KcAdmV2DescriptorBuilder.CMD_NAME_CREATE.equals(cmd.getName());
        if (!isVariantParent && cmd.isRequiresId()) {
            addIdPositional(spec, cmd.getResourceName(), !cmd.hasRequestBody());
        } else if (isCreate && hasIdOption) {
            // 对含已知 ID 字段的 create 命令（如 client → clientId），增加可选 <id> 位置参数，
            // 使用户可写 "create oidc my-client" 而非 "--client-id"；隐藏的 --client-id 保留向后兼容
            addIdPositional(spec, cmd.getResourceName(), false);
        }

        boolean hasBodyOptions = options != null && options.stream().anyMatch(not(OptionDescriptor::isQueryParam));
        if (hasBodyOptions || isVariantParent) {
            ArgGroupSpec.Builder fieldGroup = ArgGroupSpec.builder()
                    .heading("%nOptions:%n")
                    .exclusive(false)
                    .validate(false)
                    .order(1);

            // 变体叶子节点隐藏 -f（文件无需鉴别器，用户可直接 "create -f file.json"）
            boolean isVariantLeaf = variant != null;
            fieldGroup.addArg(buildFileOption(hasBodyOptions, isVariantLeaf));

            if (hasBodyOptions) {
                for (OptionDescriptor opt : options) {
                    if (!opt.isQueryParam()) {
                        boolean hideIdOption = hasIdOption && idField.equals(opt.getFieldName());
                        fieldGroup.addArg(buildOption(opt, hideIdOption));
                    }
                }
            }

            spec.addArgGroup(fieldGroup.build());
        }

        boolean hasQueryOptions = options != null && options.stream().anyMatch(OptionDescriptor::isQueryParam);
        if (hasQueryOptions) {
            ArgGroupSpec.Builder queryGroup = ArgGroupSpec.builder()
                    .heading("%nQuery options:%n")
                    .exclusive(false)
                    .validate(false)
                    .order(2);

            for (OptionDescriptor opt : options) {
                if (opt.isQueryParam()) {
                    queryGroup.addArg(buildOption(opt, false));
                }
            }

            spec.addArgGroup(queryGroup.build());
        }

        addConnectionOptionsSection(spec);

        return new CommandLine(spec);
    }

    private static OptionSpec buildOption(OptionDescriptor opt, boolean hidden) {
        String description = opt.getDescription() != null ? opt.getDescription() : "";
        List<String> enumValues = opt.getEnumValues();
        if (enumValues != null && !enumValues.isEmpty()) {
            description += (description.isEmpty() ? "" : " ") + "Valid values: " + String.join(", ", enumValues);
        }

        String paramLabel = "<value>";
        if (enumValues != null && !enumValues.isEmpty()) {
            paramLabel = "<" + String.join("|", enumValues) + ">";
        }

        OptionSpec.Builder builder = OptionSpec.builder("--" + opt.getName())
                .type(opt.isArray() ? String[].class : String.class)
                .paramLabel(paramLabel)
                .hidden(hidden)
                .description(description);

        if (opt.isArray()) {
            builder.splitRegex(",");
            if (enumValues != null && !enumValues.isEmpty()) {
                builder.hideParamSyntax(true);
                paramLabel += "[,...]";
                builder.paramLabel(paramLabel);
            }
        }

        if (enumValues != null && !enumValues.isEmpty()) {
            builder.completionCandidates(enumValues);
        }

        return builder.build();
    }

    private static OptionSpec buildFileOption(boolean hasFieldOptions, boolean hidden) {
        String description = hasFieldOptions
                ? "JSON file with request body (mutually exclusive with field options)"
                : "JSON file with request body";
        return OptionSpec.builder(OPT_FILE, "--file")
                .type(String.class)
                .paramLabel("<file>")
                .description(description)
                .hidden(hidden)
                .build();
    }

    private CommandLine buildEditCommand(CommandDescriptor getCmd, CommandDescriptor putCmd) {
        String resourceName = getCmd.getResourceName();

        CommandSpec spec = new KcAdmV2EditCmd(root, getCmd, putCmd).getSpec();
        spec.name(CMD_EDIT);
        spec.usageMessage().description(KcAdmV2EditCmd.createDescription(resourceName));
        addHelpOption(spec);
        addDumpTraceOption(spec);
        addOutputGroup(spec);
        addIdPositional(spec, resourceName, true);
        addConnectionOptionsSection(spec);

        return new CommandLine(spec);
    }

    private static void addIdPositional(CommandSpec spec, String resourceName, boolean required) {
        spec.addPositional(PositionalParamSpec.builder()
                .index("0")
                .paramLabel("<id>")
                .description(capitalize(resourceName) + " identifier")
                .required(required)
                .type(String.class)
                .build());
    }

    private static void addOutputGroup(CommandSpec spec) {
        spec.addArgGroup(ArgGroupSpec.builder()
                .heading("%nOutput options:%n")
                .exclusive(false)
                .validate(false)
                .order(10)
                .addArg(OptionSpec.builder("-c", OPT_COMPRESSED)
                        .type(boolean.class)
                        .description("Don't pretty print the output")
                        .build())
                .build());
    }

    private static void addHelpOption(CommandSpec spec) {
        spec.addOption(OptionSpec.builder("-h", OPT_HELP)
                .usageHelp(true)
                .hidden(true)
                .build());
    }

    private void addConnectionOptionsSection(CommandSpec spec) {
        UsageMessageSpec usage = spec.usageMessage();
        List<String> keys = new ArrayList<>(usage.sectionKeys());

        // 变体父命令的帮助中，连接选项须列在子命令列表之前
        int commandListIndex = keys.indexOf(UsageMessageSpec.SECTION_KEY_COMMAND_LIST_HEADING);
        if (commandListIndex >= 0) {
            keys.add(commandListIndex, CONNECTION_OPTIONS_SECTION);
        } else {
            keys.add(CONNECTION_OPTIONS_SECTION);
        }
        usage.sectionKeys(keys);

        // 连接选项作为自定义分区展示，避免出现在叶子命令的自动补全中
        usage.sectionMap().put(CONNECTION_OPTIONS_SECTION, help -> {
            Help.Layout layout = help.createDefaultLayout(connectionOptions, List.of(), help.colorScheme());
            for (OptionSpec opt : connectionOptions) {
                layout.addOption(opt, help.parameterLabelRenderer());
            }
            return help.createHeading(CONNECTION_OPTIONS_HEADING) + layout;
        });
    }

    private static void addDumpTraceOption(CommandSpec spec) {
        spec.addOption(OptionSpec.builder("-x")
                .type(boolean.class)
                .description("Print full stack trace when exiting with error")
                .hidden(true)
                .build());
    }

    /** 资源分组占位命令：无子命令参数时提示用户使用 {@code --help}。 */
    static class GroupCommand implements Runnable {
        private final String name;
        private CommandSpec spec;

        GroupCommand(String name) {
            this.name = name;
        }

        void setSpec(CommandSpec spec) {
            this.spec = spec;
        }

        @Override
        public void run() {
            spec.commandLine().getErr().println(
                    "Use '" + CMD + " " + V2_FLAG + " " + name + " --help' for available commands.");
        }
    }
}
