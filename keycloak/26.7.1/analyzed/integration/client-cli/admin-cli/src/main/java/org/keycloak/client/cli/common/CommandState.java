/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.client.cli.common;

/**
 * CLI 命令上下文状态接口。
 * <p>
 * 由 kcadm、kcreg 等不同客户端 CLI 实现，供 {@link BaseAuthOptionsCmd} 等基类
 * 获取命令名、默认配置文件路径及令牌作用域策略。
 */
public interface CommandState {

    /** 当前 CLI 命令名称（如 {@code kcadm}）。 */
    String getCommand();

    /** 默认配置文件路径。 */
    String getDefaultConfigFilePath();

    /** 令牌是否全局共享（而非按 realm/client 隔离）。 */
    boolean isTokenGlobal();

}
