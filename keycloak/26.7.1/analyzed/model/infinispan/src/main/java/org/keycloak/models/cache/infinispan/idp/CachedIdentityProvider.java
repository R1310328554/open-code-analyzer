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

import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;
import org.keycloak.models.cache.infinispan.entities.InRealm;

/**
 * 单个身份提供者（IdP）的 Infinispan 缓存实体。
 * <p>
 * 缓存 {@link IdentityProviderModel} 的快照，支持按内部 ID 或别名查询；
 * 返回模型副本以避免调用方修改缓存内容。
 */
public class CachedIdentityProvider extends AbstractRevisioned implements InRealm {

    /** 所属领域 ID。 */
    private final String realm;
    /** 缓存的身份提供者模型快照。 */
    private final IdentityProviderModel idp;

    /** 构造身份提供者缓存条目。 */
    public CachedIdentityProvider(long revision, RealmModel realm, String cacheKey, IdentityProviderModel idp) {
        super(revision, cacheKey);
        this.realm = realm.getId();
        this.idp = idp;
    }

    /** 返回所属领域 ID。 */
    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回身份提供者模型的防御性副本。 */
    public IdentityProviderModel getIdentityProvider() {
        return new IdentityProviderModel(idp);
    }

}
