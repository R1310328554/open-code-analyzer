/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.urls;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

/** {@link HostnameProvider} 的工厂 SPI。 */
public interface HostnameProviderFactory extends ProviderFactory<HostnameProvider> {

    @Override
    /** 关闭工厂（默认空实现）。 */
    default void close() {
    }

    @Override
    /** 启动时初始化（默认空实现）。
     * @param config 配置作用域 */
    default void init(Config.Scope config) {
    }

    @Override
    /** 所有工厂初始化完成后回调（默认空实现）。
     * @param factory 会话工厂 */
    default void postInit(KeycloakSessionFactory factory) {
    }

}
