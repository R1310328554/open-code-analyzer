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
package org.keycloak.authorization.common;

import org.keycloak.OAuth2Constants;
import org.keycloak.authorization.attribute.Attributes;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.AccessToken;

/**
 * 基于 {@link ClientModel} 的授权身份：代表客户端或其服务账户。
 * <p>角色校验通过关联的 service account 用户执行。</p>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientModelIdentity implements Identity {
    protected final RealmModel realm;
    protected final ClientModel client;
    protected final UserModel serviceAccount;
    protected final AccessToken token;

    /** @param session Keycloak 会话 @param client 客户端模型 */
    public ClientModelIdentity(KeycloakSession session, ClientModel client) {
        this(session, client, null);
    }

    /** @param token 可选 access token，用于 scope 属性 */
    public ClientModelIdentity(KeycloakSession session, ClientModel client, AccessToken token) {
        this.realm = session.getContext().getRealm();
        this.client = client;
        this.serviceAccount = session.users().getServiceAccount(client);
        this.token = token;
    }

    /** @return 客户端内部 ID */
    @Override
    public String getId() {
        return client.getId();
    }

    /** 合并 service account 用户属性与 token scope。 */
    @Override
    public Attributes getAttributes() {
        MultivaluedHashMap map = new MultivaluedHashMap<String, String>();
        if (serviceAccount != null) map.addAll(serviceAccount.getAttributes());
        if (token != null) {
            map.add(OAuth2Constants.SCOPE, token.getScope());
        }
        return Attributes.from(map);
    }

    /** @return service account 是否拥有 realm 角色 */
    @Override
    public boolean hasRealmRole(String roleName) {
        if (serviceAccount == null) return false;
        RoleModel role = realm.getRole(roleName);
        if (role == null) return false;
        return serviceAccount.hasRole(role);
    }

    /** @return service account 是否拥有指定客户端角色 */
    @Override
    public boolean hasClientRole(String clientId, String roleName) {
        if (serviceAccount == null) return false;
        ClientModel client = realm.getClientByClientId(clientId);
        RoleModel role = client.getRole(roleName);
        if (role == null) return false;
        return serviceAccount.hasRole(role);
    }

    /** @return service account 是否拥有任一指定客户端角色 */
    @Override
    public boolean hasOneClientRole(String clientId, String... roleNames) {
        if (serviceAccount == null) return false;
        ClientModel client = realm.getClientByClientId(clientId);
        for (String roleName : roleNames) {
            RoleModel role = client.getRole(roleName);
            if (role == null) continue;
            if (serviceAccount.hasRole(role)) return true;
        }
        return false;
    }

}
