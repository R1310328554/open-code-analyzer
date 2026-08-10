package org.keycloak.quarkus.runtime.cli;

import java.io.PrintWriter;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import org.keycloak.quarkus.runtime.cli.command.AbstractCommand;
import org.keycloak.quarkus.runtime.configuration.KcUnmatchedArgumentException;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMappers;

import picocli.CommandLine;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.UnmatchedArgumentException;

import static java.lang.String.format;

/**
 * Picocli 参数解析异常处理器：输出简短、可操作的 CLI 错误信息。
 */
public class ShortErrorMessageHandler implements IParameterExceptionHandler {

    /**
     * 处理未知选项、禁用选项、缺少参数等解析错误。
     *
     * @param ex 参数异常
     * @param args 原始命令行参数
     * @return 无效输入对应的退出码
     */
    @Override
    public int handleParseException(ParameterException ex, String[] args) {
        CommandLine cmd = ex.getCommandLine();
        PrintWriter writer = cmd.getErr();
        String errorMessage = ex.getMessage();
        String additionalSuggestion = null;

        if (ex instanceof UnmatchedArgumentException uae) {
            String[] unmatched = getUnmatchedPartsByOptionSeparator(uae, "=");

            String cliKey = unmatched[0];

            PropertyMapper<?> mapper = PropertyMappers.getMapperByCliKey(cliKey);

            Optional<PropertyMapper<?>> disabled = PropertyMappers.getKcKeyFromCliKey(cliKey).flatMap(PropertyMappers::getDisabledMapper);

            final BooleanSupplier isUnknownOption = () -> mapper == null || !(cmd.getCommand() instanceof AbstractCommand);

            if (mapper == null && disabled.filter(m -> !m.getOption().isHidden()).isPresent()) {
                var enabledWhen = disabled
                        .flatMap(PropertyMapper::getEnabledWhen)
                        .map(desc -> format(". %s", desc))
                        .orElse("");

                errorMessage = format("Disabled option: '%s'%s", cliKey, enabledWhen);
                additionalSuggestion = "Specify '--help-all' to obtain information on all options and their availability.";
            } else if (isUnknownOption.getAsBoolean()) {
                if (cliKey.split("\\s").length > 1) {
                    errorMessage = "Option: '" + cliKey + "' is not expected to contain whitespace, please remove any unnecessary quoting/escaping";
                } else {
                    errorMessage = "Unknown option: '" + cliKey + "'";
                }
            } else {
                final var optionType = mapper.isRunTime() ? "Run time" : "Build time";
                errorMessage = format("%s option: '%s' not usable with %s", optionType, cliKey, cmd.getCommandName());
            }
        } else if (ex instanceof MissingParameterException mpe) {
            if (mpe.getMissing().size() == 1) {
                ArgSpec spec = mpe.getMissing().get(0);
                if (spec instanceof OptionSpec option) {
                    errorMessage = getExpectedMessage(option);
                }
            }
        }

        writer.println(cmd.getColorScheme().errorText(errorMessage));
        if (!(ex instanceof KcUnmatchedArgumentException) && ex instanceof UnmatchedArgumentException) {
            ex = new KcUnmatchedArgumentException((UnmatchedArgumentException) ex);
        }
        UnmatchedArgumentException.printSuggestions(ex, writer);

        CommandSpec spec = cmd.getCommandSpec();
        writer.printf("Try '%s --help' for more information on the available options.%n", spec.qualifiedName());

        if (additionalSuggestion != null) {
            writer.println(additionalSuggestion);
        }

        return getInvalidInputExitCode(ex, cmd);
    }

    /** 根据 ExitCodeExceptionMapper 或命令规范返回无效输入退出码。 */
    static int getInvalidInputExitCode(Throwable ex, CommandLine cmd) {
        return cmd.getExitCodeExceptionMapper() != null
                ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                : cmd.getCommandSpec().exitCodeOnInvalidInput();
    }

    /** 按分隔符拆分首个未匹配参数。 */
    private String[] getUnmatchedPartsByOptionSeparator(UnmatchedArgumentException uae, String separator) {
        return uae.getUnmatched().get(0).split(separator);
    }

    /** 构造缺少参数时的期望值说明。 */
    private String getExpectedMessage(OptionSpec option) {
        return String.format("Option '%s' (%s) expects %s.%s", String.join(", ", option.names()), option.paramLabel(),
                option.typeInfo().isMultiValue() ? "one or more comma separated values without whitespace": "a single value",
                getExpectedValuesMessage(option.completionCandidates(), isCaseInsensitive(option)));
    }

    /** 判断选项是否配置为大小写不敏感枚举。 */
    private boolean isCaseInsensitive(OptionSpec option) {
        if (option.longestName().startsWith("--")) {
            var mapper = PropertyMappers.getMapper(option.longestName().substring(2));
            if (mapper != null) {
                return mapper.getOption().isCaseInsensitiveExpectedValues();
            }
        }
        return false;
    }

    /** 格式化候选值列表后缀。 */
    public static String getExpectedValuesMessage(Iterable<String> specCandidates, boolean caseInsensitive) {
        if (specCandidates == null || !specCandidates.iterator().hasNext()) {
            return "";
        }
        return String.format(" Expected values are%s: %s", caseInsensitive ? " (case insensitive)" : "",
                String.join(", ", specCandidates));
    }

}
