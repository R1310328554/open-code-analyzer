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

import java.util.HashSet;
import java.util.Set;

import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.RealmModel;

/**
 * 用户联邦身份链接列表的 Infinispan 缓存实体。
 * <p>
 * 缓存某用户关联的全部身份提供者链接；当任意链接增删改时需同步更新此条目。
 * 实现 {@link InRealm} 与 {@link InIdentityProvider}。
 * 
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CachedFederatedIdentityLinks extends AbstractRevisioned implements InRealm, InIdentityProvider {

    /** 所属领域 ID。 */
    private final String realmId;
    /** 该用户的全部联邦身份链接集合。 */
    private final Set<FederatedIdentityModel> federatedIdentities = new HashSet<>();

    /** 从用户 ID 与联邦身份链接集合构造缓存条目。 */
    public CachedFederatedIdentityLinks(long revision, String id, RealmModel realm, Set<FederatedIdentityModel> federatedIdentities) {
        super(revision, id);
        this.realmId = realm.getId();
        this.federatedIdentities.addAll(federatedIdentities);
    }

    @Override
    public String getRealm() {
        return realmId;
    }

    /** 返回该用户的全部联邦身份链接。 */
    public Set<FederatedIdentityModel> getFederatedIdentities() {
        return federatedIdentities;
    }

    /** 判断是否包含指定别名（alias）的身份提供者链接。 */
    @Override 
    public boolean contains(String alias) {
        return federatedIdentities.stream().anyMatch(
                federatedIdentityModel -> federatedIdentityModel.getIdentityProvider().equals(alias));
    }
}
