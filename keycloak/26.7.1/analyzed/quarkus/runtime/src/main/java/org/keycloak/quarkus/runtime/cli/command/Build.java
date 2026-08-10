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

import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.quarkus.runtime.Messages;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.quarkus.runtime.configuration.IgnoredArtifacts;
import org.keycloak.quarkus.runtime.configuration.PersistedConfigSource;

import io.quarkus.bootstrap.runner.RunnerClassLoader;
import io.quarkus.runtime.LaunchMode;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import static org.keycloak.config.DatabaseOptions.DB;
import static org.keycloak.quarkus.runtime.Environment.getHomePath;
import static org.keycloak.quarkus.runtime.Environment.isDevProfile;

/**
 * {@code build} 命令：根据当前配置生成并持久化优化后的 Keycloak 服务器镜像。
 */
@Command(name = Build.NAME,
        header = "Creates a new and optimized server image.",
        description = {
            "%nCreates a new and optimized server image based on the configuration options passed to this command. Once created, the configuration will be persisted and read during startup without having to pass them over again.",
            "",
            "Consider running this command before running the server in production for an optimal runtime."
        },
        footerHeading = "Examples:",
        footer = "  Change the database vendor:%n%n"
                + "      $ ${PARENT-COMMAND-FULL-NAME:-$PARENTCOMMAND} ${COMMAND-NAME} --db=postgres%n%n"
                + "  Enable a feature:%n%n"
                + "      $ ${PARENT-COMMAND-FULL-NAME:-$PARENTCOMMAND} ${COMMAND-NAME} --features=<feature_name>%n%n"
                + "  Or alternatively, enable all tech preview features:%n%n"
                + "      $ ${PARENT-COMMAND-FULL-NAME:-$PARENTCOMMAND} ${COMMAND-NAME} --features=preview%n%n"
                + "  Enable health endpoints:%n%n"
                + "      $ ${PARENT-COMMAND-FULL-NAME:-$PARENTCOMMAND} ${COMMAND-NAME} --health-enabled=true%n%n"
                + "  Enable metrics endpoints:%n%n"
                + "      $ ${PARENT-COMMAND-FULL-NAME:-$PARENTCOMMAND} ${COMMAND-NAME} --metrics-enabled=true%n%n"
                + "  Change the relative path:%n%n"
                + "      $ ${PARENT-COMMAND-FULL-NAME:-$PARENTCOMMAND} ${COMMAND-NAME} --http-relative-path=/auth%n")
public final class Build extends AbstractCommand {

    public static final String NAME = "build";

    /** Quarkus 构建时需从类路径移除的 artifact 列表（系统属性键）。 */
    public static final String QUARKUS_REMOVED_ARTIFACTS_PROPERTY = "quarkus.class-loading.removed-artifacts";

    @CommandLine.Mixin
    HelpAllMixin helpAllMixin;

    @CommandLine.Mixin
    DryRunMixin dryRunMixin;

    @Override
    protected void runCommand() {
        checkProfileAndDb();

        // 在标记重建前校验配置，使运行时选项仍可见；校验与 artifact 移除须在禁用持久化配置源时进行
        PersistedConfigSource.getInstance().runWithDisabled(() -> {
            validateConfig();
            System.setProperty(QUARKUS_REMOVED_ARTIFACTS_PROPERTY, String.join(",", IgnoredArtifacts.getDefaultIgnoredArtifacts()));
            return null;
        });
        picocli.println("Updating the configuration and installing your custom providers, if any. Please wait.");

        try {
            beforeReaugmentationOnWindows();
            if (!Boolean.TRUE.equals(dryRunMixin.dryRun)) {
                picocli.build();
            } else if (DryRunMixin.isDryRunBuild()) {
                PersistedConfigSource.getInstance().saveDryRunProperties();
            }

            if (!isDevProfile()) {
                picocli.println("Server configuration updated and persisted. Run the following command to review the configuration:\n");
                picocli.println("\t" + Environment.getCommand() + " show-config\n");
            }
        } catch (Throwable throwable) {
            executionError(spec.commandLine(), "Failed to update server configuration.", throwable);
        } finally {
            cleanTempResources();
        }
    }

    private void checkProfileAndDb() {
        if (Environment.isDevProfile()) {
            String cmd = picocli.getParsedCommand().map(AbstractCommand::getName).orElse(getName());
            // 允许 start-dev 及 import|export|bootstrap-admin --profile=dev，禁止 start/build 使用 dev Profile
            if (Start.NAME.equals(cmd) || Build.NAME.equals(cmd)) {
                executionError(spec.commandLine(), Messages.devProfileNotAllowedError(cmd));
            }
        } else if (Configuration.isDefault(Configuration.getConfigValue(DB))) {
            picocli.warn("Usage of the default value for the db option in the production profile is deprecated. Please explicitly set the db instead.");
        }
    }

    private void beforeReaugmentationOnWindows() {
        // Windows 上 re-augmentation 生成的文件会被锁定无法重建；重置 RunnerClassLoader 内部缓存以规避（KEYCLOAK-16218）
        if (Environment.isWindows()) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            if (classLoader instanceof RunnerClassLoader) {
                ((RunnerClassLoader) classLoader).resetInternalCaches();
            }
        }
    }

    private void cleanTempResources() {
        if (!LaunchMode.current().isDevOrTest()) {
            // 仅开发/测试环境需清理临时 quarkus-artifact.properties
            getHomePath().ifPresent(path -> path.resolve("quarkus-artifact.properties").toFile().delete());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isHelpAll() {
        return helpAllMixin != null ? helpAllMixin.allOptions : false;
    }

}
