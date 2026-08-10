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

import org.keycloak.provider.KeycloakDeploymentInfo;
import org.keycloak.provider.ProviderManager;

/**
 * Quarkus 运行时 Provider 发现工具：从类路径加载 SPI 与主题资源。
 */
public final class Providers {

    /**
     * 创建基于类路径的 {@link ProviderManager}，用于扫描服务与主题资源 Provider。
     *
     * @param classLoader 用于加载 Provider 实现的类加载器
     * @return 配置为 classpath 部署的 Provider 管理器
     */
    public static ProviderManager getProviderManager(ClassLoader classLoader) {
        KeycloakDeploymentInfo keycloakDeploymentInfo = KeycloakDeploymentInfo.create()
                .name("classpath")
                .services()
                .themeResources();

        return new ProviderManager(keycloakDeploymentInfo, classLoader);
    }
}
