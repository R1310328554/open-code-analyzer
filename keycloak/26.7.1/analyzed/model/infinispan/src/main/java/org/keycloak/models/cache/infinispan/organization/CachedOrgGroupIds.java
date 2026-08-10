/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.models.GroupModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;
import org.keycloak.models.cache.infinispan.entities.InRealm;

/**
 * 组织相关群组 ID 查询结果的轻量级缓存实体。
 * <p>
 * 仅缓存群组 ID 列表而非完整 {@link GroupModel} 对象；
 * 需要完整群组信息时从领域群组缓存按需加载。
 */
public class CachedOrgGroupIds extends AbstractRevisioned implements InRealm {

    /** 所属领域 ID。 */
    private final String realmId;
    /** 缓存的群组 ID 列表（不可变）。 */
    private final List<String> groupIds;

    /**
     * 从群组模型流构造缓存条目（流会被消费并转换为 ID 集合）。
     *
     * @param revision 缓存修订号
     * @param id 缓存键
     * @param realm 所属领域
     * @param groups 待缓存的群组流
     */
    public CachedOrgGroupIds(Long revision, String id, RealmModel realm, Stream<GroupModel> groups) {
        super(revision, id);
        this.realmId = realm.getId();
        Set<String> ids = groups.map(GroupModel::getId).collect(Collectors.toSet());
        groupIds = ids.isEmpty() ? List.of() : List.copyOf(ids);
    }

    /**
     * 返回缓存的群组 ID 集合。
     *
     * @return 不可变的群组 ID 集合
     */
    public Collection<String> getGroupIds() {
        return groupIds;
    }

    /** 返回所属领域 ID。 */
    @Override
    public String getRealm() {
        return realmId;
    }
}
