/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.cli;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.keycloak.common.profile.ProfileException;
import org.keycloak.config.DeprecatedMetadata;
import org.keycloak.config.Option;
import org.keycloak.config.OptionCategory;
import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.quarkus.runtime.KeycloakMain;
import org.keycloak.quarkus.runtime.Messages;
import org.keycloak.quarkus.runtime.cli.command.AbstractAutoBuildCommand;
import org.keycloak.quarkus.runtime.cli.command.AbstractCommand;
import org.keycloak.quarkus.runtime.cli.command.AbstractNonServerCommand;
import org.keycloak.quarkus.runtime.cli.command.Build;
import org.keycloak.quarkus.runtime.cli.command.Main;
import org.keycloak.quarkus.runtime.cli.command.ShowConfig;
import org.keycloak.quarkus.runtime.cli.command.Tools;
import org.keycloak.quarkus.runtime.cli.command.WindowsService;
import org.keycloak.quarkus.runtime.configuration.ConfigArgsConfigSource;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.quarkus.runtime.configuration.KcUnmatchedArgumentException;
import org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider;
import org.keycloak.quarkus.runtime.configuration.PropertyMappingInterceptor;
import org.keycloak.quarkus.runtime.configuration.QuarkusPropertiesConfigSource;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMappers;

import io.quarkus.bootstrap.runner.QuarkusEntryPoint;
import io.quarkus.dev.console.TerminalUtils;
import io.quarkus.runtime.LaunchMode;
import io.smallrye.config.ConfigValue;
import io.smallrye.mutiny.tuples.Functions.TriConsumer;
import picocli.CommandLine;
import picocli.CommandLine.DuplicateOptionAnnotationsException;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.ISetter;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;

import static java.lang.String.format;

import static org.keycloak.quarkus.runtime.Environment.getProviderFiles;
import static org.keycloak.quarkus.runtime.configuration.Configuration.isUserModifiable;
import static org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST;

/**
 * Keycloak Quarkus CLI 核心门面：构建 Picocli 命令树、解析参数、校验配置并驱动启动/构建。
 */
public class Picocli {

    /** Provider JAR 时间戳变更且需完全重建时的错误消息。 */
    static final String PROVIDER_TIMESTAMP_ERROR = "A provider JAR was updated since the last build, please rebuild for this to be fully utilized.";
    /** 容器内 Provider 时间戳与构建时不一致时的警告消息。 */
    static final String PROVIDER_TIMESTAMP_WARNING = "A provider jar has a different timestamp than when the optimized container image was created. If you are changing provider jars after the build, you must run another build to properly account for those modifications.";
    /** 持久化 Provider 文件时间戳属性的键前缀。 */
    static final String KC_PROVIDER_FILE_PREFIX = "kc.provider.file.";
    /** 长选项前缀。 */
    public static final String ARG_PREFIX = "--";
    /** 短选项前缀。 */
    public static final String ARG_SHORT_PREFIX = "-";
    /** 表示无参数占位符的特殊标签。 */
    public static final String NO_PARAM_LABEL = "none";

    /** 控制帮助/校验时是否包含运行时、构建时选项及未识别参数。 */
    private record IncludeOptions(boolean includeRuntime, boolean includeBuildTime, boolean allowUnrecognized) {
    }

    private final ExecutionExceptionHandler errorHandler = new ExecutionExceptionHandler();
    /** 当前解析到的 AbstractCommand，配置初始化后可用。 */
    private Optional<AbstractCommand> parsedCommand = Optional.empty();
    /** 是否已输出 Provider 时间戳警告（容器场景仅警告一次）。 */
    private boolean warnedTimestampChanged;

    private Ansi colorMode = hasColorSupport() ? Ansi.ON : Ansi.OFF;
    /** 由当前命令推导的选项包含策略。 */
    private IncludeOptions options;
    /** CLI 中重复出现的选项名，用于校验后警告。 */
    private Set<String> duplicatedOptionsNames = new HashSet<String>();

    /** @return 终端是否支持 ANSI 颜色 */
    public static boolean hasColorSupport() {
        return TerminalUtils.hasColorSupport();
    }

    /** @return 当前 ANSI 颜色模式 */
    public Ansi getColorMode() {
        return colorMode;
    }

    /** 递归判断解析结果或其子命令是否请求了 --help。 */
    private boolean isHelpRequested(ParseResult result) {
        if (result.isUsageHelpRequested()) {
            return true;
        }

        return result.subcommands().stream().anyMatch(this::isHelpRequested);
    }

