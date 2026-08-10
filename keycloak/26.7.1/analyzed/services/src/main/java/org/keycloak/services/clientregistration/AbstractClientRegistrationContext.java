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

package org.keycloak.services.clientregistration;

import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientRepresentation;

/**
 * 客户端注册上下文的抽象基类，封装会话、客户端表示与注册提供者。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractClientRegistrationContext implements ClientRegistrationContext {

    /** Keycloak 会话 */
    protected final KeycloakSession session;
    /** 待注册/更新的客户端表示 */
    protected final ClientRepresentation client;
    /** 当前注册提供者实例 */
    protected final ClientRegistrationProvider provider;

    /**
     * @param session Keycloak 会话
     * @param client 客户端表示
     * @param provider 注册提供者
     */
    public AbstractClientRegistrationContext(KeycloakSession session, ClientRepresentation client, ClientRegistrationProvider provider) {
        this.session = session;
        this.client = client;
        this.provider = provider;
    }

    /** {@inheritDoc} 返回客户端表示 */
    @Override
    public ClientRepresentation getClient() {
        return client;
    }

    /** {@inheritDoc} 返回 Keycloak 会话 */
    @Override
    public KeycloakSession getSession() {
        return session;
    }

    /** {@inheritDoc} 返回注册提供者 */
    @Override
    public ClientRegistrationProvider getProvider() {
        return provider;
    }

}
