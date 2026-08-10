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

package org.keycloak.keys;

import org.keycloak.Config;
import org.keycloak.component.ComponentFactory;
import org.keycloak.component.ComponentModel;
import org.keycloak.crypto.KeyUse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * {@link KeyProvider} 的 {@link org.keycloak.component.ComponentFactory} 工厂接口。
 * <p>按 realm 组件配置实例化具体密钥后端（Java Keystore、HSM 等）。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface KeyProviderFactory<T extends KeyProvider> extends ComponentFactory<T, KeyProvider> {

    /**
     * 根据组件模型创建密钥提供者实例。
     *
     * @param session Keycloak 会话
     * @param model 组件配置模型
     * @return 密钥提供者
     */
    T create(KeycloakSession session, ComponentModel model);

    /**
     * 是否在缺少可用密钥时自动创建回退密钥。
     *
     * @param session Keycloak 会话
     * @param keyUse 密钥用途（签名/加密）
     * @param algorithm 算法名称
     * @return 是否创建回退密钥
     */
    default boolean createFallbackKeys(KeycloakSession session, KeyUse keyUse, String algorithm) {
        return false;
    }

    @Override
    default void init(Config.Scope config) {
    }

    @Override
    default void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    default void close() {
    }

}
