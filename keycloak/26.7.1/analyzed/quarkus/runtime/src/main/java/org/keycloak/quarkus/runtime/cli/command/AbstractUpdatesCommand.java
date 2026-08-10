/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import org.keycloak.Config;
import org.keycloak.compatibility.CompatibilityMetadataProvider;
import org.keycloak.quarkus.runtime.cli.PropertyException;
import org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProviderFactory;

import picocli.CommandLine;

/**
 * 升级/兼容性相关 CLI 命令抽象基类，加载配置后执行 {@link #executeAction()} 并返回退出码。
 */
public abstract class AbstractUpdatesCommand extends AbstractAutoBuildCommand {

    /** {@code --optimized} 选项混入。 */
    @CommandLine.Mixin
    OptimizedMixin optimizedMixin = new OptimizedMixin();

    @Override
    public boolean shouldStart() {
        return false;
    }

    @Override
    protected Optional<Integer> callCommand() {
        return super.callCommand().or(() -> {
            loadConfiguration();
            validateConfig();
            return Optional.of(executeAction());
        });
    }

    abstract int executeAction();

    static void validateFileIsNotDirectory(File file, String option) {
        if (file.isDirectory()) {
            throw new PropertyException("Incorrect argument %s. Path '%s' is not a valid file.".formatted(option, file.getAbsolutePath()));
        }
    }

    static Map<String, CompatibilityMetadataProvider> loadAllProviders() {
        Map<String, CompatibilityMetadataProvider> providers = new HashMap<>();
        for (var p : ServiceLoader.load(CompatibilityMetadataProvider.class)) {
            providers.merge(p.getId(), p, (existing, current) -> {
                if (existing.priority() == current.priority()) {
                    throw new IllegalArgumentException("Unable to handle two providers with the same id (%s) and priority.".formatted(existing.getId()));
                }
                // 允许用户以更高优先级 Provider 替换默认实现。
                return existing.priority() < current.priority() ?
                        current :
                        existing;
            });
        }
        return providers;
    }

    private static void loadConfiguration() {
        // 间接初始化 Config，避免命令创建阶段直接引用 MicroProfileConfigProvider 引发类加载问题（如 provider JAR 被删除）
        Config.init(new MicroProfileConfigProviderFactory().create());
    }

    @Override
    protected OptimizedMixin getOptimizedMixin() {
        return optimizedMixin;
    }

}
