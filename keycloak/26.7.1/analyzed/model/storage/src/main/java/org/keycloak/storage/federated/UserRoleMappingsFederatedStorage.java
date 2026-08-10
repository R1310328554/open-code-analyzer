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
package org.keycloak.storage.federated;

import java.util.stream.Stream;

import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;

/**
 * 联邦用户角色映射存储接口，管理外部用户与 Keycloak 角色之间的映射关系。
 *
 * <p>当外部用户存储无法直接维护角色归属时，通过联邦存储持久化角色映射。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserRoleMappingsFederatedStorage {

    /** 为联邦用户授予指定角色。 */
    void grantRole(RealmModel realm, String userId, RoleModel role);

    /**
     * 获取 {@code userId} 标识的联邦用户关联的全部角色。
     *
     * @param realm a reference to the realm.
     * @param userId the user identifier.
     * @return a non-null {@code Stream} of roles.
     */
    Stream<RoleModel> getRoleMappingsStream(RealmModel realm, String userId);

    /** 删除联邦用户与指定角色之间的映射。 */
    void deleteRoleMapping(RealmModel realm, String userId, RoleModel role);

   /**
    * 获取指定 {@code realm} 中拥有 {@code role} 的全部联邦用户 ID。
    *
    * @param realm a reference to the realm.
    * @param role a reference to the role whose federated members are being searched.
    * @param firstResult first result to return. Ignored if negative or {@code null}.
    * @param max maximum number of results to return. Ignored if negative or {@code null}.
    * @return a non-null {@code Stream} of federated user ids that are members of the role in the realm.
    */
	Stream<String> getRoleMembersStream(RealmModel realm, RoleModel role, Integer firstResult, Integer max);
    
    /**
     * @deprecated This interface is no longer necessary; collection-based methods were removed from the parent interface
     * and therefore the parent interface can be used directly
     */
    @Deprecated
    interface Streams extends UserRoleMappingsFederatedStorage {
    }
}
