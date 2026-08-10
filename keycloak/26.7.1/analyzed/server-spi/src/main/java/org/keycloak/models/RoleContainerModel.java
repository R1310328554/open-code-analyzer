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

package org.keycloak.models;

import java.util.stream.Stream;

import org.keycloak.provider.ProviderEvent;

/**
 * 角色容器模型：Realm 或客户端上角色的 CRUD 与查询接口。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RoleContainerModel {

    /** 角色删除事件。 */
    interface RoleRemovedEvent extends ProviderEvent {
        RoleModel getRole();
        KeycloakSession getKeycloakSession();
    }

    /** @return 容器唯一标识符 */
    String getId();

    /** @param name 角色名称
     * @return 匹配的角色 */
    RoleModel getRole(String name);

    /** @param name 角色名称
     * @return 新创建的角色 */
    RoleModel addRole(String name);

    /** @param id 角色 ID
     * @param name 角色名称
     * @return 新创建的角色 */
    RoleModel addRole(String id, String name);

    /** @param role 待删除角色
     * @return 是否成功删除 */
    boolean removeRole(RoleModel role);

    /**
     * 以流形式返回可用角色。
     * Returns available roles as a stream.
     * @return Stream of {@link RoleModel}. Never returns {@code null}.
     */
    Stream<RoleModel> getRolesStream();

    /**
     * Returns available roles as a stream.
     * @param firstResult {@code Integer} Index of the first desired role. Ignored if negative or {@code null}.
     * @param maxResults {@code Integer} Maximum number of returned roles. Ignored if negative or {@code null}.
     * @return Stream of {@link RoleModel}. Never returns {@code null}.
     */
    Stream<RoleModel> getRolesStream(Integer firstResult, Integer maxResults);

    /**
     * 按名称搜索匹配的角色。
     * Searches roles by the given name. Returns all roles that match the given filter.
     * @param search {@code String} Name of the role to be used as a filter.
     * @param first {@code Integer} Index of the first desired role. Ignored if negative or {@code null}.
     * @param max {@code Integer} Maximum number of returned roles. Ignored if negative or {@code null}.
     * @return Stream of {@link RoleModel}. Never returns {@code null}.
     */
    Stream<RoleModel> searchForRolesStream(String search, Integer first, Integer max);

}
