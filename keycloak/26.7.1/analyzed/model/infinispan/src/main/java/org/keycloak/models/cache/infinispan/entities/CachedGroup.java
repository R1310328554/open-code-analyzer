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

package org.keycloak.models.cache.infinispan.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.GroupModel;
import org.keycloak.models.GroupModel.Type;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.cache.infinispan.DefaultLazyLoader;
import org.keycloak.models.cache.infinispan.LazyLoader;

/**
 * 用户组（Group）的 Infinispan 缓存快照实体。
 * <p>
 * 缓存组名称、层级关系与类型；属性、角色映射与子组通过 {@link LazyLoader} 按需加载。
 * 实现 {@link InRealm}。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CachedGroup extends AbstractRevisioned implements InRealm {

    /** 所属领域 ID。 */
    private final String realm;
    /** 组名称。 */
    private final String name;
    /** 组描述。 */
    private final String description;
    /** 父组 ID（根组为 null）。 */
    private final String parentId;
    /** 组创建时间戳。 */
    private final Long createdTimestamp;
    /** 组最后修改时间戳。 */
    private final Long lastModifiedTimestamp;
    /** 组属性懒加载器。 */
    private final LazyLoader<GroupModel, MultivaluedHashMap<String, String>> attributes;
    /** 组角色映射懒加载器。 */
    private final LazyLoader<GroupModel, Set<String>> roleMappings;
    /**
     * 供缓存失效逻辑读取先前已缓存的角色映射，以判定本条目是否应被驱逐。
     */
    private Set<String> cachedRoleMappings = new HashSet<>();
    /** 子组 ID 懒加载器。 */
    private final LazyLoader<GroupModel, Set<String>> subGroups;
    /** 组类型（普通组或组织组等）。 */
    private final Type type;
    /** 关联组织 ID（非组织组时为 null）。 */
    private final String organizationId;

    /** 从组模型构造缓存快照。 */
    public CachedGroup(long revision, RealmModel realm, GroupModel group) {
        super(revision, group.getId());
        this.realm = realm.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.parentId = group.getParentId();
        this.createdTimestamp = group.getCreatedTimestamp();
        this.lastModifiedTimestamp = group.getLastModifiedTimestamp();
        this.attributes = new DefaultLazyLoader<>(source -> new MultivaluedHashMap<>(source.getAttributes()), MultivaluedHashMap::new);
        this.roleMappings = new DefaultLazyLoader<>(source -> source.getRoleMappingsStream().map(RoleModel::getId).collect(Collectors.toSet()), Collections::emptySet);
        this.subGroups = new DefaultLazyLoader<>(source -> source.getSubGroupsStream().map(GroupModel::getId).collect(Collectors.toSet()), Collections::emptySet);
        this.type = group.getType();
        this.organizationId = group.getOrganization() == null ? null : group.getOrganization().getId();
    }

    @Override
    public String getRealm() {
        return realm;
    }

    /** 按需加载并返回组属性映射。 */
    public MultivaluedHashMap<String, String> getAttributes(KeycloakSession session, Supplier<GroupModel> group) {
        return attributes.get(session, group);
    }

    /** 按需加载并返回组角色映射，同时更新 {@link #cachedRoleMappings}。 */
    public Set<String> getRoleMappings(KeycloakSession session, Supplier<GroupModel> group) {
        cachedRoleMappings = roleMappings.get(session, group);
        return cachedRoleMappings;
    }

    /**
     * 供缓存失效逻辑读取先前已缓存的角色映射，以判定本条目是否应被驱逐。
     * 若尚未加载则返回空集合（此时无需失效）。
     */
    public Set<String> getCachedRoleMappings() {
        return cachedRoleMappings;
    }

    /** 返回组名称。 */
    public String getName() {
        return name;
    }

    /** 返回组创建时间戳。 */
    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    /** 返回组最后修改时间戳。 */
    public Long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    /** 返回组描述。 */
    public String getDescription() {
        return description;
    }

    /** 返回父组 ID。 */
    public String getParentId() {
        return parentId;
    }

    /** 按需加载并返回子组 ID 集合。 */
    public Set<String> getSubGroups(KeycloakSession session, Supplier<GroupModel> group) {
        return subGroups.get(session, group);
    }

    /** 返回组类型。 */
    public Type getType() {
        return type;
    }

    /** 返回关联组织 ID。 */
    public String getOrganizationId() {
        return organizationId;
    }
}
