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

package org.keycloak.quarkus.runtime.cli.command;

import picocli.CommandLine;

import static org.keycloak.quarkus.runtime.cli.Picocli.NO_PARAM_LABEL;
import static org.keycloak.quarkus.runtime.cli.command.AbstractAutoBuildCommand.OPTIMIZED_BUILD_OPTION_LONG;

/**
 * Picocli Mixin：提供 {@code --optimized} 选项，用于基于已构建镜像快速启动服务器。
 * <p>
 * 若此前已通过 {@link Build} 命令生成优化镜像，启用此选项可跳过启动前的自动构建，缩短启动时间。
 */
public final class OptimizedMixin {

    /** 是否使用已优化的构建产物启动（对应 {@link AbstractAutoBuildCommand#OPTIMIZED_BUILD_OPTION_LONG}）。 */
    @CommandLine.Option(names = {OPTIMIZED_BUILD_OPTION_LONG},
            description = "Use this option to achieve an optimal startup time if you have previously built a server image using the 'build' command.",
            paramLabel = NO_PARAM_LABEL,
            order = 1)
    boolean optimized;

}
