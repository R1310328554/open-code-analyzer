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

import org.keycloak.models.RealmModel;
import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;
import org.keycloak.models.cache.infinispan.entities.InRealm;

/**
 * 组织成员关系判定结果的 Infinispan 缓存实体。
 * <p>
 * 缓存某用户是否属于指定组织，以及该成员关系是否由组织托管（managed），
 * 避免重复查询数据库以判断成员资格。
 */
public class CachedMembership extends AbstractRevisioned implements InRealm {

    /** 所属领域 ID。 */
    private final String realm;
    /** 成员关系是否由组织托管。 */
    private final boolean managed;
    /** 用户是否为该组织成员。 */
    private final boolean isMember;

    /** 构造组织成员关系缓存条目。 */
    public CachedMembership(long revision, String key, RealmModel realm, boolean managed, boolean isMember) {
        super(revision, key);
        this.realm = realm.getId();
        this.managed = managed;
        this.isMember = isMember;
    }

    /** 返回所属领域 ID。 */
    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回成员关系是否由组织托管。 */
    public boolean isManaged() {
        return managed;
    }

    /** 返回用户是否为该组织成员。 */
    public boolean isMember() {
        return isMember;
    }
}
