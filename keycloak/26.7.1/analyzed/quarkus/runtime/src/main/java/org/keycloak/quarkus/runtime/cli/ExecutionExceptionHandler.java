/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime;

import java.io.PrintWriter;
import java.nio.file.FileSystemException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.quarkus.runtime.Messages;

import io.quarkus.bootstrap.logging.InitialConfigurator;
import io.smallrye.config.ConfigValue;
import org.jboss.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

import static org.keycloak.quarkus.runtime.configuration.Configuration.getConfig;

/**
 * Picocli 命令执行异常处理器：格式化 CLI 错误输出并支持异常转换链。
 * 实现 {@link CommandLine.IExecutionExceptionHandler}。
 */
public final class ExecutionExceptionHandler implements CommandLine.IExecutionExceptionHandler {

    private static Logger logger;
    /** 是否输出完整堆栈跟踪。 */
    private boolean verbose;
    /** 按堆栈类名注册的异常转换器。 */
    private static Map<String, Function<Throwable, Throwable>> exceptionTransformers = new HashMap<>();

    public ExecutionExceptionHandler() {}

    /**
     * 处理命令执行期间的异常。
     *
     * @param cause 捕获的异常
     * @param cmd 命令行实例
     * @param parseResult 解析结果
     * @return 退出码
     */
    @Override
    public int handleExecutionException(Exception cause, CommandLine cmd, ParseResult parseResult) {
        var exception = handleExceptionTransformers(cause);
        if (exception instanceof PropertyException) {
            PrintWriter writer = cmd.getErr();
            writer.println(cmd.getColorScheme().errorText(exception.getMessage()));
            if (verbose && exception.getCause() != null) {
                dumpException(writer, exception.getCause());
            }
            return ShortErrorMessageHandler.getInvalidInputExitCode(exception, cmd);
        }
        error(cmd.getErr(), "Failed to run '" + parseResult.subcommands().stream()
                .map(ParseResult::commandSpec)
                .map(CommandLine.Model.CommandSpec::name)
                .findFirst()
                .orElse(Environment.getCommand()) + "' command.", exception);
        return cmd.getCommandSpec().exitCodeOnExecutionException();
    }

    /**
     * 向 stderr 输出错误消息及可选的异常链。
     *
     * @param errorWriter 错误输出流
     * @param message 顶层错误描述
     * @param cause 根本原因
     */
    public void error(PrintWriter errorWriter, String message, Throwable cause) {
        var exception = handleExceptionTransformers(cause);
        if (message != null) {
            logError(errorWriter, "ERROR: " + message);
        }

        if (exception != null) {
            dumpException(errorWriter, exception);

            if (!verbose) {
                logError(errorWriter, "For more details run the same command passing the '--verbose' option. Also you can use '--help' to see the details about the usage of the particular command.");
            }
        }
    }

    /** 按 verbose 模式输出异常详情或仅输出消息链。 */
    private void dumpException(PrintWriter errorWriter, Throwable cause) {
        if (verbose) {
            logError(errorWriter, cause == null ? "Unknown error." : "Error details:", cause);
        } else {
            do {
                if (cause.getMessage() != null) {
                    logError(errorWriter, String.format("ERROR: %s", cause.getMessage()));
                }
                printErrorHints(errorWriter, cause);
            } while ((cause = cause.getCause()) != null);
        }

        printErrorHints(errorWriter, cause);
    }

    /** 针对证书文件缺失等场景输出额外提示。 */
    private void printErrorHints(PrintWriter errorWriter, Throwable cause) {
        if (cause instanceof FileSystemException) {
            FileSystemException fse = (FileSystemException) cause;
            ConfigValue httpsCertFile = getConfig().getConfigValue("kc.https-certificate-file");

            if (fse.getFile().equals(Optional.ofNullable(httpsCertFile.getValue()).orElse(null))) {
                logError(errorWriter, Messages.httpsConfigurationNotSet());
            }
        }
    }

    private void logError(PrintWriter errorWriter, String errorMessage) {
        logError(errorWriter, errorMessage, null);
    }

    // cause 可为 null
    /** 优先委托 JBoss Logger，否则直接写入 PrintWriter。 */
    private void logError(PrintWriter errorWriter, String errorMessage, Throwable cause) {
        if (InitialConfigurator.DELAYED_HANDLER.isActivated()) {
            // 延迟处理器激活后可使用正式 Logger
            if (cause == null) {
                getLogger().error(errorMessage);
            } else {
                getLogger().error(errorMessage, cause);
            }
        } else {
            if (cause == null) {
                errorWriter.println(errorMessage);
            } else {
                errorWriter.println(errorMessage);
                cause.printStackTrace(errorWriter);
            }
        }
    }

    private static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(ExecutionExceptionHandler.class);
        }
        return logger;
    }

    /** 设置是否输出详细堆栈。 */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * 注册按堆栈帧类名触发的异常转换器。
     *
     * @param fromClass 触发转换的类
     * @param transformer 转换函数
     */
    public static void addExceptionTransformer(Class<?> fromClass, Function<Throwable, Throwable> transformer) {
        if (exceptionTransformers.get(fromClass.getName()) != null) {
            getLogger().warnf("Transformer for the '%s' class is overridden", fromClass.getName());
        }
        exceptionTransformers.put(fromClass.getName(), transformer);
    }

    /** 清空所有异常转换器（测试或重置配置时调用）。 */
    public static void resetExceptionTransformers() {
        exceptionTransformers = new HashMap<>();
    }

    /** 沿堆栈查找并应用首个匹配的转换器。 */
    private static Throwable handleExceptionTransformers(Throwable exception) {
        if (exception == null) {
            return null;
        }

        if (exceptionTransformers.isEmpty()) {
            return exception;
        }

        var stackTrace = exception.getStackTrace();
        for (var trace : stackTrace) {
            var transformer = exceptionTransformers.get(trace.getClassName());
            if (transformer != null) {
                return transformer.apply(exception);
            }
        }
        return exception;
    }
}
