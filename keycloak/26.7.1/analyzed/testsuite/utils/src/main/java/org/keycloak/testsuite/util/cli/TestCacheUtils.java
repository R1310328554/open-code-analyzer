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

package org.keycloak.testsuite.util.cli;

import java.util.Collections;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;

/**
 * 测试用缓存预热工具，通过重复查询将领域对象加载到 Keycloak 缓存中。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TestCacheUtils {

    /**
     * 预热指定领域的客户端、角色、组、客户端作用域及用户相关缓存条目。
     *
     * @param session Keycloak 会话
     * @param realmName 领域名称
     */
    public static void cacheRealmWithEverything(KeycloakSession session, String realmName) {
        RealmModel realm  = session.realms().getRealmByName(realmName);

        realm.getClientsStream().forEach(c -> {
            realm.getClientById(c.getId());
            realm.getClientByClientId(c.getClientId());

            cacheRoles(session, realm, c);
        });

        cacheRoles(session, realm, realm);

        realm.getTopLevelGroupsStream().forEach(group -> cacheGroupRecursive(realm, group));

        realm.getClientScopesStream().map(ClientScopeModel::getId).forEach(realm::getClientScopeById);

        session.users().searchForUserStream(realm, Collections.emptyMap()).forEach(user -> {
            session.users().getUserById(realm, user.getId());
            if (user.getEmail() != null) {
                session.users().getUserByEmail(realm, user.getEmail());
            }
            session.users().getUserByUsername(realm, user.getUsername());

            session.users().getConsentsStream(realm, user.getId());

            session.users().getFederatedIdentitiesStream(realm, user)
                    .forEach(identity -> session.users().getUserByFederatedIdentity(realm, identity));
        });
    }

    /** 遍历角色容器并触发按 ID 与名称的缓存加载。 */
    private static void cacheRoles(KeycloakSession session, RealmModel realm, RoleContainerModel roleContainer) {
        roleContainer.getRolesStream().forEach(role -> {
            realm.getRoleById(role.getId());
            roleContainer.getRole(role.getName());
            if (roleContainer instanceof RealmModel) {
                session.roles().getRealmRole(realm, role.getName());
            } else {
                session.roles().getClientRole((ClientModel) roleContainer, role.getName());
            }
        });
    }

    /** 递归预热组及其子组的缓存条目。 */
    private static void cacheGroupRecursive(RealmModel realm, GroupModel group) {
        realm.getGroupById(group.getId());
        group.getSubGroupsStream().forEach(sub -> cacheGroupRecursive(realm, sub));
    }
}
