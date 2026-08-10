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
package org.keycloak.authorization.identity;

import java.util.Map;

import org.keycloak.authorization.attribute.Attributes;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

/**
 * 基于 {@link UserModel} 的 {@link Identity} 实现，用于授权评估中的主体身份。
 * <p>封装领域用户及其角色检查逻辑。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserModelIdentity implements Identity {
    protected RealmModel realm;
    protected UserModel user;

    /** 构造用户身份，绑定领域与用户模型。 */
    public UserModelIdentity(RealmModel realm, UserModel user) {
        this.realm = realm;
        this.user = user;
    }

    /** 返回用户 ID。 */
    @Override
    public String getId() {
        return user.getId();
    }

    /** 返回用户属性集合。 */
    @Override
    public Attributes getAttributes() {
        Map attr = user.getAttributes();
        return Attributes.from(attr);
    }

    /** 检查用户是否拥有指定领域角色。 */
    @Override
    public boolean hasRealmRole(String roleName) {
        RoleModel role = realm.getRole(roleName);
        if (role == null) return false;
        return user.hasRole(role);
    }

    /** 检查用户是否拥有客户端任一指定角色。 */
    @Override
    public boolean hasOneClientRole(String clientId, String... roleNames) {
        ClientModel client = realm.getClientByClientId(clientId);
        for (String roleName : roleNames) {
            RoleModel role = client.getRole(roleName);
            if (role == null) continue;
            if (user.hasRole(role)) return true;
        }
        return false;
    }

    /** 检查用户是否拥有客户端指定角色。 */
    @Override
    public boolean hasClientRole(String clientId, String roleName) {
        ClientModel client = realm.getClientByClientId(clientId);
        RoleModel role = client.getRole(roleName);
        if (role == null) return false;
        return user.hasRole(role);
    }
}
