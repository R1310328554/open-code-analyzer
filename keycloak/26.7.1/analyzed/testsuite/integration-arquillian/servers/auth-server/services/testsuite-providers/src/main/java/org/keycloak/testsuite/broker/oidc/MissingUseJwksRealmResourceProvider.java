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

package org.keycloak.testsuite.broker.oidc;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * 提供缺少 {@code use} 字段 JWKS 端点的 Realm 资源提供者，用于 broker OIDC 测试。
 */
public class MissingUseJwksRealmResourceProvider implements RealmResourceProvider {

    /** 当前 Keycloak 会话。 */
    private KeycloakSession session;

    /**
     * @param session Keycloak 会话
     */
    public MissingUseJwksRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    /** {@inheritDoc} 返回 {@link MissingUseJwksRestResource} REST 资源实例。 */
    @Override
    public Object getResource() {
        return new MissingUseJwksRestResource(session);
    }

    @Override
    public void close() {
    }

}
