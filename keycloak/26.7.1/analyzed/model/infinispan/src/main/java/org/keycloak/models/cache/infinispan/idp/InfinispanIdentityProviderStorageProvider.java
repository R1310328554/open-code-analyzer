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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.common.Profile;
import org.keycloak.models.IdentityProviderCapability;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderQuery;
import org.keycloak.models.IdentityProviderStorageProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.cache.CacheRealmProvider;
import org.keycloak.models.cache.infinispan.CachedCount;
import org.keycloak.models.cache.infinispan.RealmCacheManager;
import org.keycloak.models.cache.infinispan.RealmCacheSession;
import org.keycloak.organization.OrganizationProvider;

import static org.keycloak.models.IdentityProviderStorageProvider.LoginFilter.getLoginPredicate;

/**
 * 基于 Infinispan 领域缓存的身份提供者存储提供者。
 * <p>
 * 在 JPA 委托之上增加读缓存与写失效逻辑，涵盖 IdP 实体、映射器、
 * 数量统计、登录可用列表及按组织查询等场景；组织功能开启时会
 * 根据组织启用状态动态调整 IdP 的 {@code isEnabled()} 语义。
 */
public class InfinispanIdentityProviderStorageProvider implements IdentityProviderStorageProvider {

    /** IdP 数量统计缓存键后缀。 */
    private static final String IDP_COUNT_KEY_SUFFIX = ".idp.count";
    /** 按别名查询 IdP 的缓存键后缀。 */
    private static final String IDP_ALIAS_KEY_SUFFIX = ".idp.alias";
    /** 按组织 ID 查询 IdP 列表的缓存键后缀。 */
    private static final String IDP_ORG_ID_KEY_SUFFIX = ".idp.orgId";
    /** 登录可用 IdP 列表的缓存键后缀。 */
    private static final String IDP_LOGIN_SUFFIX = ".idp.login";
    /** 身份联合是否启用的缓存键后缀。 */
    private static final String IDP_ENABLED_KEY_SUFFIX = ".idp.enabled";

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** 底层 JPA 身份提供者存储委托。 */
    private final IdentityProviderStorageProvider idpDelegate;
    /** 领域缓存会话，用于读写 Infinispan 缓存。 */
    private final RealmCacheSession realmCache;
    /** 启动时的缓存修订计数，用于版本化写入。 */
    private final long startupRevision;

    /** 构造带缓存层的身份提供者存储提供者。 */
    public InfinispanIdentityProviderStorageProvider(KeycloakSession session) {
        this.session = session;
        this.idpDelegate = session.getProvider(IdentityProviderStorageProvider.class, "jpa");
        this.realmCache = (RealmCacheSession) session.getProvider(CacheRealmProvider.class);
        this.startupRevision = realmCache.getCache().getCurrentCounter();
    }

    /** 生成领域 IdP 数量统计的缓存键。 */
    private static String cacheKeyIdpCount(RealmModel realm) {
        return realm.getId() + IDP_COUNT_KEY_SUFFIX;
    }

    /** 生成按别名查询 IdP 的缓存键。 */
    private static String cacheKeyIdpAlias(RealmModel realm, String alias) {
        return realm.getId() + "." + alias + IDP_ALIAS_KEY_SUFFIX;
    }

    /** 生成按 IdP 别名与映射器名称查询映射器的缓存键。 */
    private static String cacheKeyIdpMapperAliasName(RealmModel realm, String alias, String name) {
        return realm.getId() + "." + alias + IDP_ALIAS_KEY_SUFFIX + "." + name;
    }

    /** 生成按组织 ID 查询 IdP 列表的缓存键。 */
    public static String cacheKeyOrgId(RealmModel realm, String orgId) {
        return realm.getId() + "." + orgId + IDP_ORG_ID_KEY_SUFFIX;
    }

    /** 生成登录场景 IdP 列表的缓存键。 */
    public static String cacheKeyForLogin(RealmModel realm, FetchMode mode) {
        return realm.getId() + IDP_LOGIN_SUFFIX + "." + mode;
    }

    /** 生成身份联合启用状态探测的缓存键。 */
    public static String cacheKeyIsEnabled(RealmModel realm) {
        return realm.getId() + IDP_ENABLED_KEY_SUFFIX;
    }

    /** 创建 IdP 并注册数量与登录列表缓存失效。 */
    @Override
    public IdentityProviderModel create(IdentityProviderModel model) {
        registerCountInvalidation();
        registerIDPLoginInvalidation(model);
        return idpDelegate.create(model);
    }

