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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.quarkus.runtime.cli.command.AbstractAutoBuildCommand;
import org.keycloak.quarkus.runtime.cli.command.Build;

import org.jboss.logging.Logger;
import picocli.CommandLine;

/**
 * Keycloak Quarkus CLI 常用错误与提示消息的工厂方法。
 */
public final class Messages {

    private Messages() {

    }

    /** @return HTTPS 未配置密钥材料时的提示信息 */
    public static String httpsConfigurationNotSet() {
        StringBuilder builder = new StringBuilder("Key material not provided to setup HTTPS. Please configure your keys/certificates, or if HTTPS access is not needed see the `http-enabled` option.");
        if (!org.keycloak.common.util.Environment.DEV_PROFILE_VALUE.equals(org.keycloak.common.util.Environment.getProfile())) {
            builder.append(" If you meant to start the server in development mode, see the `start-dev` command.");
        }
        return builder.toString();
    }

    /**
     * 包装并抛出 Picocli 命令执行异常。
     *
     * @param cmd 当前命令行
     * @param message 错误消息
     * @param cause 根本原因
     */
    public static void cliExecutionError(CommandLine cmd, String message, Throwable cause) {
        throw new CommandLine.ExecutionException(cmd, message, cause);
    }

    /** @return 在开发 Profile 下不允许执行指定命令时的错误文本 */
    public static String devProfileNotAllowedError(String cmd) {
        return String.format("You can not '%s' the server in %s mode. Please re-build the server first, using '%s build' for the default production mode.%n", cmd, Environment.getKeycloakModeFromProfile(org.keycloak.common.util.Environment.DEV_PROFILE_VALUE), Environment.getCommand());
    }

    /** @return 首次启动误用 {@code --optimized} 时的警告信息 */
    public static String optimizedUsedForFirstStartup() {
        return String.format("The '%s' flag was used for first ever server start. Please don't use this flag for the first startup or use '%s %s' to build the server first.", AbstractAutoBuildCommand.OPTIMIZED_BUILD_OPTION_LONG, Environment.getCommand(), Build.NAME);
    }

    /** @return 无效日志级别及合法取值列表的说明 */
    public static String invalidLogLevel(String logLevel) {
        Set<String> values = Arrays.stream(Logger.Level.values()).map(Logger.Level::name).map(String::toLowerCase).collect(Collectors.toSet());
        return "Invalid log level: " + logLevel + ". Possible values are: " + String.join(", ", values) + ".";
    }

    /** @return 日志类别格式错误说明（应为 category:level） */
    public static String invalidLogCategoryFormat(String category) {
        return "Invalid log category format: " + category + ". The format is 'category:level' such as 'org.keycloak:debug'.";
    }

}
