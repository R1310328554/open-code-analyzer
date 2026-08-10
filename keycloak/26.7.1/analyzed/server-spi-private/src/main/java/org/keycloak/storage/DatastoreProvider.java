/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.storage;

import org.keycloak.models.ClientProvider;
import org.keycloak.models.ClientScopeProvider;
import org.keycloak.models.GroupProvider;
import org.keycloak.models.IdentityProviderStorageProvider;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.RevokedTokenProvider;
import org.keycloak.models.RoleProvider;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.UserLoginFailureProvider;
import org.keycloak.models.UserProvider;
import org.keycloak.models.UserSessionProvider;
import org.keycloak.provider.Provider;
import org.keycloak.sessions.AuthenticationSessionProvider;

/**
 * 数据存储提供者：聚合 Realm、用户、客户端、会话等各模型子提供者。
 * <p>作为 Keycloak 持久化层的统一入口，供 {@link org.keycloak.models.KeycloakSession} 使用。</p>
 */
public interface DatastoreProvider extends Provider {
    /** @return 认证会话提供者 */
    AuthenticationSessionProvider authSessions();

    /** @return 客户端作用域提供者 */
    ClientScopeProvider clientScopes();

    /** @return 客户端提供者 */
    ClientProvider clients();

    /** @return 组提供者 */
    GroupProvider groups();

    /** @return 身份提供者存储 */
    IdentityProviderStorageProvider identityProviders();

    /** @return 用户登录失败记录提供者 */
    UserLoginFailureProvider loginFailures();

    /** @return Realm 提供者 */
    RealmProvider realms();

    /** @return 角色提供者 */
    RoleProvider roles();

    /** @return 单次使用对象提供者 */
    SingleUseObjectProvider singleUseObjects();

    /** @return 用户提供者 */
    UserProvider users();

    /** @return 用户会话提供者 */
    UserSessionProvider userSessions();

    /** @return 导入导出管理器 */
    ExportImportManager getExportImportManager();

    /** @return 已撤销令牌提供者 */
    RevokedTokenProvider revokedTokens();
}
