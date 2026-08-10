/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.RealmModel;

/**
 * 按组名解析得到的组 ID 查询缓存实体。
 * <p>
 * 实现 {@link InRealm}，缓存名称查询命中的单个组 ID，
 * 供 {@code InGroupPredicate} 等流式过滤器进行缓存匹配。
 */
public class GroupNameQuery extends AbstractRevisioned implements InRealm {
    /** 所属 realm 的唯一标识。 */
    private final String realm;
    /** 名称查询命中的组 ID。 */
    private final String groupId;

    /** 以组 ID 与 realm 构造名称查询缓存条目。 */
    public GroupNameQuery(long revisioned, String id, String groupId, RealmModel realm) {
        super(revisioned, id);
        this.realm = realm.getId();
        this.groupId = groupId;
    }

    /** 返回名称查询命中的组 ID。 */
    public String getGroupId() {
        return groupId;
    }

    /** 返回所属 realm 的唯一标识。 */
    public String getRealm() {
        return realm;
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return "GroupNameQuery{" +
                "id='" + getId() + "'" +
                "realm='" + realm + '\'' +
                '}';
    }
}