    /** 更新 IdP；别名变更时需先按 ID 查原记录以正确失效旧缓存键。 */
    @Override
    public void update(IdentityProviderModel model) {
        // 别名可能被修改，需按 ID 查原记录以获取原始 alias
        IdentityProviderModel idpById = getById(model.getInternalId());
        registerIDPInvalidation(idpById);
        registerIDPLoginInvalidationOnUpdate(idpById, model);
        idpDelegate.update(model);
    }

    /** 按别名删除 IdP 并失效相关缓存条目。 */
    @Override
    public boolean remove(String alias) {
        String cacheKey = cacheKeyIdpAlias(getRealm(), alias);
        IdentityProviderModel storedIdp = idpDelegate.getByAlias(alias);
        if (isInvalid(cacheKey)) {
            // 缓存已失效时从数据库查 IdP 以获取 internalId 用于失效
            registerIDPInvalidation(storedIdp);
        } else {
            CachedIdentityProvider cached = realmCache.getCache().get(cacheKey, CachedIdentityProvider.class);
            if (cached != null) {
                registerIDPInvalidation(cached.getIdentityProvider());
            }
        }
        registerCountInvalidation();
        registerIDPLoginInvalidation(storedIdp);
        return idpDelegate.remove(alias);
    }

    /** 删除全部 IdP（通常仅在领域删除时调用，无需逐条失效）。 */
    @Override
    public void removeAll() {
        registerCountInvalidation();
        // removeAll() 目前仅在领域删除时调用，无需逐条失效缓存
        idpDelegate.removeAll();
    }

    /** 按内部 ID 查询 IdP，优先读缓存。 */
    @Override
    public IdentityProviderModel getById(String internalId) {
        if (internalId == null) return null;
        CachedIdentityProvider cached = realmCache.getCache().get(internalId, CachedIdentityProvider.class);
        String realmId = getRealm().getId();
        if (cached != null && !cached.getRealm().equals(realmId)) {
            cached = null;
        }

        if (cached == null) {
            long loaded = realmCache.getCache().getCurrentRevision(internalId);
            IdentityProviderModel model = idpDelegate.getById(internalId);
            if (model == null) return null;
            if (isInvalid(internalId)) return createOrganizationAwareIdentityProviderModel(model);
            cached = new CachedIdentityProvider(loaded, getRealm(), internalId, model);
            realmCache.getCache().addRevisioned(cached, realmCache.getStartupRevision());
        } else if (isInvalid(internalId)) {
            return createOrganizationAwareIdentityProviderModel(idpDelegate.getById(internalId));
        }
        return createOrganizationAwareIdentityProviderModel(cached.getIdentityProvider());
    }

    /** 按别名查询 IdP，优先读缓存。 */
    @Override
    public IdentityProviderModel getByAlias(String alias) {
        String cacheKey = cacheKeyIdpAlias(getRealm(), alias);

        if (isInvalid(cacheKey)) {
            return createOrganizationAwareIdentityProviderModel(idpDelegate.getByAlias(alias));
        }

        CachedIdentityProvider cached = realmCache.getCache().get(cacheKey, CachedIdentityProvider.class);

        if (cached == null) {
            long loaded = realmCache.getCache().getCurrentRevision(cacheKey);
            IdentityProviderModel model = idpDelegate.getByAlias(alias);
            if (model == null) {
                return null;
            }
            cached = new CachedIdentityProvider(loaded, getRealm(), cacheKey, model);
            realmCache.getCache().addRevisioned(cached, realmCache.getStartupRevision());
        }

        return createOrganizationAwareIdentityProviderModel(cached.getIdentityProvider());
    }

    /** 判断领域是否启用了身份联合（存在支持用户链接的 IdP）。 */
    @Override
    public boolean isIdentityFederationEnabled() {
        String cacheKey = cacheKeyIsEnabled(getRealm());

        if (isInvalid(cacheKey)) {
            return idpDelegate.isIdentityFederationEnabled();
        }

        CachedCount cached = realmCache.getCache().get(cacheKey, CachedCount.class);

        if (cached == null) {
            long loaded = realmCache.getCache().getCurrentRevision(cacheKey);
            long count = idpDelegate.getAllStream(IdentityProviderQuery.capability(IdentityProviderCapability.USER_LINKING), 0, 1).count();
            cached = new CachedCount(loaded, getRealm(), cacheKey, count);
            realmCache.getCache().addRevisioned(cached, realmCache.getStartupRevision());
        }

        return cached.getCount() > 0;
    }

