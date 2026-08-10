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

import java.util.HashMap;
import java.util.List;

import org.keycloak.models.RealmModel;

/**
 * 用户全部授权同意（User Consents）的 Infinispan 缓存实体。
 * <p>
 * 以客户端 ID 为键缓存 {@link CachedUserConsent} 条目，实现 {@link InRealm}。
 * {@link #allConsents} 标记是否已加载用户的全部同意记录。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CachedUserConsents extends AbstractRevisioned implements InRealm {
    /** 按客户端 ID 索引的用户同意缓存映射。 */
    private HashMap<String, CachedUserConsent> consents = new HashMap<>();
    /** 所属领域 ID。 */
    private final String realmId;
    /** 是否已缓存用户的全部同意记录。 */
    private boolean allConsents;

    /** 构造包含全部同意的缓存条目（默认 {@code allConsents=true}）。 */
    public CachedUserConsents(long revision, String id, RealmModel realm,
                              List<CachedUserConsent> consents) {
        this(revision, id, realm, consents, true);
    }

    /** 构造用户同意缓存条目，可指定是否为全部同意。 */
    public CachedUserConsents(long revision, String id, RealmModel realm,
            List<CachedUserConsent> consents, boolean allConsents) {
        super(revision, id);
        this.realmId = realm.getId();
        this.allConsents = allConsents;
        if (consents != null) {
            for (CachedUserConsent consent : consents) {
                this.consents.put(consent.getClientDbId(), consent);
            }
        }
    }

    @Override
    public String getRealm() {
        return realmId;
    }


    /** 返回按客户端 ID 索引的同意缓存映射。 */
    public HashMap<String, CachedUserConsent> getConsents() {
        return consents;
    }

    /** 返回是否已缓存用户的全部同意记录。 */
    public boolean isAllConsents() {
        return allConsents;
    }
}
