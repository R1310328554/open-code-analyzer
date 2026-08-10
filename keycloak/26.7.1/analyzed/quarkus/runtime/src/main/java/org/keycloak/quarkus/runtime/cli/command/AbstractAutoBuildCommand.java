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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.quarkus.runtime.cli.Picocli;
import org.keycloak.quarkus.runtime.configuration.Configuration;

import picocli.CommandLine;

import static org.keycloak.quarkus.runtime.Environment.isDevMode;
import static org.keycloak.quarkus.runtime.Environment.isDevProfile;
import static org.keycloak.quarkus.runtime.Environment.isRebuildCheck;

/**
 * 支持自动构建（re-augmentation）的服务器启动命令抽象基类。
 * <p>
 * 在配置变更时自动触发 {@link Build}，并协调优化镜像与 Profile 切换。
 */
public abstract class AbstractAutoBuildCommand extends AbstractCommand {

    /** 长选项：使用已优化的构建产物启动。 */
    public static final String OPTIMIZED_BUILD_OPTION_LONG = "--optimized";

    /** 特性被禁用时的退出码。 */
    public static final int FEATURE_DISABLED_EXIT_CODE = 4;
    /** 触发重新构建后的退出码。 */
    public static final int REBUILT_EXIT_CODE = 10;

    @CommandLine.Mixin
    DryRunMixin dryRunMixin = new DryRunMixin();

    @CommandLine.Mixin
    HelpAllMixin helpAllMixin;

    /**
     * 若处于重建检查阶段且需要 re-augmentation，则执行构建并返回 {@link #REBUILT_EXIT_CODE}。
     */
    @Override
    protected Optional<Integer> callCommand() {
        if (isRebuildCheck()) {
            if (requiresReAugmentation()) {
                runReAugmentation();
                return Optional.of(REBUILT_EXIT_CODE);
            }
            // 清除检查标志并切换到命令运行时 Profile
            String profile = org.keycloak.common.util.Environment.getProfile();
            Environment.setRebuildCheck(false);
            String runtimeProfile = getInitProfile();
            if (!Objects.equals(profile, runtimeProfile)) {
                Environment.setProfile(runtimeProfile);
                Configuration.resetConfig();
            }
        }
        return Optional.empty();
    }

    /** 比较持久化构建选项与当前选项，判断是否需要重新构建。 */
    static boolean requiresReAugmentation() {
        Map<String, String> rawPersistedProperties = Configuration.getRawPersistedProperties();
        if (rawPersistedProperties.isEmpty()) {
            return true; // 尚未执行过 build
        }
        var current = Picocli.getNonPersistedBuildTimeOptions();

        // 除 optimized 标志外，其余持久化项须与当前一致
        String key = Configuration.KC_OPTIMIZED;
        Optional.ofNullable(rawPersistedProperties.get(key)).ifPresentOrElse(value -> current.put(key, value), () -> current.remove(key));
        return !rawPersistedProperties.equals(current);
    }

    /** 执行自动 re-augmentation 并提示用户使用 {@code --optimized}。 */
    private void runReAugmentation() {
        if(!isDevMode()) {
            spec.commandLine().getOut().println("Changes detected in configuration. Updating the server image.");
            if (Configuration.isOptimized()) {
                picocli.checkChangesInBuildOptionsDuringAutoBuild(spec.commandLine().getOut());
            }
        }

        directBuild();

        if(!isDevMode()) {
            spec.commandLine().getOut().printf("Next time you run the server, just add %s to the command to ensure this build is used.\n", OPTIMIZED_BUILD_OPTION_LONG);
        }
    }

    /** 委托 {@link Build} 命令执行实际构建逻辑。 */
    void directBuild() {
        Build build = new Build();
        build.dryRunMixin = this.dryRunMixin;
        build.setPicocli(picocli);
        build.spec = spec;
        build.runCommand();
    }

    /** 校验配置、输出开发模式警告并按需启动 Quarkus 服务器。 */
    @Override
    protected void runCommand() {
        if (isServing() && Environment.isRunInContainer() && Environment.getScriptPid().filter(v -> !v.equals("1")).isPresent()) {
            picocli.warn("Keycloak is running inside a container, but is not PID 1. Graceful shutdown may not work. Use 'exec' in your entrypoint script to ensure signals are forwarded correctly. See https://www.keycloak.org/server/containers for more details.");
        }
        doBeforeRun();
        validateConfig();

        if (isDevProfile()) {
            picocli.getOutWriter().println(picocli.getColorMode().string(
                    "@|bold,red Running the server in development mode. DO NOT use this configuration in production.|@"));
        }
        if (shouldStart() && !Boolean.TRUE.equals(dryRunMixin.dryRun)) {
            picocli.start();
        }
    }

    /** 子类可在启动前插入钩子逻辑。 */
    protected void doBeforeRun() {

    }

    @Override
    public boolean isHelpAll() {
        return helpAllMixin != null ? helpAllMixin.allOptions : false;
    }

    /** @return 优化启动 Mixin，由具体 start 类实现 */
    abstract protected OptimizedMixin getOptimizedMixin();

    @Override
    public boolean isOptimized() {
        return Optional.ofNullable(getOptimizedMixin()).map(o -> o.optimized).orElse(false);
    }

    @Override
    public boolean shouldStart() {
        return true;
    }

}
