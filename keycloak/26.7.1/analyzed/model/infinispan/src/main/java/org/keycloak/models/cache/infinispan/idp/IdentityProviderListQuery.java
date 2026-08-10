/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.cache.infinispan.idp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.keycloak.models.RealmModel;
import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;
import org.keycloak.models.cache.infinispan.entities.InRealm;

/**
 * 身份提供者列表查询结果的 Infinispan 缓存实体。
 * <p>
 * 同一缓存键下可存储多个搜索条件（searchKey）对应的结果集，
 * 用于缓存按组织、登录场景等分页查询返回的 IdP 内部 ID 集合。
 */
public class IdentityProviderListQuery extends AbstractRevisioned implements InRealm {
    /** 所属领域 ID。 */
    private final String realmId;
    /** 搜索键 → IdP 内部 ID 结果集的映射。 */
    private final Map<String, Set<String>> searchKeys;

    /** 构造单搜索键的列表查询缓存条目。 */
    public IdentityProviderListQuery(long revision, String id, RealmModel realm, String searchKey, Set<String> result) {
        super(revision, id);
        this.realmId = realm.getId();
        this.searchKeys = new HashMap<>();
        this.searchKeys.put(searchKey, result);
    }

    /** 在已有缓存条目上追加新的搜索键结果。 */
    public IdentityProviderListQuery(long revision, String id, RealmModel realm, String searchKey, Set<String> result, IdentityProviderListQuery previous) {
        super(revision, id);
        this.realmId = realm.getId();
        this.searchKeys = new HashMap<>();
        this.searchKeys.putAll(previous.searchKeys);
        this.searchKeys.put(searchKey, result);
    }

    /** 返回所属领域 ID。 */
    @Override
    public String getRealm() {
        return realmId;
    }

    /** 返回指定搜索键对应的 IdP 内部 ID 集合。 */
    public Set<String> getIDPs(String searchKey) {
        return searchKeys.get(searchKey);
    }
}
