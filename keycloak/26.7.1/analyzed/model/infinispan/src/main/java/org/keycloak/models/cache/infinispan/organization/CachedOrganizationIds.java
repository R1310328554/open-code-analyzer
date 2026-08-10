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

package org.keycloak.models.cache.infinispan.organization;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;
import org.keycloak.models.cache.infinispan.entities.InRealm;

/**
 * 组织 ID 列表查询结果的 Infinispan 缓存实体。
 * <p>
 * 缓存按条件查询返回的组织内部 ID 集合，支持单个组织或组织流两种构造方式，
 * 避免重复执行相同的数据库列表查询。
 */
public class CachedOrganizationIds extends AbstractRevisioned implements InRealm {

    /** 所属领域 ID。 */
    private final String realmId;
    /** 缓存的组织 ID 列表（不可变）。 */
    private final List<String> orgIds;

    /** 从单个组织模型构造仅含一个 ID 的缓存条目。 */
    public CachedOrganizationIds(long revision, String id, RealmModel realm, OrganizationModel model) {
        super(revision, id);
        this.realmId = realm.getId();
        orgIds = List.of(model.getId());
    }

    /** 从组织模型流构造缓存条目（流会被消费并转换为 ID 列表）。 */
    public CachedOrganizationIds(long revision, String id, RealmModel realm, Stream<OrganizationModel> models) {
        super(revision, id);
        this.realmId = realm.getId();
        var ids = models.map(OrganizationModel::getId).collect(Collectors.toSet());
        orgIds = ids.isEmpty() ? List.of() : List.of(ids.toArray(new String[0]));
    }

    /** 返回缓存的组织 ID 集合。 */
    public Collection<String> getOrgIds() {
        return orgIds;
    }

    /** 返回所属领域 ID。 */
    @Override
    public String getRealm() {
        return realmId;
    }
}
