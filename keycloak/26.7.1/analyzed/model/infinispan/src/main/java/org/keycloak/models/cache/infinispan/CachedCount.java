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
 * 缓存的计数查询结果（如客户端/用户数量）。
 * <p>
 * 与 InRealm 关联到特定 realm，避免跨 realm 缓存污染。
 */
public class CachedCount extends AbstractRevisioned implements InRealm {

    /** 所属 realm ID。 */
    private final String realm;
    /** 缓存的计数值。 */
    private final long count;

    /** 构造带 revision 的计数缓存条目。 */
    public CachedCount(long revision, RealmModel realm, String cacheKey, long count) {
        super(revision, cacheKey);
        this.realm = realm.getId();
        this.count = count;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回缓存的计数值。 */
    public long getCount() {
        return count;
    }
}
