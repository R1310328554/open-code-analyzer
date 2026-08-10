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

import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;
import org.keycloak.models.cache.infinispan.entities.InRealm;

/**
 * 身份提供者映射器（IdP Mapper）的 Infinispan 缓存实体。
 * <p>
 * 缓存 {@link IdentityProviderMapperModel}，支持按内部 ID 或 IdP 别名+名称组合查询。
 */
public class CachedIdentityProviderMapper extends AbstractRevisioned implements InRealm {

    /** 所属领域 ID。 */
    private final String realm;
    /** 缓存的身份提供者映射器模型。 */
    private final IdentityProviderMapperModel mapper;

    /** 构造身份提供者映射器缓存条目。 */
    public CachedIdentityProviderMapper(long revision, RealmModel realm, String cacheKey, IdentityProviderMapperModel mapper) {
        super(revision, cacheKey);
        this.realm = realm.getId();
        this.mapper = mapper;
    }

    /** 返回所属领域 ID。 */
    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回缓存的身份提供者映射器模型。 */
    public IdentityProviderMapperModel getIdentityProviderMapper() {
        return mapper;
    }

}
