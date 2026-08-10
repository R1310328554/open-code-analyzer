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
package org.keycloak.authorization.authzen;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * AuthZen realm 级 REST 资源提供者：将 {@link AuthZenResource} 暴露为 JAX-RS 子资源。
 */
public class AuthZenRealmResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    /**
     * @param session 当前 Keycloak 会话
     */
    public AuthZenRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    /** 返回 AuthZen REST 资源实例。 */
    @Override
    public Object getResource() {
        return new AuthZenResource(session);
    }

    /** AuthZen 提供者无额外资源需释放。 */
    @Override
    public void close() {
    }
}
