/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.locale;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 默认 {@link LocaleUpdaterProviderFactory} 实现，工厂 ID 为 {@code default}。
 * <p>为每个 {@link KeycloakSession} 创建 {@link DefaultLocaleUpdaterProvider}，用于持久化用户语言偏好与 Cookie。</p>
 */
public class DefaultLocaleUpdaterProviderFactory implements LocaleUpdaterProviderFactory {

    /** 创建绑定当前会话的 {@link DefaultLocaleUpdaterProvider} 实例。 */
    @Override
    public LocaleUpdaterProvider create(KeycloakSession session) {
        return new DefaultLocaleUpdaterProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    /** @return 工厂标识 {@code default} */
    @Override
    public String getId() {
        return "default";
    }

}
