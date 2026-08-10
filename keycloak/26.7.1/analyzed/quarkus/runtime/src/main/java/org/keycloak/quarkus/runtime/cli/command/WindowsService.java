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

import picocli.CommandLine.Command;

/**
 * {@code tools windows-service} 命令组：在 Windows 上将 Keycloak 注册为系统服务。
 * <p>
 * 基于 Apache Commons Daemon（Procrun）实现服务的安装与卸载。
 */
@Command(name = WindowsService.NAME,
        description = "Manage Keycloak as a Windows service.",
        subcommands = {WindowsServiceInstall.class, WindowsServiceUninstall.class})
public class WindowsService {

    /** 子命令组名称。 */
    public static final String NAME = "windows-service";

}
