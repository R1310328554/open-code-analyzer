/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import java.util.Set;

import org.keycloak.models.RealmModel;

/**
 * 按角色名称查询的 Infinispan 缓存实体。
 * <p>
 * 缓存单个角色名称及其所属领域与客户端上下文，实现 {@link RoleQuery} 与 {@link InClient}，
 * 供按名称查找角色时复用查询结果。
 *
 * @author Alexander Schwartz
 * @version $Revision: 1 $
 */
public class RoleByNameQuery extends AbstractRevisioned implements RoleQuery, InClient {
    /** 目标角色名称。 */
    private final String role;
    /** 所属领域 ID。 */
    private final String realm;
    /** 所属客户端 UUID（领域角色时为 null）。 */
    private String client;

    /** 构造领域或客户端无关的角色名称查询缓存条目。 */
    public RoleByNameQuery(long revisioned, String id, RealmModel realm, String role) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.role = role;
    }

    /** 构造绑定特定客户端的角色名称查询缓存条目。 */
    public RoleByNameQuery(long revision, String id, RealmModel realm, String role, String client) {
        this(revision, id, realm, role);
        this.client = client;
    }

    /** 返回包含单个角色名称的集合（名称缺失时为空集）。 */
    @Override
    public Set<String> getRoles() {
        return role == null ? Set.of() : Set.of(role);
    }

    /** 返回目标角色名称。 */
    public String getRole() {
        return role;
    }

    /** 返回所属领域 ID。 */
    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回所属客户端 UUID。 */
    @Override
    public String getClientId() {
        return client;
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return "RoleNameQuery{" +
                "id='" + getId() + "'" +
                ", realm='" + realm + '\'' +
                ", clientUuid='" + client + '\'' +
                '}';
    }
}
