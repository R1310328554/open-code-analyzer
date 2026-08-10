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
package org.keycloak.models.cache.infinispan;

import org.keycloak.models.RealmModel;
import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;
import org.keycloak.models.cache.infinispan.entities.InRealm;

/**
 * 缓存的存在性查询结果（如某 ID 是否存在）。
 * <p>
 * 用于避免重复数据库存在性检查，结果与 realm 绑定。
 */
public class CachedExists extends AbstractRevisioned implements InRealm {

    /** 所属 realm ID。 */
    private final String realm;

    /** 缓存的存在性布尔结果。 */
    private final boolean exists;

    /** 构造带 revision 的存在性缓存条目。 */
    public CachedExists(long revision, RealmModel realm, String cacheKey, boolean exists) {
        super(revision, cacheKey);
        this.realm = realm.getId();
        this.exists = exists;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回缓存的存在性结果。 */
    public boolean isExists() {
        return exists;
    }
}