    /** 按组织 ID 分页查询 IdP，结果以内部 ID 列表缓存。 */
    @Override
    public Stream<IdentityProviderModel> getByOrganization(String orgId, Integer first, Integer max) {
        RealmModel realm = getRealm();
        String cacheKey = cacheKeyOrgId(realm, orgId);

        // 检查该键或组织本身是否已失效
        if (isInvalid(cacheKey) || isInvalid(orgId)) {
            return idpDelegate.getByOrganization(orgId, first, max).map(this::createOrganizationAwareIdentityProviderModel);
        }

        RealmCacheManager cache = realmCache.getCache();
        IdentityProviderListQuery query = cache.get(cacheKey, IdentityProviderListQuery.class);
        String searchKey = Optional.ofNullable(first).orElse(-1) + "." + Optional.ofNullable(max).orElse(-1);
        Set<String> cached;

        if (query == null) {
            // 尚未缓存
            long loaded = cache.getCurrentRevision(cacheKey);
            cached = idpDelegate.getByOrganization(orgId, first, max).map(IdentityProviderModel::getInternalId).collect(Collectors.toSet());
            query = new IdentityProviderListQuery(loaded, cacheKey, realm, searchKey, cached);
            cache.addRevisioned(query, startupRevision);
        } else {
            cached = query.getIDPs(searchKey);
            if (cached == null) {
                // 已有缓存条目但当前分页条件尚未缓存
                cache.invalidateObject(cacheKey);
                long loaded = cache.getCurrentRevision(cacheKey);
                cached = idpDelegate.getByOrganization(orgId, first, max).map(IdentityProviderModel::getInternalId)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                query = new IdentityProviderListQuery(loaded, cacheKey, realm, searchKey, cached, query);
                cache.addRevisioned(query, cache.getCurrentCounter());
            }
        }

        Set<IdentityProviderModel> identityProviders = new LinkedHashSet<>();
        for (String id : cached) {
            IdentityProviderModel idp = session.identityProviders().getById(id);
            if (idp == null) {
                realmCache.registerInvalidation(cacheKey);
                return idpDelegate.getByOrganization(orgId, first, max).map(this::createOrganizationAwareIdentityProviderModel);
            }
            identityProviders.add(idp);
        }

        return identityProviders.stream();
    }