    /**
     * 解析 CLI 参数、初始化配置、校验选项并执行命令。
     *
     * @param cliArgs 命令行参数字符串列表
     */
    public void parseAndRun(List<String> cliArgs) {
        List<String> unrecognizedArgs = new ArrayList<>();
        CommandLine cmd = createCommandLine(unrecognizedArgs);

        String[] argArray = cliArgs.toArray(new String[0]);

        try {
            ParseResult result = cmd.parseArgs(argArray);

            var commandLineList = result.asCommandLineList();

            CommandLine cl = commandLineList.get(commandLineList.size() - 1);

            AbstractCommand currentCommand;
            if (cl.getCommand() instanceof AbstractCommand ac) {
                currentCommand = ac;
            } else {
                currentCommand = null;
            }

            // 未识别参数可规范化为基于 PropertyMapper 的键值对
            Map<String, String> normalizedArgs = new LinkedHashMap<String, String>();
            List<String> unknown = new ArrayList<String>();
            ConfigArgsConfigSource.parseConfigArgs(unrecognizedArgs, (k, v) -> {
                if (normalizedArgs.put(k, v) != null) {
                    duplicatedOptionsNames.add(k);
                }
            }, unknown::add);
            unrecognizedArgs = null;

            ConfigArgsConfigSource.setCliArgs(normalizedArgs.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).toArray(String[]::new));

            initConfig(currentCommand);

            // PropertyMapper 就绪后进一步精炼参数
            if (options.allowUnrecognized) {
                normalizedArgs.keySet().removeIf(arg -> PropertyMappers.getMapperByCliKey(arg) != null || arg.startsWith(ConfigArgsConfigSource.SPI_OPTION_PREFIX));
            }
            unknown.forEach(arg -> {
                if (PropertyMappers.getMapperByCliKey(arg) != null) {
                    addCommandOptions(cl, currentCommand);
                    throw new MissingParameterException(cl, cl.getCommandSpec().optionsMap().get(arg), null);
                } else if (arg.startsWith(ConfigArgsConfigSource.SPI_OPTION_PREFIX)) {
                    throw new PropertyException(format("spi argument %s requires a value.", arg));
                }
            });
            unknown.addAll(normalizedArgs.keySet());
            if (!unknown.isEmpty()) {
                addCommandOptions(cl, currentCommand);
                throw new KcUnmatchedArgumentException(cl, unknown);
            }

            if (isHelpRequested(result)) {
                addCommandOptions(cl, currentCommand);
            }

            // ParseResult 占用内存，命令执行前释放引用
            result = null;

            // execute 内部会再次创建 ParseResult；复用需重构上述逻辑或复制 execute 内逻辑
            int exitCode = execute(cmd, argArray);

            exit(exitCode);
        } catch (ParameterException parEx) {
            catchParameterException(parEx, cmd, argArray);
        } catch (ProfileException | PropertyException proEx) {
            usageException(proEx.getMessage(), proEx.getCause());
        }
    }

    /** 子类可覆盖以自定义 execute 行为（测试用）。 */
    protected int execute(CommandLine cmd, String[] argArray) {
        return cmd.execute(argArray);
    }

    /** @return 当前已解析的 AbstractCommand（若存在） */
    public Optional<AbstractCommand> getParsedCommand() {
        return parsedCommand;
    }

    /** 处理 ParameterException 并退出进程。 */
    private void catchParameterException(ParameterException parEx, CommandLine cmd, String[] args) {
        int exitCode;
        try {
            exitCode = cmd.getParameterExceptionHandler().handleParseException(parEx, args);
        } catch (Exception e) {
            errorHandler.error(cmd.getErr(), e.getMessage(), null);
            exitCode = parEx.getCommandLine().getCommandSpec().exitCodeOnInvalidInput();
        }
        exit(exitCode);
    }

    /** 输出用法错误并以 USAGE 退出码终止。 */
    public void usageException(String message, Throwable cause) {
        errorHandler.error(getErrWriter(), message, cause);
        exit(CommandLine.ExitCode.USAGE);
    }

    /** 以给定退出码终止 JVM（子类可改为 Quarkus.asyncExit）。 */
    public void exit(int exitCode) {
        System.exit(exitCode);
    }

    /** @return 是否曾执行过 build（持久化配置非空） */
    private static boolean wasBuildEverRun() {
        return !Configuration.getRawPersistedProperties().isEmpty();
    }

    /**
     * 校验当前命令的配置：必填项、禁用项、弃用项、Provider 变更等。
     * 额外验证并处理已弃用选项。
     */
    public void validateConfig() {
        AbstractCommand abstractCommand = this.getParsedCommand().orElseThrow();
        if (abstractCommand.isOptimized() && !wasBuildEverRun()) {
            throw new PropertyException(Messages.optimizedUsedForFirstStartup());
        }
        warnOnDuplicatedOptionsInCli();

        if (!options.includeBuildTime && !options.includeRuntime) {
            return;
        }

        if (!options.includeBuildTime) {
            validateBuildtime();
        }

        final List<String> ignoredRunTime = new ArrayList<>();
        final Set<String> disabledBuildTime = new LinkedHashSet<>();
        final Set<String> disabledRunTime = new LinkedHashSet<>();
        final Set<String> deprecatedInUse = new LinkedHashSet<>();
        final Set<String> missingOption = new LinkedHashSet<>();
        final Set<String> ambiguousSpi = new LinkedHashSet<>();
        final Set<String> unnecessary = new LinkedHashSet<>();
        final LinkedHashMap<String, String> secondClassOptions = new LinkedHashMap<>();

        final Set<PropertyMapper<?>> disabledMappers = new HashSet<>();
        if (options.includeBuildTime) {
            disabledMappers.addAll(PropertyMappers.getDisabledBuildTimeMappers().values());
        }
        if (options.includeRuntime) {
            disabledMappers.addAll(PropertyMappers.getDisabledRuntimeMappers().values());
        }

        // 第一遍：校验已出现的属性名，便于通配符与 SPI 解析
        Configuration.getPropertyNames().forEach(name -> {
            if (name.startsWith(PropertyMappers.KC_SPI_PREFIX)) {
                if (!options.includeRuntime) {
                    checkRuntimeSpiOptions(name, ignoredRunTime);
                }
                if (PropertyMappers.isMaybeSpiBuildTimeProperty(name)) {
                    ambiguousSpi.add(name);
                }
            }
            PropertyMapper<?> mapper = PropertyMappers.getMapper(name);
            if (mapper == null || mapper.getOption().isSynthetic()) {
                return; // TODO: need to look for disabled Wildcard mappers
            }
            var forKey = mapper.forKey(name);
            if (!name.equals(forKey.getFrom())) {
                ConfigValue value = getUnmappedValue(name);
                if (value.getValue() != null && isUserModifiable(value)) {
                    secondClassOptions.put(name, forKey.getFrom());
                }
            }
            if (!mapper.hasWildcard() // 非通配符选项在第二遍校验
                    // 仅校验映射到 kc. 的规范键
                    || !name.startsWith(MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX)) {
                return;
            }
            validateProperty(abstractCommand, options, ignoredRunTime, disabledBuildTime, disabledRunTime,
                    deprecatedInUse, missingOption, false, forKey, unnecessary);
        });

        // 第二遍：校验第一遍未覆盖的 PropertyMapper（必填、禁用等）
        for (PropertyMapper<?> mapper : PropertyMappers.getMappers()) {
            if (!mapper.hasWildcard() && !mapper.getOption().isSynthetic()) {
                validateProperty(abstractCommand, options, ignoredRunTime, disabledBuildTime, disabledRunTime,
                        deprecatedInUse, missingOption, false, mapper, unnecessary);
            }
        }

        PropertyMappers.getPropertyMapperGroupings().forEach(g -> g.validateConfig(this));

        // 第三遍：检查已禁用的映射器
        for (PropertyMapper<?> mapper : disabledMappers) {
            if (!mapper.hasWildcard() && !mapper.getOption().isSynthetic()) {
                validateProperty(abstractCommand, options, ignoredRunTime, disabledBuildTime, disabledRunTime,
                        deprecatedInUse, missingOption, true, mapper, unnecessary);
            }
        }

        if (!missingOption.isEmpty()) {
            throw new PropertyException("The following options are required: \n%s".formatted(String.join("\n", missingOption)));
        }
        if (!ignoredRunTime.isEmpty()) {
            info(format("The following run time options were found, but will be ignored during build time: %s\n",
                    String.join(", ", ignoredRunTime)));
        }

        if (!disabledBuildTime.isEmpty()) {
            outputDisabledProperties(disabledBuildTime, true);
        } else if (!disabledRunTime.isEmpty()) {
            outputDisabledProperties(disabledRunTime, false);
        }

        if (!deprecatedInUse.isEmpty()) {
            warn("The following used options or option values are DEPRECATED and will be removed or their behaviour changed in a future release:\n" + String.join("\n", deprecatedInUse) + "\nConsult the Release Notes for details.");
        }
        if (!ambiguousSpi.isEmpty()) {
            warn("The following SPI options are using the legacy format and are not being treated as build time options. Please use the new format with the appropriate -- separators to resolve this ambiguity: " + String.join("\n", ambiguousSpi));
        }
        secondClassOptions.forEach((key, firstClass) -> {
            if (Configuration.getConfigValue(firstClass).getConfigSourceName() != null) {
                warn("With the first-class option `%s` set, you should remove the usage of `%s`".formatted(firstClass, key));
            } else {
                warn("Please use the first-class option `%s` instead of `%s`".formatted(firstClass, key));
            }
        });
        if (!unnecessary.isEmpty()) {
            info("The following options were specified, but are typically not relevant for this command: " + String.join("\n", unnecessary));
        }
    }

    /** 校验构建时选项是否与持久化值一致，并检测 Provider JAR 变更。 */
    private void validateBuildtime() {
        final List<String> ignoredBuildTime = new ArrayList<>();
        // 检查 Provider 变更或持久化选项被覆盖；Profile 等由命令在运行时设置，此处忽略
        checkChangesInBuildOptions((key, oldValue, newValue) -> {
            if (key.startsWith(KC_PROVIDER_FILE_PREFIX)) {
                boolean changed = false;
                if (newValue == null || oldValue == null) {
                    changed = true;
                } else if (!warnedTimestampChanged && timestampChanged(oldValue, newValue)) {
                    if (Environment.isRunInContainer()) {
                        warnedTimestampChanged = true;
                        warn(PROVIDER_TIMESTAMP_WARNING);
                    } else {
                        changed = true;
                    }
                }
                if (changed) {
                    throw new PropertyException(PROVIDER_TIMESTAMP_ERROR);
                }
            } else if (newValue != null && !isIgnoredPersistedOption(key)
                    && isUserModifiable(Configuration.getConfigValue(key))
                    // Quarkus 前缀项由 Quarkus 处理，Keycloak 不支持直接使用
                    && !key.startsWith(MicroProfileConfigProvider.NS_QUARKUS_PREFIX)) {
                ignoredBuildTime.add(key);
            }
        });

        if (!ignoredBuildTime.isEmpty()) {
            throw new PropertyException(format("The following build time options have values that differ from what is persisted - the new values will NOT be used until another build is run: %s\n",
                    String.join(", ", ignoredBuildTime)));
        }
    }

    /**
     * 比较新旧 Provider 文件时间戳；Docker 常截断到秒，允许该特例。
     */
    static boolean timestampChanged(String oldValue, String newValue) {
        long longNewValue = Long.valueOf(newValue);
        long longOldValue = Long.valueOf(oldValue);
        // docker 运行时常见秒级截断，对此做特殊容忍
        return ((longNewValue / 1000) * 1000) != longNewValue || ((longOldValue / 1000) * 1000) != longNewValue;
    }

    /** 临时禁用 PropertyMappingInterceptor 以读取未映射的原始配置值。 */
    private ConfigValue getUnmappedValue(String key) {
        PropertyMappingInterceptor.disable();
        try {
            return Configuration.getConfigValue(key);
        } finally {
            PropertyMappingInterceptor.enable();
        }
    }

    /** 对单个 PropertyMapper 执行必填、禁用、弃用与多余选项校验。 */
    private void validateProperty(AbstractCommand abstractCommand, IncludeOptions options,
            final List<String> ignoredRunTime, final Set<String> disabledBuildTime, final Set<String> disabledRunTime,
            final Set<String> deprecatedInUse, final Set<String> missingOption,
            boolean disabled, PropertyMapper<?> mapper, final Set<String> unnecessary) {
        if (mapper.isBuildTime() && !options.includeBuildTime) {
            return; // 构建时选项已在 validateBuildtime 中检查变更
        }
        boolean ignoreRuntime = mapper.isRunTime() && !options.includeRuntime;

        ConfigValue configValue = getUnmappedValue(mapper.getFrom());
        String configValueStr = configValue.getValue();

        if (configValueStr == null) {
            if (!ignoreRuntime && mapper.isRequired()) {
                handleRequired(missingOption, mapper);
            }
            return;
        }

        if (!isUserModifiable(configValue)) {
            return;
        }

        if (disabled) {
            // 无启用映射器且非 CLI 来源的值视为禁用
            if (PropertyMappers.getMapper(mapper.getFrom()) == null
                    && !PropertyMapper.isCliOption(configValue)) {
                handleDisabled(mapper.isRunTime() ? disabledRunTime : disabledBuildTime, mapper);
            }
            return;
        }

        if (ignoreRuntime) {
            ignoredRunTime.add(mapper.getFrom());
            return;
        }

        mapper.validate(configValue);

        mapper.getDeprecatedMetadata().ifPresent(metadata -> handleDeprecated(deprecatedInUse, mapper, configValueStr, metadata));

        if (mapper.isRunTime() && PropertyMapper.isCliOption(configValue) && abstractCommand.isHiddenCategory(mapper.getCategory())) {
            unnecessary.add(mapper.getCliFormat());
        }
    }

    /** 在仅构建阶段将运行时 SPI 选项记入 ignoredRunTime 列表。 */
    private static void checkRuntimeSpiOptions(String key, final List<String> ignoredRunTime) {
        boolean buildTimeOption = PropertyMappers.isSpiBuildTimeProperty(key);

        if (!buildTimeOption) {
            ConfigValue configValue = Configuration.getConfigValue(key);
            String configValueStr = configValue.getValue();

            // 忽略缺失值及低于标准环境变量优先级的配置
            if (configValueStr != null && isUserModifiable(configValue)) {
                ignoredRunTime.add(key);
            }
        }
    }

    /** 收集弃用选项/取值的使用说明行。 */
    private static void handleDeprecated(Set<String> deprecatedInUse, PropertyMapper<?> mapper, String configValue,
            DeprecatedMetadata metadata) {
        Set<String> deprecatedValuesInUse = new HashSet<>();
        if (!metadata.getDeprecatedValues().isEmpty()) {
            deprecatedValuesInUse.addAll(Arrays.asList(configValue.split(",")));
            deprecatedValuesInUse.retainAll(metadata.getDeprecatedValues());

            if (deprecatedValuesInUse.isEmpty()) {
                return; // 未使用弃用取值则不警告
            }
        }

        String optionName = mapper.getFrom();
        if (optionName.startsWith(NS_KEYCLOAK_PREFIX)) {
            optionName = optionName.substring(NS_KEYCLOAK_PREFIX.length());
        }

        StringBuilder sb = new StringBuilder("\t- ");
        sb.append(optionName);

        if (!deprecatedValuesInUse.isEmpty()) {
            sb.append("=").append(String.join(",", deprecatedValuesInUse));
        }

        if (metadata.getNote() != null || !metadata.getNewOptionsKeys().isEmpty()) {
            sb.append(":");
        }
        if (metadata.getNote() != null) {
            sb.append(" ");
            sb.append(metadata.getNote());
            if (!metadata.getNote().endsWith(".")) {
                sb.append(".");
            }
        }
        if (!metadata.getNewOptionsKeys().isEmpty()) {
            sb.append(" Use ");
            sb.append(String.join(", ", metadata.getNewOptionsKeys()));
            sb.append(".");
        }
        deprecatedInUse.add(sb.toString());
    }

    /** 记录禁用选项及 enabledWhen 说明。 */
    private static void handleDisabled(Set<String> disabledInUse, PropertyMapper<?> mapper) {
        handleMessage(disabledInUse, mapper, PropertyMapper::getEnabledWhen);
    }

    /** 记录必填选项及 requiredWhen 说明。 */
    private static void handleRequired(Set<String> requiredOptions, PropertyMapper<?> mapper) {
        handleMessage(requiredOptions, mapper, PropertyMapper::getRequiredWhen);
    }

    private static void handleMessage(Set<String> messages, PropertyMapper<?> mapper, Function<PropertyMapper<?>, Optional<String>> retrieveMessage) {
        var optionName = mapper.getOption().getKey();
        final StringBuilder sb = new StringBuilder("\t- ");
        sb.append(optionName);
        retrieveMessage.apply(mapper).ifPresent(msg -> sb.append(": ").append(msg).append("."));
        messages.add(sb.toString());
    }

    /** 向 stdout 输出绿色 INFO 前缀消息。 */
    public void info(String text) {
        ColorScheme defaultColorScheme = picocli.CommandLine.Help.defaultColorScheme(colorMode);
        getOutWriter().println(defaultColorScheme.apply("INFO: ", Arrays.asList(Style.fg_green, Style.bold)) + text);
    }

    /** 向 stderr 输出红色错误消息。 */
    public void error(String text) {
        ColorScheme defaultColorScheme = picocli.CommandLine.Help.defaultColorScheme(colorMode);
        getErrWriter().println(defaultColorScheme.apply(text, Arrays.asList(Style.fg_red, Style.bold)));
    }

    /** 向 stdout 输出黄色 WARNING 前缀消息。 */
    public void warn(String text) {
        ColorScheme defaultColorScheme = picocli.CommandLine.Help.defaultColorScheme(colorMode);
        getOutWriter().println(defaultColorScheme.apply("WARNING: ", Arrays.asList(Style.fg_yellow, Style.bold)) + text);
    }

    /** 输出在当前阶段不可用的构建时/运行时选项列表。 */
    private void outputDisabledProperties(Set<String> properties, boolean build) {
        warn(format("The following used %s time options are UNAVAILABLE and will be ignored during %s time:\n %s",
                build ? "build" : "run", build ? "run" : "build",
                String.join("\n", properties)));
    }

    /**
     * 收集当前应持久化的构建时选项（含 Quarkus 与 Provider 时间戳）。
     *
     * @return 待写入 PersistedConfigSource 的属性集
     */
    public static Properties getNonPersistedBuildTimeOptions() {
        Properties properties = new Properties();
        // TODO: 可仅获取非持久化属性名
        Configuration.getPropertyNames().forEach(name -> {
            boolean quarkus = false;
            PropertyMapper<?> mapper = PropertyMappers.getMapper(name);
            if (mapper != null) {
                if (!mapper.isBuildTime()) {
                    return;
                }
                name = mapper.forKey(name).getFrom();
                if (properties.containsKey(name)) {
                    return;
                }
            } else if (name.startsWith(MicroProfileConfigProvider.NS_QUARKUS)) {
                // TODO: 此处会混入部分运行时 Quarkus 属性，但至少来自文件
                quarkus = true;
            } else if (!PropertyMappers.isSpiBuildTimeProperty(name)) {
                return;
            }
            ConfigValue value = Configuration.getNonPersistedConfigValue(name);
            if (value.getValue() == null || value.getConfigSourceName() == null
                    || (quarkus && !value.getConfigSourceName().contains(QuarkusPropertiesConfigSource.NAME))) {
                // 仅持久化来自配置源解析的值，非默认值
                // Profile 可能影响默认值，故单独持久化
                return;
            }
            // 持久化全部 Quarkus 值可能泄露运行时信息，避免保存含环境变量引用的展开表达式
            String stringValue = value.getValue();
            if (quarkus && value.getRawValue() != null) {
                stringValue = value.getRawValue();
            }
            properties.put(name, stringValue);
        });

        // 以下项在优化检查消息中忽略：非用户设置或未正确初始化

        for (File jar : getProviderFiles().values()) {
            properties.put(String.format(KC_PROVIDER_FILE_PREFIX + "%s.last-modified", jar.getName()), String.valueOf(jar.lastModified()));
        }

        if (!Environment.isRebuildCheck()) {
            // 非 auto-build 的常规 build，标记为 optimized 镜像
            Configuration.markAsOptimized(properties);
        }

        String profile = org.keycloak.common.util.Environment.getProfile();
        if (profile != null) {
            properties.put(org.keycloak.common.util.Environment.PROFILE, profile);
            properties.put(LaunchMode.current().getProfileKey(), profile);
        }

        return properties;
    }

    /** 递归为命令树添加 --help 与未匹配参数绑定。 */
    private void updateSpecHelpAndUnmatched(CommandSpec spec, List<String> unrecognizedArgs) {
        try {
            spec.addOption(OptionSpec.builder(Help.OPTION_NAMES)
                    .usageHelp(true)
                    .description("This help message.")
                    .build());
        } catch (DuplicateOptionAnnotationsException e) {
            // Completion 子命令继承了 mixinStandardHelpOptions = true
        }

        spec.addUnmatchedArgsBinding(CommandLine.Model.UnmatchedArgsBinding.forStringArrayConsumer(new ISetter() {
            @Override
            public <T> T set(T value) {
                if (value != null) {
                    unrecognizedArgs.addAll(Arrays.asList((String[]) value));
                }
                return null; // doesn't matter
            }
        }));

        spec.subcommands().values().forEach(c -> updateSpecHelpAndUnmatched(c.getCommandSpec(), unrecognizedArgs));
    }

    /** 创建根 CommandLine：Main 命令、异常处理器、Help 与子命令渲染器。 */
    CommandLine createCommandLine(List<String> unrecognizedArgs) {
        CommandSpec spec = CommandSpec.forAnnotatedObject(new Main(), new IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                K result = CommandLine.defaultFactory().create(cls);
                if (result instanceof AbstractCommand ac) {
                    ac.setPicocli(Picocli.this);
                }
                return result;
            }
        }).name(getCommandNameForHelp());
        updateSpecHelpAndUnmatched(spec, unrecognizedArgs);

        CommandLine cmd = new CommandLine(spec);
        cmd.setExpandAtFiles(false);
        cmd.setPosixClusteredShortOptionsAllowed(false);
        cmd.setExecutionExceptionHandler(this.errorHandler);
        cmd.setParameterExceptionHandler(new ShortErrorMessageHandler());
        cmd.setHelpFactory(new HelpFactory());
        cmd.getHelpSectionMap().put(SECTION_KEY_COMMAND_LIST, new SubCommandListRenderer());
        cmd.setErr(getErrWriter());
        cmd.setOut(getOutWriter());
        configureUsageHelpWidth(cmd);

        removePlatformSpecificCommands(cmd);

        return cmd;
    }

    /**
     * 移除非当前平台的子命令（如 Unix 下隐藏 Windows 服务命令）。
     */
    private void removePlatformSpecificCommands(CommandLine cmd) {
        if (getCommandMode() == CommandMode.UNIX) {
            CommandLine toolsCmd = cmd.getSubcommands().get(Tools.NAME);
            if (toolsCmd != null) {
                CommandLine windowsServiceCmd = toolsCmd.getSubcommands().get(WindowsService.NAME);
                if (windowsServiceCmd != null) {
                    toolsCmd.getCommandSpec().removeSubcommand(WindowsService.NAME);
                }
            }
        }
    }

    /** CLI 帮助中使用的命令模式（ALL/WIN/UNIX）。 */
    enum CommandMode {
        ALL,
        WIN,
        UNIX
    }

    /** 由环境变量 KEYCLOAK_COMMAND_MODE 或 OS 推导命令展示模式。 */
    protected CommandMode getCommandMode() {
        // 非官方选项，供集成测试跨 OS 一致输出
        return Optional.ofNullable(System.getenv("KEYCLOAK_COMMAND_MODE")).map(CommandMode::valueOf)
                .orElse(Environment.isWindows() ? CommandMode.WIN : CommandMode.UNIX);
    }

    /** 帮助 synopsis 中使用的脚本名（ALL 模式统一为 kc.sh）。 */
    private String getCommandNameForHelp() {
        // ALL 模式强制 kc.sh 以保证换行一致
        return switch (getCommandMode()) {
        case WIN -> "kc.bat";
        default -> "kc.sh";
        };
    }

    /** 通过 KEYCLOAK_HELP_WIDTH 环境变量覆盖帮助文本宽度。 */
    private void configureUsageHelpWidth(CommandLine cmd) {
        // 非官方选项，仅用于可配置换行宽度
        Optional.ofNullable(System.getenv("KEYCLOAK_HELP_WIDTH"))
                .map(Integer::parseInt)
                .filter(width -> width > 0)
                .ifPresent(width -> {
                    cmd.setUsageHelpAutoWidth(false);
                    cmd.setUsageHelpWidth(width);
                });
    }

    /** @return 绑定 System.err 的 PrintWriter */
    public PrintWriter getErrWriter() {
        return new PrintWriter(System.err, true);
    }

    /** @return 绑定 System.out 的 PrintWriter */
    public PrintWriter getOutWriter() {
        return new PrintWriter(System.out, true);
    }

    /** 根据命令类型决定校验/帮助中是否包含运行时与构建时选项。 */
    private IncludeOptions getIncludeOptions(AbstractCommand abstractCommand) {
        if (abstractCommand == null) {
            return new IncludeOptions(false, false, false);
        }
        boolean autoBuild = abstractCommand instanceof AbstractAutoBuildCommand;
        boolean includeBuildTime = abstractCommand instanceof Build || (autoBuild && !abstractCommand.isOptimized());
        return new IncludeOptions(autoBuild, includeBuildTime, autoBuild || includeBuildTime || abstractCommand instanceof ShowConfig);
    }

    /** 将 PropertyMapper 定义的选项按类别加入 ArgGroup。 */
    private void addCommandOptions(CommandLine command, AbstractCommand ac) {
        if (!options.includeBuildTime && !options.includeRuntime) {
            return;
        }
        final Map<OptionCategory, List<PropertyMapper<?>>> mappers = new EnumMap<>(OptionCategory.class);

        PropertyMappers.getRuntimeMappers().entrySet().forEach(e -> mappers.put(e.getKey(), new ArrayList<>(e.getValue())));
        PropertyMappers.getBuildTimeMappers().entrySet().forEach(e -> mappers.computeIfAbsent(e.getKey(), category -> new ArrayList<>()).addAll(e.getValue()));

        addMappedOptionsToArgGroups(command, mappers, ac, options);
    }

    /** 静态方法：为命令行规范批量添加映射选项分组。 */
    private static void addMappedOptionsToArgGroups(CommandLine commandLine, Map<OptionCategory, List<PropertyMapper<?>>> propertyMappers, AbstractCommand ac, IncludeOptions options) {
        CommandSpec cSpec = commandLine.getCommandSpec();
        for (Entry<OptionCategory, List<PropertyMapper<?>>> entry : propertyMappers.entrySet()) {
            Set<String> names = new HashSet<String>();
            OptionCategory category = entry.getKey();

            ArgGroupSpec.Builder argGroupBuilder = ArgGroupSpec.builder()
                    .heading(category.getHeading() + ":")
                    .order(category.getOrder())
                    .validate(false);

            for (PropertyMapper<?> mapper : entry.getValue()) {
                if (mapper.getOption().isSynthetic()) {
                    continue;
                }
                String name = mapper.getCliFormat();

                boolean hidden = mapper.isHidden() || ac.isHiddenCategory(mapper.getCategory())
                        || (!options.includeBuildTime && mapper.isBuildTime())
                        || (!options.includeRuntime && mapper.isRunTime());

                if (hidden && ac.isHelpAll()) {
                    continue; // doesn't need defined
                }

                if (cSpec.optionsMap().containsKey(name)) {
                    continue; // 命令级选项优先，跳过重复映射
                }

                if (ac.isHelpAll() && !names.add(name)) {
                    continue; // --help-all 下同 CLI 键可能重复
                }

                OptionSpec.Builder optBuilder = OptionSpec.builder(name)
                        .description(getDecoratedOptionDescription(mapper))
                        .completionCandidates(() -> mapper.getExpectedValues().iterator())
                        .hidden(hidden);

                if (mapper.getParamLabel() != null) {
                    optBuilder.paramLabel(mapper.getParamLabel());
                }

                if (mapper.getDefaultValue().isPresent()) {
                    optBuilder.defaultValue(Option.getDefaultValueString(mapper.getDefaultValue().get()));
                }

                optBuilder.arity("1"); // 与 ConfigArgs 解析一致，所有选项均需值
                if (mapper.getType() != null) {
                    optBuilder.type(mapper.getType());
                    if (mapper.isList()) {
                        // 唯一允许的列表约定：逗号分隔
                        optBuilder.splitRegex(",");
                    } else if (mapper.getType().isEnum()) {
                        // 阻止 Picocli 自动转换，校验阶段再检查枚举
                        optBuilder.type(String.class);
                    }
                } else {
                    optBuilder.type(String.class);
                }

                argGroupBuilder.addArg(optBuilder.build());
            }

            if (argGroupBuilder.args().isEmpty()) {
                continue;
            }

            cSpec.addArgGroup(argGroupBuilder.build());
        }
    }

    /** 为帮助描述追加候选值、默认值、enabledWhen 与弃用标记。 */
    private static String getDecoratedOptionDescription(PropertyMapper<?> mapper) {
        StringBuilder transformedDesc = new StringBuilder(Optional.ofNullable(mapper.getDescription()).orElse(""));

        if (mapper.getType() != Boolean.class && !mapper.getExpectedValues().isEmpty()) {
            List<String> decoratedExpectedValues = mapper.getExpectedValues().stream().map(value -> {
                if (mapper.getDeprecatedMetadata().filter(metadata -> metadata.getDeprecatedValues().contains(value)).isPresent()) {
                    return value + " (deprecated)";
                }
                return value;
            }).toList();

            var isStrictExpectedValues = mapper.getOption().isStrictExpectedValues();
            var isCaseInsensitiveExpectedValues = mapper.getOption().isCaseInsensitiveExpectedValues();
            var printableValues = String.join(", ", decoratedExpectedValues) + (!isStrictExpectedValues ? ", or a custom one" : "");

            transformedDesc.append(String.format(" Possible values are%s: %s.",
                    isCaseInsensitiveExpectedValues ? " (case insensitive)" : "",
                    printableValues)
            );
        }

        mapper.getDefaultValue()
                .map(d -> Option.getDefaultValueString(d).replaceAll("%", "%%")) // 转义格式占位符
                .map(d -> " Default: " + d + ".")
                .ifPresent(transformedDesc::append);

        mapper.getEnabledWhen().map(e -> format(" %s.", e)).ifPresent(transformedDesc::append);
        mapper.getRequiredWhen().map(e -> format(" %s.", e)).ifPresent(transformedDesc::append);

        // 仅整项弃用的选项，非仅弃用某个取值
        mapper.getDeprecatedMetadata()
                .filter(deprecatedMetadata -> deprecatedMetadata.getDeprecatedValues().isEmpty())
                .ifPresent(deprecatedMetadata -> {
            List<String> deprecatedDetails = new ArrayList<>();
            String note = deprecatedMetadata.getNote();
            if (note != null) {
                if (!note.endsWith(".")) {
                    note += ".";
                }
                deprecatedDetails.add(note);
            }
            if (!deprecatedMetadata.getNewOptionsKeys().isEmpty()) {
                String s = deprecatedMetadata.getNewOptionsKeys().size() > 1 ? "s" : "";
                deprecatedDetails.add("Use the following option" + s + " instead: " + String.join(", ", deprecatedMetadata.getNewOptionsKeys()) + ".");
            }

            transformedDesc.insert(0, "@|bold DEPRECATED.|@ ");
            if (!deprecatedDetails.isEmpty()) {
                transformedDesc
                        .append(" @|bold ")
                        .append(String.join(" ", deprecatedDetails))
                        .append("|@");
            }
        });

        return transformedDesc.toString();
    }

    /** 向 stdout 打印一行消息。 */
    public void println(String message) {
        getOutWriter().println(message);
    }

    /** 自动构建前对比持久化选项，输出将被覆盖的构建项警告。 */
    public void checkChangesInBuildOptionsDuringAutoBuild(PrintWriter out) {
        StringBuilder options = new StringBuilder();

        checkChangesInBuildOptions((key, oldValue, newValue) -> optionChanged(options, key, oldValue, newValue));

        if (options.isEmpty()) {
            return;
        }
        out.println(
                colorMode.string(
                        new StringBuilder("@|bold,red ")
                                .append("The previous optimized build will be overridden with the following build options:")
                                .append(options)
                                .append("\nTo avoid that, run the 'build' command again and then start the optimized server instance using the '--optimized' flag.")
                                .append("|@").toString()
                )
        );
    }

    /** 比较当前与持久化构建选项，对每个差异调用 valueChanged。 */
    private static void checkChangesInBuildOptions(TriConsumer<String, String, String> valueChanged) {
        var current = getNonPersistedBuildTimeOptions();
        var persisted = Configuration.getRawPersistedProperties();

        // TODO: 键顺序在此未严格定义

        current.forEach((key, value) -> {
            String persistedValue = persisted.get(key);
            if (!value.equals(persistedValue)) {
                valueChanged.accept((String)key, persistedValue, (String)value);
            }
        });

        persisted.forEach((key, value) -> {
            if (current.get(key) == null) {
                valueChanged.accept(key, value, null);
            }
        });
    }

    /** 格式化单个构建选项从旧值到新值的变更行。 */
    private static void optionChanged(StringBuilder options, String key, String oldValue, String newValue) {
        // 假定构建时选项无需掩码处理
        boolean isIgnored = !key.startsWith(MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX)
                || key.startsWith(KC_PROVIDER_FILE_PREFIX) || isIgnoredPersistedOption(key);
        if (!isIgnored) {
            key = key.substring(3);
            options.append("\n\t- ").append(key).append("=")
                    .append(Optional.ofNullable(oldValue).orElse("<unset>")).append(" > ")
                    .append(key).append("=")
                    .append(Optional.ofNullable(newValue).orElse("<unset>"));
        }
    }

    private static boolean isIgnoredPersistedOption(String key) {
        return key.equals(Configuration.KC_OPTIMIZED) || key.equals(org.keycloak.common.util.Environment.PROFILE)
                || key.equals(LaunchMode.current().getProfileKey());
    }

    /** 启动 Quarkus 服务器（委托 {@link KeycloakMain#start}）。 */
    public void start() {
        KeycloakMain.start(this, (AbstractNonServerCommand) this.getParsedCommand()
                .filter(AbstractNonServerCommand.class::isInstance).orElse(null), this.errorHandler);
    }

    /** 触发 Quarkus 构建（re-augmentation）。 */
    public void build() throws Throwable {
        Environment.setRebuild();
        QuarkusEntryPoint.main();
    }

    /**
     * 在 Profile 确定后初始化 MicroProfile 配置与 PropertyMappers。
     *
     * @param command 当前解析到的命令，可为 null
     */
    public void initConfig(AbstractCommand command) {
        if (Configuration.isInitialized()) {
            throw new IllegalStateException("Config should not be initialized until profile is determined");
        }
        this.parsedCommand = Optional.ofNullable(command);
        options = getIncludeOptions(command);

        Environment.setRebuildCheck(!Environment.isRebuilt() && command instanceof AbstractAutoBuildCommand
                && !command.isOptimized());

        String profile = Optional.ofNullable(org.keycloak.common.util.Environment.getProfile())
                .or(() -> parsedCommand.map(AbstractCommand::getInitProfile)).orElse(Environment.PROD_PROFILE_VALUE);

        Environment.setProfile(profile);
        if (parsedCommand.filter(AbstractCommand::isHelpAll).isEmpty()) {
            parsedCommand.ifPresent(PropertyMappers::sanitizeDisabledMappers);
        }
    }

    /** 若 CLI 中存在重复选项名，输出 WARNING。 */
    public void warnOnDuplicatedOptionsInCli() {
        if (!duplicatedOptionsNames.isEmpty()) {
            warn("Duplicated options present in CLI: %s".formatted(String.join(", ", duplicatedOptionsNames)));
        }
    }

}
