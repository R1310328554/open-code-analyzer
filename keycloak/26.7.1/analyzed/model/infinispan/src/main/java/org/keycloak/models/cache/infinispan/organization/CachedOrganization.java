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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationDomainModel;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.cache.infinispan.DefaultLazyLoader;
import org.keycloak.models.cache.infinispan.LazyLoader;
import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;
import org.keycloak.models.cache.infinispan.entities.InRealm;

/**
 * 单个组织的 Infinispan 缓存实体。
 * <p>
 * 缓存组织的基本属性、域名集合、关联 IdP 及懒加载的属性映射；
 * 域名名称采用 LRU 策略限制内存占用，超出 {@link #DOMAIN_NAMES_CACHE_MAX_SIZE} 时触发失效回调。
 */
public class CachedOrganization extends AbstractRevisioned implements InRealm {

    /** 域名名称 LRU 缓存的最大条目数。 */
    public static final int DOMAIN_NAMES_CACHE_MAX_SIZE = 100;

    /** 所属领域 ID。 */
    private final String realm;
    /** 组织名称。 */
    private final String name;
    /** 组织别名。 */
    private final String alias;
    /** 组织描述。 */
    private final String description;
    /** 组织重定向 URL。 */
    private final String redirectUrl;
    /** 组织是否启用。 */
    private final boolean enabled;
    /** 组织属性的懒加载器（按需从数据库加载）。 */
    private final LazyLoader<OrganizationModel, MultivaluedHashMap<String, String>> attributes;
    /** 组织域名模型集合。 */
    private final Set<OrganizationDomainModel> domains;
    /** 域名 → 域名 的 LRU 缓存，用于快速按名称查找。 */
    private final Map<String, String> domainNames;
    /** 组织关联的身份提供者集合。 */
    private final Set<IdentityProviderModel> idps;

    /** 从组织模型构造缓存条目。 */
    public CachedOrganization(long revision, RealmModel realm, OrganizationModel organization, Consumer<String> invalidateDomain) {
        super(revision, organization.getId());
        this.realm = realm.getId();
        this.name = organization.getName();
        this.alias = organization.getAlias();
        this.description = organization.getDescription();
        this.redirectUrl = organization.getRedirectUrl();
        this.enabled = organization.isEnabled();
        this.attributes = new DefaultLazyLoader<>(orgModel -> new MultivaluedHashMap<>(orgModel.getAttributes()), MultivaluedHashMap::new);
        this.domains = organization.getDomains().collect(Collectors.toSet());
        this.domainNames = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                boolean remove = domainNames.size() > DOMAIN_NAMES_CACHE_MAX_SIZE;

                if (remove) {
                    String domain = eldest.getKey().toString();
                    invalidateDomain.accept(domain);
                }

                return remove;
            }
        });
        organization.getDomains().forEach(domain -> domainNames.put(domain.getName(), domain.getName()));
        this.idps = organization.getIdentityProviders().collect(Collectors.toSet());
    }

    /** 返回所属领域 ID。 */
    @Override
    public String getRealm() {
        return realm;
    }

    /** 返回组织名称。 */
    public String getName() {
        return name;
    }

    /** 返回组织别名。 */
    public String getAlias() {
        return alias;
    }

    /** 返回组织描述。 */
    public String getDescription() {
        return description;
    }

    /** 返回组织重定向 URL。 */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /** 返回组织是否启用。 */
    public boolean isEnabled() {
        return enabled;
    }

    /** 懒加载并返回组织属性映射。 */
    public MultivaluedHashMap<String, String> getAttributes(KeycloakSession session, Supplier<OrganizationModel> organizationModel) {
        return attributes.get(session, organizationModel);
    }

    /** 返回组织域名模型流。 */
    public Stream<OrganizationDomainModel> getDomains() {
        return domains.stream();
    }

    /** 返回已缓存的全部域名名称集合。 */
    public Set<String> getDomainNames() {
        return domainNames.keySet();
    }

    /** 向 LRU 缓存追加域名名称。 */
    public void addDomainName(String domainName) {
        domainNames.put(domainName, domainName);
    }

    /** 返回组织关联的身份提供者流。 */
    public Stream<IdentityProviderModel> getIdentityProviders() {
        return idps.stream();
    }
}
