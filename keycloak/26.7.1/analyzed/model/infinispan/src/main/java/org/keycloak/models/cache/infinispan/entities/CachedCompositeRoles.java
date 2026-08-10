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

import java.util.List;
import java.util.Set;

import org.keycloak.models.RealmModel;

/**
 * 组合角色（Composite Roles）查询结果的 Infinispan 缓存实体。
 * <p>
 * 缓存父角色 ID 集合与组合子角色 ID 列表，实现 {@link InRealm}，供按角色查询组合关系时使用。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CachedCompositeRoles extends AbstractRevisioned implements InRealm {

    /** 父角色 ID 集合。 */
    private final Set<String> parentIds;
    /** 所属领域 ID。 */
    final protected String realm;
    /** 组合子角色 ID 列表。 */
    private final List<String> compositeIds;

    /** 构造组合角色查询缓存条目。 */
    public CachedCompositeRoles(long revision, String id, Set<String> parentIds, List<String> compositeIds, RealmModel realm) {
        super(revision, id);
        this.parentIds = parentIds;
        this.realm = realm.getId();
        this.compositeIds = compositeIds;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回父角色 ID 集合。 */
    public Set<String> getParentIds() {
        return parentIds;
    }

    /** 返回组合子角色 ID 列表。 */
    public List<String> getCompositeIds() {
        return compositeIds;
    }

}
