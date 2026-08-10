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

package org.keycloak.quarkus.runtime.cli.command;

import java.util.Optional;
import java.util.concurrent.Callable;

import org.keycloak.config.OptionCategory;
import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.quarkus.runtime.cli.Picocli;
import org.keycloak.quarkus.runtime.configuration.PersistedConfigSource;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import static org.keycloak.quarkus.runtime.Messages.cliExecutionError;

/**
 * Keycloak Quarkus CLI 命令抽象基类，封装 Picocli 执行流程、Profile 初始化与配置校验。
 */
public abstract class AbstractCommand implements Callable<Integer> {

    @Spec
    /** Picocli 命令规格；对 {@code start --optimized} 等场景可能为 null。 */
    protected CommandSpec spec; // will be null for "start --optimized"
    protected Picocli picocli;

    protected void executionError(CommandLine cmd, String message) {
        executionError(cmd, message, null);
    }

    protected void executionError(CommandLine cmd, String message, Throwable cause) {
        cliExecutionError(cmd, message, cause);
    }

    /**
     * Get the effective profile used when the config is initialized
     */
 * 获取配置初始化时生效的 Profile。

    public String getInitProfile() {
        if (Environment.isRebuildCheck()) {
            // 构建检查阶段默认 prod，除非 CLI 显式覆盖 Profile
            return Environment.PROD_PROFILE_VALUE;
        }
        // 否则依次取命令默认 Profile、持久化 Profile，最终回退 prod
        return Optional.ofNullable(this.getDefaultProfile())
                .or(() -> Optional.ofNullable(
                        PersistedConfigSource.getInstance().getValue(org.keycloak.common.util.Environment.PROFILE)))
                .orElse(Environment.PROD_PROFILE_VALUE);
    }

    @Override
    public Integer call() {
        return callCommand().orElseGet(() -> {
            runCommand();
            return CommandLine.ExitCode.OK;
        });
    }

    /**
     * An alternative to {@link #runCommand()} that allows for returning an exit code.
     * If the Optional is empty, {@link #runCommand()} will still be called
     * <br>
     * see {@link #call()}
     */
 * 可返回退出码的命令执行入口；若返回空 {@link Optional} 则回退 {@link #runCommand()}。

    protected Optional<Integer> callCommand() {
        return Optional.empty();
    }

    /**
     * If {@link #callCommand()} returns an empty {@link Optional}, then this method will be used to run the command. OK will be returned as the exit code after successful completion.
     * <br>
     * see {@link #call()}
     */
 * 默认命令体；成功完成后 {@link #call()} 返回 OK 退出码。

    protected void runCommand() {

    }

    /**
     * @param category
     * @return true if runtime options for the given category should be hidden from the cli
     */
 * 判断给定 {@link OptionCategory} 的运行时选项是否应从 CLI 帮助中隐藏。

    public boolean isHiddenCategory(OptionCategory category) {
        return category == OptionCategory.IMPORT || category == OptionCategory.EXPORT;
    }

    protected void validateConfig() {
        picocli.validateConfig();
    }

    public abstract String getName();

    public Optional<CommandLine> getCommandLine() {
        return Optional.ofNullable(spec).map(CommandSpec::commandLine);
    }

    public void setPicocli(Picocli picocli) {
        this.picocli = picocli;
    }

    /**
     * The default profile for the command, or null if the persisted profile should be checked first
     * @return
     */
 * 命令默认 Profile；返回 null 时优先读取持久化 Profile。

    protected String getDefaultProfile() {
        return Environment.PROD_PROFILE_VALUE;
    }

    /**
     * @return true if the command starts an http server
     */
 * 该命令是否会启动 HTTP 服务器。

    public boolean isServing() {
        return false;
    }

    /**
     * @return true if a form of help all was used. Only valid if this is the parsed command.
     */
 * 是否使用了 {@code --help-all}；仅当本命令为当前解析命令时有效。

    public abstract boolean isHelpAll();

    /**
     * @return true if --optimized was used. Only valid if this is the parsed command.
     */
 * 是否使用了 {@code --optimized}；仅当本命令为当前解析命令时有效。

    public boolean isOptimized() {
        return false;
    }

    /**
     * Controls whether the command actually starts the server
     */
 * 控制命令是否实际启动 Keycloak 服务器进程。

    public boolean shouldStart() {
        return false;
    }

}