    /** 查询登录页可用的 IdP 列表，按获取模式与组织 ID 缓存。 */
    @Override
    public Stream<IdentityProviderModel> getForLogin(FetchMode mode, String organizationId) {
        String cacheKey = cacheKeyForLogin(getRealm(), mode);

        if (isInvalid(cacheKey)) {
            return idpDelegate.getForLogin(mode, organizationId).map(this::createOrganizationAwareIdentityProviderModel);
        }

        RealmCacheManager cache = realmCache.getCache();
        IdentityProviderListQuery query = cache.get(cacheKey, IdentityProviderListQuery.class);
        String searchKey = organizationId != null ? organizationId : "";
        Set<String> cached;

        if (query == null) {
            // 尚未缓存
            long loaded = cache.getCurrentRevision(cacheKey);
            cached = idpDelegate.getForLogin(mode, organizationId).map(IdentityProviderModel::getInternalId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            query = new IdentityProviderListQuery(loaded, cacheKey, getRealm(), searchKey, cached);
            cache.addRevisioned(query, startupRevision);
        } else {
            cached = query.getIDPs(searchKey);
            if (cached == null) {
                // 已有缓存条目但当前组织条件尚未缓存
                cache.invalidateObject(cacheKey);
                long loaded = cache.getCurrentRevision(cacheKey);
                cached = idpDelegate.getForLogin(mode, organizationId).map(IdentityProviderModel::getInternalId).collect(Collectors.toSet());
                query = new IdentityProviderListQuery(loaded, cacheKey, getRealm(), searchKey, cached, query);
                cache.addRevisioned(query, cache.getCurrentCounter());
            }
        }

        Set<IdentityProviderModel> identityProviders = new LinkedHashSet<>();
        for (String id : cached) {
            IdentityProviderModel idp = session.identityProviders().getById(id);
            if (idp == null) {
                realmCache.registerInvalidation(cacheKey);
                return idpDelegate.getForLogin(mode, organizationId).map(this::createOrganizationAwareIdentityProviderModel);
            }
            identityProviders.add(idp);
        }

        return identityProviders.stream();
    }

    /** 按认证流 ID 查询 IdP 别名流（不经缓存）。 */
    @Override
    public Stream<String> getByFlow(String flowId, String search, Integer first, Integer max) {
        return idpDelegate.getByFlow(flowId, search, first, max);
    }

    /** 通用条件查询 IdP 流（不经列表缓存，逐条走 getById 缓存）。 */
    @Override
    public Stream<IdentityProviderModel> getAllStream(IdentityProviderQuery query, Integer first, Integer max) {
        return idpDelegate.getAllStream(query, first, max).map(this::createOrganizationAwareIdentityProviderModel);
    }

    /** 返回领域内 IdP 总数，优先读计数缓存。 */
    @Override
    public long count() {
        String cacheKey = cacheKeyIdpCount(getRealm());
        CachedCount cached = realmCache.getCache().get(cacheKey, CachedCount.class);

        // 缓存命中且未失效
        if (cached != null && !isInvalid(cacheKey)) {
            return cached.getCount();
        }

        long loaded = realmCache.getCache().getCurrentRevision(cacheKey);
        long count = idpDelegate.count();
        cached = new CachedCount(loaded, getRealm(), cacheKey, count);
        realmCache.getCache().addRevisioned(cached, realmCache.getStartupRevision());

        return count;
    }

    /** 关闭底层 JPA 委托。 */
    @Override
    public void close() {
        idpDelegate.close();
    }

    /** 创建 IdP 映射器（不经缓存）。 */
    @Override
    public IdentityProviderMapperModel createMapper(IdentityProviderMapperModel model) {
        return idpDelegate.createMapper(model);
    }

    /** 更新 IdP 映射器并失效相关缓存。 */
    @Override
    public void updateMapper(IdentityProviderMapperModel model) {
        registerIDPMapperInvalidation(model);
        idpDelegate.updateMapper(model);
    }

    /** 删除 IdP 映射器并失效相关缓存。 */
    @Override
    public boolean removeMapper(IdentityProviderMapperModel model) {
        registerIDPMapperInvalidation(model);
        return idpDelegate.removeMapper(model);
    }

    /** 删除全部映射器（通常仅在领域删除时调用）。 */
    @Override
    public void removeAllMappers() {
        // removeAllMappers() 目前仅在领域删除时调用，无需逐条失效
        idpDelegate.removeAllMappers();
    }

    /** 按内部 ID 查询 IdP 映射器，优先读缓存。 */
    @Override
    public IdentityProviderMapperModel getMapperById(String id) {
        CachedIdentityProviderMapper cached = realmCache.getCache().get(id, CachedIdentityProviderMapper.class);
        String realmId = getRealm().getId();
        if (cached != null && !cached.getRealm().equals(realmId)) {
            cached = null;
        }

        if (cached == null) {
            long loaded = realmCache.getCache().getCurrentRevision(id);
            IdentityProviderMapperModel model = idpDelegate.getMapperById(id);
            if (model == null) return null;
            if (isInvalid(id)) return model;
            cached = new CachedIdentityProviderMapper(loaded, getRealm(), id, model);
            realmCache.getCache().addRevisioned(cached, realmCache.getStartupRevision());
        } else if (isInvalid(id)) {
            return idpDelegate.getMapperById(id);
        }
        return cached.getIdentityProviderMapper();
    }

    /** 按 IdP 别名与映射器名称查询，优先读缓存。 */
    @Override
    public IdentityProviderMapperModel getMapperByName(String identityProviderAlias, String name) {
        String cacheKey = cacheKeyIdpMapperAliasName(getRealm(), identityProviderAlias, name);

        if (isInvalid(cacheKey)) {
            return idpDelegate.getMapperByName(identityProviderAlias, name);
        }

        CachedIdentityProviderMapper cached = realmCache.getCache().get(cacheKey, CachedIdentityProviderMapper.class);

        if (cached == null) {
            long loaded = realmCache.getCache().getCurrentRevision(cacheKey);
            IdentityProviderMapperModel model = idpDelegate.getMapperByName(identityProviderAlias, name);
            if (model == null) return null;
            cached = new CachedIdentityProviderMapper(loaded, getRealm(), cacheKey, model);
            realmCache.getCache().addRevisioned(cached, realmCache.getStartupRevision());
        }

        return cached.getIdentityProviderMapper();
    }

    /** 条件分页查询映射器流（不经缓存）。 */
    @Override
    public Stream<IdentityProviderMapperModel> getMappersStream(Map<String, String> options, Integer first, Integer max) {
        return idpDelegate.getMappersStream(options, first, max);
    }

    /** 按 IdP 别名查询其全部映射器（不经缓存）。 */
    @Override
    public Stream<IdentityProviderMapperModel> getMappersByAliasStream(String identityProviderAlias) {
        return idpDelegate.getMappersByAliasStream(identityProviderAlias);
    }

    /** 注册单个 IdP 实体相关缓存键的失效。 */
    private void registerIDPInvalidation(IdentityProviderModel idp) {
        realmCache.registerInvalidation(idp.getInternalId());
        realmCache.registerInvalidation(cacheKeyIdpAlias(getRealm(), idp.getAlias()));
    }

    /** 注册 IdP 数量与身份联合启用状态缓存的失效。 */
    private void registerCountInvalidation() {
        realmCache.registerInvalidation(cacheKeyIdpCount(getRealm()));
        realmCache.registerInvalidation(cacheKeyIsEnabled(getRealm()));
    }

    /** 注册 IdP 映射器相关缓存键的失效。 */
    private void registerIDPMapperInvalidation(IdentityProviderMapperModel mapper) {
        if (mapper.getId() == null) {
            throw new ModelException("Identity Provider Mapper does not exist");
        }
        realmCache.registerInvalidation(mapper.getId());
        realmCache.registerInvalidation(cacheKeyIdpMapperAliasName(getRealm(), mapper.getIdentityProviderAlias(), mapper.getName()));
    }

    /** 若 IdP 符合登录条件，则失效全部登录列表缓存。 */
    private void registerIDPLoginInvalidation(IdentityProviderModel idp) {
        // 仅当 IdP 可作为登录选项时才失效登录缓存
        if (getLoginPredicate().test(idp)) {
            for (FetchMode mode : FetchMode.values()) {
                realmCache.registerInvalidation(cacheKeyForLogin(getRealm(), mode));
            }
        }
    }

    /**
     * 在 IdP 更新时注册登录列表缓存的失效。
     * <p>
     * 以下情况<strong>不</strong>失效登录缓存：
     * <ul>
     *     <li>IdP 当前不可用于登录，且更新后仍不可用于登录；</li>
     *     <li>IdP 当前可用于登录，且更新后仍可用于登录，且组织关联未变更。</li>
     * </ul>
     * 其余场景均需失效登录缓存。
     *
     * @param original 更新前的身份提供者模型
     * @param updated 更新后的身份提供者模型
     */
    private void registerIDPLoginInvalidationOnUpdate(IdentityProviderModel original, IdentityProviderModel updated) {
        // IdP 当前不可登录且更新后仍不可登录——无需失效
        if (!getLoginPredicate().test(original) && !getLoginPredicate().test(updated)) {
            return;
        }
        // IdP 当前可登录且更新后仍可登录，且组织关联未变——无需失效
        if (getLoginPredicate().test(original) && getLoginPredicate().test(updated)
                && Objects.equals(original.getOrganizationId(), updated.getOrganizationId())) {
            return;
        }

        // 其余场景失效全部登录模式下的列表缓存
        for (FetchMode mode : FetchMode.values()) {
            realmCache.registerInvalidation(cacheKeyForLogin(getRealm(), mode));
        }
    }

    /** 返回当前会话绑定的领域，未绑定时抛出异常。 */
    private RealmModel getRealm() {
        RealmModel realm = session.getContext().getRealm();
        if (realm == null) {
            throw new IllegalArgumentException("Session not bound to a realm");
        }
        return realm;
    }

    /** 判断指定缓存键是否已被标记为失效。 */
    private boolean isInvalid(String cacheKey) {
        return realmCache.isInvalid(cacheKey);
    }

    /**
     * 返回感知组织启用状态的 IdP 模型包装。
     * <p>
     * 组织功能开启且 IdP 绑定组织时，仅当组织与组织提供者均启用时才视为启用。
     */
    private IdentityProviderModel createOrganizationAwareIdentityProviderModel(IdentityProviderModel idp) {
        if (!Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION)) return idp;
        return new IdentityProviderModel(idp) {
            @Override
            public boolean isEnabled() {
                // IdP 绑定了组织时，需额外校验组织启用状态
                if (getOrganizationId() != null) {
                    OrganizationProvider provider = session.getProvider(OrganizationProvider.class);
                    OrganizationModel org = provider == null ? null : provider.getById(getOrganizationId());
                    return org != null && provider.isEnabled() && org.isEnabled() && super.isEnabled();
                }
                return super.isEnabled();
            }
        };
    }
}
