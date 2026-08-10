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
import org.keycloak.models.UserModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.resources.admin.AdminAuth;

/**
 * Admin REST 客户端 CRUD 上下文基类：从 {@link AdminAuth} 暴露已认证客户端、用户与 Bearer 令牌。
 * <p>供 Admin 客户端注册/更新相关 {@link ClientPolicyContext} 实现复用。</p>
 */
abstract class AbstractAdminClientCRUDContext implements ClientCRUDContext {

    /** Admin API 认证信息（用户、客户端、令牌）。 */
    protected final AdminAuth adminAuth;

    /** @param adminAuth Admin REST 认证上下文 */
    public AbstractAdminClientCRUDContext(AdminAuth adminAuth) {
        this.adminAuth = adminAuth;
    }

    /** {@inheritDoc} @return 服务账户或调用方客户端 */
    @Override
    public ClientModel getAuthenticatedClient() {
        return adminAuth.getClient();
    }

    /** {@inheritDoc} @return 已认证 Admin 用户 */
    @Override
    public UserModel getAuthenticatedUser() {
        return adminAuth.getUser();
    }

    /** {@inheritDoc} @return Admin Bearer 访问令牌 */
    @Override
    public JsonWebToken getToken() {
        return adminAuth.getToken();
    }
}