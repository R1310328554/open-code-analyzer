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

package org.keycloak.services.resources.admin;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.AccessToken;

/**
 * 管理 REST API 认证上下文。
 * <p>封装 Bearer 令牌解析后的领域、用户、客户端及角色检查逻辑。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AdminAuth {

    /** 当前领域 */
    private final RealmModel realm;
    /** Bearer 访问令牌 */
    private final AccessToken token;
    /** 已认证管理员用户 */
    private final UserModel user;
    /** 发起请求的客户端（admin-cli 等） */
    private final ClientModel client;

    /** 构造管理 API 认证上下文。 */
    public AdminAuth(RealmModel realm, AccessToken token, UserModel user, ClientModel client) {
        this.token = token;
        this.realm = realm;

        this.user = user;
        this.client = client;
    }

    /** @return 当前领域 */
    public RealmModel getRealm() {
        return realm;
    }

    /** @return 已认证用户 */
    public UserModel getUser() {
        return user;
    }

    /** @return 请求客户端 */
    public ClientModel getClient() {
        return client;
    }

    /** @return Bearer 访问令牌 */
    public AccessToken getToken() {
        return token;
    }


    /** 检查用户是否具备指定领域角色（含 client scope 校验）。 */
    public boolean hasRealmRole(String role) {
        if (client instanceof ClientModel) {
            RoleModel roleModel = realm.getRole(role);
            if (roleModel == null) return false;
            return user.hasRole(roleModel) && client.hasScope(roleModel);
        } else {
            AccessToken.Access access = token.getRealmAccess();
            return access != null && access.isUserInRole(role);
        }
    }

    /** 检查用户是否具备任一指定领域角色。 */
    public boolean hasOneOfRealmRole(String... roles) {
        for (String r : roles) {
            if (hasRealmRole(r)) {
                return true;
            }
        }
        return false;
    }

    /** 检查用户是否具备指定客户端应用角色。 */
    public boolean hasAppRole(ClientModel app, String role) {
        if (client instanceof ClientModel) {
            RoleModel roleModel = app.getRole(role);
            if (roleModel == null) return false;
            return user.hasRole(roleModel) && client.hasScope(roleModel);
        } else {
            AccessToken.Access access = token.getResourceAccess(app.getClientId());
            return access != null && access.isUserInRole(role);
        }
    }

    /** 检查用户是否具备指定客户端的任一应用角色。 */
    public boolean hasOneOfAppRole(ClientModel app, String... roles) {
        for (String r : roles) {
            if (hasAppRole(app, r)) {
                return true;
            }
        }
        return false;
    }

    /** 管理 API 资源类型枚举，用于权限评估。 */
    public enum Resource {
        CLIENT, USER, REALM, EVENTS, IDENTITY_PROVIDER, IMPERSONATION, AUTHORIZATION
    }
}
