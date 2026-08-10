/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.utils;

import java.util.Map;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.representations.AccessToken;

/**
 * 角色解析缓存工具。
 * <p>确保每个请求仅加载一次用户授权角色（含复合角色），供协议映射器复用。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RoleResolveUtil {

    /** 会话属性前缀，用于缓存已解析角色。 */
    private static final String RESOLVED_ROLES_ATTR = "RESOLVED_ROLES";


    /**
     * 获取用户领域角色（含组角色、展开复合角色）。
     * <p>仅包含当前客户端有 scope 映射的角色。</p>
     *
     * @param session Keycloak 会话
     * @param clientSessionCtx 客户端会话上下文
     * @param createIfMissing 不存在时是否创建空 Access 对象
     * @return 领域角色 Access，{@code createIfMissing} 为 false 时可能为 null
     */
    public static AccessToken.Access getResolvedRealmRoles(KeycloakSession session, ClientSessionContext clientSessionCtx, boolean createIfMissing) {
        AccessToken rolesToken = getAndCacheResolvedRoles(session, clientSessionCtx);
        AccessToken.Access access = rolesToken.getRealmAccess();
        if (access == null && createIfMissing) {
            access = new AccessToken.Access();
            rolesToken.setRealmAccess(access);
        }

        return access;
    }


    /**
     * 获取指定 clientId 的客户端角色（含组角色、展开复合角色）。
     *
     * @param session Keycloak 会话
     * @param clientSessionCtx 客户端会话上下文
     * @param clientId 目标客户端 ID
     * @param createIfMissing 不存在时是否创建空 Access 对象
     * @return 客户端角色 Access，{@code createIfMissing} 为 false 时可能为 null
     */
    public static AccessToken.Access getResolvedClientRoles(KeycloakSession session, ClientSessionContext clientSessionCtx, String clientId, boolean createIfMissing) {
        AccessToken rolesToken = getAndCacheResolvedRoles(session, clientSessionCtx);
        AccessToken.Access access = rolesToken.getResourceAccess(clientId);

        if (access == null && createIfMissing) {
            access = rolesToken.addAccess(clientId);
        }

        return access;
    }


    /**
     * 获取所有客户端的角色映射（非 null，可为空 Map）。
     *
     * @param session Keycloak 会话
     * @param clientSessionCtx 客户端会话上下文
     * @return clientId → Access 映射
     */
    public static Map<String, AccessToken.Access> getAllResolvedClientRoles(KeycloakSession session, ClientSessionContext clientSessionCtx) {
        return getAndCacheResolvedRoles(session, clientSessionCtx).getResourceAccess();
    }

    /** 解析并缓存角色到会话属性，避免重复计算。 */
    private static AccessToken getAndCacheResolvedRoles(KeycloakSession session, ClientSessionContext clientSessionCtx) {
        ClientModel client = clientSessionCtx.getClientSession().getClient();
        String resolvedRolesAttrName = RESOLVED_ROLES_ATTR + ":" + clientSessionCtx.getClientSession().getUserSession().getId() + ":" + client.getId();
        AccessToken token = session.getAttribute(resolvedRolesAttrName, AccessToken.class);

        if (token == null) {
            AccessToken finalToken = new AccessToken();
            clientSessionCtx.getRolesStream().forEach(role -> addToToken(finalToken, role));
            token = finalToken;
            session.setAttribute(resolvedRolesAttrName, token);
        }

        return token;
    }

    /** 将单个角色加入 AccessToken 的领域或资源 Access 段。 */
    private static void addToToken(AccessToken token, RoleModel role) {
        AccessToken.Access access = null;
        if (role.getContainer() instanceof RealmModel) {
            access = token.getRealmAccess();
            if (token.getRealmAccess() == null) {
                access = new AccessToken.Access();
                token.setRealmAccess(access);
            } else if (token.getRealmAccess().getRoles() != null && token.getRealmAccess().isUserInRole(role.getName()))
                return;

        } else {
            ClientModel app = (ClientModel) role.getContainer();
            if (app == null) {
                return;
            }
            access = token.getResourceAccess(app.getClientId());
            if (access == null) {
                access = token.addAccess(app.getClientId());
                if (app.isSurrogateAuthRequired()) access.verifyCaller(true);
            } else if (access.isUserInRole(role.getName())) return;

        }
        access.addRole(role.getName());
    }

}
