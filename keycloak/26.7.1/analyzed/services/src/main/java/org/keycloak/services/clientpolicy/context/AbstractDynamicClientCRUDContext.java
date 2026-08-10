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

package org.keycloak.services.clientpolicy.context;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.JsonWebToken;

/**
 * 动态客户端注册 CRUD 上下文基类：从注册/更新 JWT 解析已认证客户端与用户。
 * <p>根据 {@code azp}（issuedFor）与 {@code sub} 在 Realm 中查找 {@link ClientModel} 与 {@link UserModel}。</p>
 */
abstract class AbstractDynamicClientCRUDContext implements ClientCRUDContext {

    /** 动态注册/更新请求携带的 JWT（初始或注册访问令牌等）。 */
    private final JsonWebToken token;
    /** 从令牌 azp 解析的客户端；可能为 null。 */
    private ClientModel authenticatedClient;
    /** 从令牌 sub 解析的用户；可能为 null。 */
    private UserModel authenticatedUser;

    /**
     * 解析令牌中的客户端与用户身份。
     * @param session Keycloak 会话
     * @param token 注册/更新 JWT
     * @param realm 目标 Realm
     */
        this.token = token;
        if (token == null) {
            return;
        }
        if (token.getIssuedFor() != null) {
            this.authenticatedClient = realm.getClientByClientId(token.getIssuedFor());
        }
        if (token.getSubject() != null) {
            this.authenticatedUser = session.users().getUserById(realm, token.getSubject());
        }
    }

    /** {@inheritDoc} @return 令牌 azp 对应的客户端 */
    @Override
    public ClientModel getAuthenticatedClient() {
        return authenticatedClient;
    }

    /** {@inheritDoc} @return 令牌 sub 对应的用户 */
    @Override
    public UserModel getAuthenticatedUser() {
        return authenticatedUser;
    }

    /** {@inheritDoc} @return 原始 JWT */
    @Override
    public JsonWebToken getToken() {
        return token;
    }
}
