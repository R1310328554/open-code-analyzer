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

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 作用域容器模型：管理客户端/资源的作用域角色映射。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ScopeContainerModel {

    /**
     * 以流形式返回此作用域容器的全部作用域映射。
     * Returns scope mappings for this scope container as a stream.
     * @return Stream of {@link RoleModel}. Never returns {@code null}.
     */
    Stream<RoleModel> getScopeMappingsStream();

    /**
     * 从 {@link #getScopeMappingsStream()} 结果中仅返回属于拥有此容器的 Realm 的映射。
     * From the scope mappings returned by {@link #getScopeMappingsStream()} returns only those
     * that belong to the realm that owns this scope container.
     * @return stream of {@link RoleModel}. Never returns {@code null}.
     */
    Stream<RoleModel> getRealmScopeMappingsStream();

    /** @param role 待添加的作用域角色 */
    void addScopeMapping(RoleModel role);

    /** @param role 待删除的作用域角色 */
    void deleteScopeMapping(RoleModel role);

    /**
     * 若作用域中直接包含给定角色则返回 {@code true}。
     * Returns {@code true}, if this object has the given role directly in its scope.
     *
     * @param role the role
     * @return see description
     * @see #hasScope(RoleModel) if you want to check whether this object has the given role directly or indirectly in
     *      its scope
     */
    default boolean hasDirectScope(RoleModel role) {
        return getScopeMappingsStream().anyMatch(r -> Objects.equals(r, role));
    }

    /**
     * 若作用域中直接或间接包含给定角色则返回 {@code true}，否则 {@code false}。
     * Returns {@code true}, if this object has the given role directly or indirectly in its scope, {@code false}
     * otherwise.
     *
     * @param role the role
     * @return see description
     * @see #hasDirectScope(RoleModel) if you want to check if this object has the given role directly in its scope
     */
    boolean hasScope(RoleModel role);

}
