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
package org.keycloak.headers;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link SecurityHeadersProvider} 的 {@link ProviderFactory} 工厂接口。
 * <p>默认实现提供标准 OWASP 推荐安全头集合。</p>
 */
public interface SecurityHeadersProviderFactory extends ProviderFactory<SecurityHeadersProvider> {

    /** 从 SPI 配置初始化（默认空实现）。 */
    @Override
    default void init(Config.Scope config) {
    }

    /** 会话工厂就绪后回调（默认空实现）。 */
    @Override
    default void postInit(KeycloakSessionFactory factory) {
    }

    /** 关闭工厂资源（默认空实现）。 */
    @Override
    default void close() {
    }

}
