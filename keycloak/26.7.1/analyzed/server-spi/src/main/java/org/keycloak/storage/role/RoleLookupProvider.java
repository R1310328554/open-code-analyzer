/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.storage.role;

import java.util.stream.Stream;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;

/**
 * 角色查找抽象：按 ID、名称与描述查询 realm 角色与客户端角色。
 * Abstraction interface for lookup of both realm roles and client roles by id, name and description.
 */
public interface RoleLookupProvider {

    /**
     * 按名称精确查找 realm 角色。
     * Exact search for a role by given name.
     * @param realm Realm.
     * @param name String name of the role.
     * @return Model of the role, or {@code null} if no role is found.
     */
    RoleModel getRealmRole(RealmModel realm, String name);

    /**
     * 按内部 ID 精确查找角色。
     * Exact search for a role by its internal ID..
     * @param realm Realm.
     * @param id Internal ID of the role.
     * @return Model of the role.
     */
    RoleModel getRoleById(RealmModel realm, String id);

    /**
     * 在角色名称或描述中不区分大小写地模糊搜索 realm 角色。
     * Case-insensitive search for roles that contain the given string in their name or description.
     * @param realm Realm.
     * @param search Searched substring of the role's name or description.
     * @param first First result to return. Ignored if negative or {@code null}.
     * @param max Maximum number of results to return. Ignored if negative or {@code null}.
     * @return Stream of the realm roles their name or description contains given search string. 
     * Never returns {@code null}.
     */
    Stream<RoleModel> searchForRolesStream(RealmModel realm, String search, Integer first, Integer max);

    /**
     * 按名称精确查找客户端角色。
     * Exact search for a client role by given name.
     * @param client Client.
     * @param name String name of the role.
     * @return Model of the role, or {@code null} if no role is found.
     */
    RoleModel getClientRole(ClientModel client, String name);

    /**
     * 在客户端角色名称或描述中不区分大小写地模糊搜索。
     * Case-insensitive search for client roles that contain the given string in their name or description.
     * @param client Client.
     * @param search String to search by role's name or description.
     * @param first First result to return. Ignored if negative or {@code null}.
     * @param max Maximum number of results to return. Ignored if negative or {@code null}.
     * @return Stream of the client roles their name or description contains given search string. 
     * Never returns {@code null}.
     */
    Stream<RoleModel> searchForClientRolesStream(ClientModel client, String search, Integer first, Integer max);

    /**
     * 在客户端角色名称或所属 clientId 中模糊搜索（可限定 ID 集合）。
     * Case-insensitive search for client roles that contain the given string in its name or their client's public identifier (clientId - ({@code client_id} in OIDC or {@code entityID} in SAML)).
     * @param realm Realm.
     * @param ids Stream of ids to include in search. Ignored when {@code null}. Returns empty {@code Stream} when empty.
     * @param search String to search by role's name or client's public identifier.
     * @param first First result to return. Ignored if negative or {@code null}.
     * @param max Maximum number of results to return. Ignored if negative or {@code null}.
     * @return Stream of the client roles where role name or client public identifier contains given search string.
     * Never returns {@code null}.
     */
    Stream<RoleModel> searchForClientRolesStream(RealmModel realm, Stream<String> ids, String search, Integer first, Integer max);

    /**
     * 在客户端角色名称或 clientId 中模糊搜索，可排除指定 ID。
     * Case-insensitive search for client roles that contain the given string in their name or their client's public identifier (clientId - ({@code client_id} in OIDC or {@code entityID} in SAML)).
     *
     * @param realm       Realm.
     * @param search      String to search by role's name or client's public identifier.
     * @param excludedIds Stream of ids to exclude. Ignored if empty or {@code null}.
     * @param first       First result to return. Ignored if negative or {@code null}.
     * @param max         Maximum number of results to return. Ignored if negative or {@code null}.
     * @return Stream of the client roles where role name or client's public identifier contains given search string.
     * Never returns {@code null}.
     */
    Stream<RoleModel> searchForClientRolesStream(RealmModel realm, String search, Stream<String> excludedIds, Integer first, Integer max);
}
