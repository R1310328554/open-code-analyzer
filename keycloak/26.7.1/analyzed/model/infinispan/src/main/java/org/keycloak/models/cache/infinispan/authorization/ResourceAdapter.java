/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models.cache.infinispan.authorization;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.keycloak.authorization.model.CachedModel;
import org.keycloak.authorization.model.PermissionTicket;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.store.PermissionTicketStore;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.cache.infinispan.LazyModel;
import org.keycloak.models.cache.infinispan.authorization.entities.CachedResource;

/**
 * 授权资源（Resource）的 Infinispan 缓存适配器，实现 {@link Resource} 与 {@link CachedModel}。
 * <p>
 * 读操作返回 {@link CachedResource} 快照；写操作加载 DB 委托并注册资源失效。
 * 作用域、URI 与属性在首次访问时懒解析并缓存。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ResourceAdapter implements Resource, CachedModel<Resource> {

    /** 惰性加载资源 DB 模型的供应器。 */
    private final Supplier<Resource> modelSupplier;
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** 缓存的资源快照实体。 */
    protected final CachedResource cached;
    /** 所属授权缓存会话。 */
    protected final StoreFactoryCacheSession cacheSession;
    /** 数据库委托模型，写操作时懒加载。 */
    protected Resource updated;

    /** 构造资源缓存适配器。 */
    public ResourceAdapter(CachedResource cached, StoreFactoryCacheSession cacheSession) {
        this.cached = cached;
        this.cacheSession = cacheSession;
        this.session = cacheSession.session;
        this.modelSupplier = new LazyModel<>(this::getResourceModel);
    }

    /** 获取用于更新的数据库委托，首次调用时注册资源失效。 */
    @Override
    public Resource getDelegateForUpdate() {
        if (updated == null) {
            updated = modelSupplier.get();
            cacheSession.registerResourceInvalidation(cached.getId(), cached.getName(), cached.getType(), cached.getUris(session, modelSupplier), cached.getScopesIds(session, modelSupplier), cached.getResourceServerId(), cached.getOwner());
            if (updated == null) throw new IllegalStateException("Not found in database");
        }
        return updated;
    }

    /** 缓存条目是否已被标记失效。 */
    protected boolean invalidated;

    /** 标记本地缓存条目失效（不立即加载 DB）。 */
    protected void invalidateFlag() {
        invalidated = true;

    }

    @Override
    public void invalidate() {
        invalidated = true;
        getDelegateForUpdate();
    }

    @Override
    public long getCacheTimestamp() {
        return cached.getCacheTimestamp();
    }

    /** 判断是否已切换到 DB 委托（更新或失效后重载）。 */
    protected boolean isUpdated() {
        if (updated != null) return true;
        if (!invalidated) return false;
        updated = cacheSession.getResourceStoreDelegate().findById(getResourceServer(), cached.getId());
        if (updated == null) throw new IllegalStateException("Not found in database");
        return true;
    }


    @Override
    public String getId() {
        if (isUpdated()) return updated.getId();
        return cached.getId();
    }

    @Override
    public String getName() {
        if (isUpdated()) return updated.getName();
        return cached.getName();
    }

    @Override
    public void setName(String name) {
        getDelegateForUpdate();
        cacheSession.registerResourceInvalidation(cached.getId(), name, cached.getType(), cached.getUris(session, modelSupplier), cached.getScopesIds(session, modelSupplier), cached.getResourceServerId(), cached.getOwner());
        updated.setName(name);
    }

    @Override
    public String getDisplayName() {
        if (isUpdated()) return updated.getDisplayName();
        return cached.getDisplayName();
    }

    @Override
    public void setDisplayName(String name) {
        getDelegateForUpdate();
        cacheSession.registerResourceInvalidation(cached.getId(), cached.getName(), cached.getType(), cached.getUris(session, modelSupplier), cached.getScopesIds(session, modelSupplier), cached.getResourceServerId(), cached.getOwner());
        updated.setDisplayName(name);
    }

    @Override
    public String getIconUri() {
        if (isUpdated()) return updated.getIconUri();
        return cached.getIconUri();
    }

    @Override
    public void setIconUri(String iconUri) {
        getDelegateForUpdate();
        updated.setIconUri(iconUri);

    }

    @Override
    public ResourceServer getResourceServer() {
        return cacheSession.getResourceServerStore().findById(cached.getResourceServerId());
    }

    @Override
    public Set<String> getUris() {
        if (isUpdated()) return updated.getUris();
        return cached.getUris(session, modelSupplier);
    }

    @Override
    public void updateUris(Set<String> uris) {
        getDelegateForUpdate();
        cacheSession.registerResourceInvalidation(cached.getId(), cached.getName(), cached.getType(), uris, cached.getScopesIds(session, modelSupplier), cached.getResourceServerId(), cached.getOwner());
        updated.updateUris(uris);
    }

    @Override
    public String getType() {
        if (isUpdated()) return updated.getType();
        return cached.getType();
    }

    @Override
    public void setType(String type) {
        getDelegateForUpdate();
        cacheSession.registerResourceInvalidation(cached.getId(), cached.getName(), type, cached.getUris(session, modelSupplier), cached.getScopesIds(session, modelSupplier), cached.getResourceServerId(), cached.getOwner());
        updated.setType(type);

    }

    /** 已解析的作用域列表，懒加载后不可变缓存。 */
    protected List<Scope> scopes;

    /** 返回资源关联的作用域，未更新时从缓存 ID 列表懒解析。 */
    @Override
    public List<Scope> getScopes() {
        if (isUpdated()) return updated.getScopes();
        if (scopes != null) return scopes;
        scopes = new LinkedList<>();
        for (String scopeId : cached.getScopesIds(session, modelSupplier)) {
            scopes.add(cacheSession.getScopeStore().findById(getResourceServer(), scopeId));
        }
        return scopes = Collections.unmodifiableList(scopes);
    }

    @Override
    public String getOwner() {
        if (isUpdated()) return updated.getOwner();
        return cached.getOwner();
    }

    @Override
    public boolean isOwnerManagedAccess() {
        if (isUpdated()) return updated.isOwnerManagedAccess();
        return cached.isOwnerManagedAccess();
    }

    @Override
    public void setOwnerManagedAccess(boolean ownerManagedAccess) {
        getDelegateForUpdate();
        cacheSession.registerResourceInvalidation(cached.getId(), cached.getName(), cached.getType(), cached.getUris(session, modelSupplier), cached.getScopesIds(session, modelSupplier), cached.getResourceServerId(), cached.getOwner());
        updated.setOwnerManagedAccess(ownerManagedAccess);
    }

    /** 更新作用域集合，移除的作用域会级联删除关联权限票据与策略引用。 */
    @Override
    public void updateScopes(Set<Scope> scopes) {
        Resource updated = getDelegateForUpdate();

        for (Scope scope : updated.getScopes()) {
            if (!scopes.contains(scope)) {
                // 移除作用域时清理关联权限票据
                PermissionTicketStore permissionStore = cacheSession.getPermissionTicketStore();
                List<PermissionTicket> permissions = permissionStore.findByScope(getResourceServer(), scope);

                for (PermissionTicket permission : permissions) {
                    permissionStore.delete(permission.getId());
                }
            }
        }

        PolicyStore policyStore = cacheSession.getPolicyStore();

        for (Scope scope : updated.getScopes()) {
            if (!scopes.contains(scope)) {
                policyStore.findByResource(getResourceServer(), this, policy -> policy.removeScope(scope));
            }
        }

        cacheSession.registerResourceInvalidation(cached.getId(), cached.getName(), cached.getType(), cached.getUris(session, modelSupplier), scopes.stream().map(scope1 -> scope1.getId()).collect(Collectors.toSet()), cached.getResourceServerId(), cached.getOwner());
        updated.updateScopes(scopes);
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        if (updated != null) return updated.getAttributes();
        return cached.getAttributes(session, modelSupplier);
    }

    @Override
    public String getSingleAttribute(String name) {
        if (updated != null) return updated.getSingleAttribute(name);

        List<String> values = cached.getAttributes(session, modelSupplier).getOrDefault(name, Collections.emptyList());

        if (values.isEmpty()) {
            return null;
        }

        return values.get(0);
    }

    @Override
    public List<String> getAttribute(String name) {
        if (updated != null) return updated.getAttribute(name);

        List<String> values = cached.getAttributes(session, modelSupplier).getOrDefault(name, Collections.emptyList());

        if (values.isEmpty()) {
            return null;
        }

        return Collections.unmodifiableList(values);
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        getDelegateForUpdate();
        updated.setAttribute(name, values);
    }

    @Override
    public void removeAttribute(String name) {
        getDelegateForUpdate();
        updated.removeAttribute(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource)) return false;

        Resource that = (Resource) o;
        return that.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /** 从数据库加载资源模型（供 LazyModel 使用）。 */
    private Resource getResourceModel() {
        return cacheSession.getResourceStoreDelegate().findById(getResourceServer(), cached.getId());
    }
}
