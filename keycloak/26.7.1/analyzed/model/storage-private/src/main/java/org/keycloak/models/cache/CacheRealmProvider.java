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

package org.keycloak.models.cache;

import org.keycloak.models.ClientProvider;
import org.keycloak.models.ClientScopeProvider;
import org.keycloak.models.GroupProvider;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.RoleModel;
import org.keycloak.models.RoleProvider;

/**
 * 领域（Realm）缓存 Provider 接口：聚合 Realm、Client、ClientScope、Group 与 Role 的缓存化访问，
 * 并提供细粒度的缓存失效注册能力。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface CacheRealmProvider extends RealmProvider, ClientProvider, ClientScopeProvider, GroupProvider, RoleProvider {
    /** 清空整个 realm 相关缓存。 */
    void clear();
    /** 获取底层未缓存的 {@link RealmProvider} 委托实现。 */
    RealmProvider getRealmDelegate();

    /** 注册 realm 缓存失效（按 ID 与名称）。 */
    void registerRealmInvalidation(String id, String name);

    /** 注册 client 缓存失效（按 ID、clientId 与所属 realm）。 */
    void registerClientInvalidation(String id, String clientId, String realmId);
    /** 注册 client scope 缓存失效（按 ID 与所属 realm）。 */
    void registerClientScopeInvalidation(String id, String realmId);

    /** 注册 role 缓存失效（按 ID、角色名与容器 ID）。 */
    void registerRoleInvalidation(String id, String roleName, String roleContainerId);

    /** 注册 group 缓存失效（按 ID）。 */
    void registerGroupInvalidation(String id);
    /** 注册通用实体缓存失效（按 ID）。 */
    void registerInvalidation(String id);
    /** 尝试刷新 master 管理员角色缓存；默认返回 false 表示无需刷新。 */
    default boolean refreshMasterAdminRole(RoleModel masterAdminRole, String clientId) {
        return false;
    }
}
