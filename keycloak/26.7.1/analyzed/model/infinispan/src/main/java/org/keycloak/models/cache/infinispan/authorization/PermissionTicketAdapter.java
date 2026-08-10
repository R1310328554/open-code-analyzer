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

import org.keycloak.authorization.model.CachedModel;
import org.keycloak.authorization.model.PermissionTicket;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.models.cache.infinispan.authorization.entities.CachedPermissionTicket;

/**
 * 权限票据（Permission Ticket）的 Infinispan 缓存适配器。
 * <p>
 * 读操作返回 {@link CachedPermissionTicket} 快照；写操作加载 DB 委托并注册票据失效。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PermissionTicketAdapter implements PermissionTicket, CachedModel<PermissionTicket> {

    /** 缓存的权限票据快照。 */
    protected CachedPermissionTicket cached;
    /** 所属授权缓存会话。 */
    protected StoreFactoryCacheSession cacheSession;
    /** 数据库委托模型，写操作时懒加载。 */
    protected PermissionTicket updated;

    /** 构造权限票据缓存适配器。 */
    public PermissionTicketAdapter(CachedPermissionTicket cached, StoreFactoryCacheSession cacheSession) {
        this.cached = cached;
        this.cacheSession = cacheSession;
    }

    /** 获取用于更新的数据库委托，首次调用时注册权限票据失效。 */
    @Override
    public PermissionTicket getDelegateForUpdate() {
        if (updated == null) {
            ResourceServer resourceServer = cacheSession.getResourceServerStoreDelegate().findById(cached.getResourceServerId());
            updated = cacheSession.getPermissionTicketStoreDelegate().findById(resourceServer, cached.getId());
            if (updated == null) throw new IllegalStateException("Not found in database");
            cacheSession.registerPermissionTicketInvalidation(cached.getId(), cached.getOwner(), cached.getRequester(), cached.getResourceId(), updated.getResource().getName(), cached.getScopeId(), cached.getResourceServerId());
        }
        return updated;
    }

    /** 缓存条目是否已被标记失效。 */
    protected boolean invalidated;

    /** 标记缓存条目为失效状态。 */
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

    /** 判断是否需要从数据库读取（已更新或已失效）。 */
    protected boolean isUpdated() {
        if (updated != null) return true;
        if (!invalidated) return false;
        ResourceServer resourceServer = cacheSession.getResourceServerStoreDelegate().findById(cached.getResourceServerId());
        updated = cacheSession.getPermissionTicketStoreDelegate().findById(resourceServer, cached.getId());
        if (updated == null) throw new IllegalStateException("Not found in database");
        return true;
    }


    @Override
    public String getId() {
        if (isUpdated()) return updated.getId();
        return cached.getId();
    }

    @Override
    public String getOwner() {
        if (isUpdated()) return updated.getOwner();
        return cached.getOwner();
    }

    @Override
    public String getRequester() {
        if (isUpdated()) return updated.getRequester();
        return cached.getRequester();
    }

    @Override
    public boolean isGranted() {
        if (isUpdated()) return updated.isGranted();
        return cached.isGranted();
    }

    @Override
    public Long getCreatedTimestamp() {
        if (isUpdated()) return updated.getCreatedTimestamp();
        return cached.getCreatedTimestamp();
    }

    @Override
    public Long getGrantedTimestamp() {
        if (isUpdated()) return updated.getGrantedTimestamp();
        return cached.getGrantedTimestamp();
    }

    @Override
    public void setGrantedTimestamp(Long millis) {
        getDelegateForUpdate();
        cacheSession.registerPermissionTicketInvalidation(cached.getId(), cached.getOwner(), cached.getRequester(), cached.getResourceId(), updated.getResource().getName(), cached.getScopeId(), cached.getResourceServerId());
        updated.setGrantedTimestamp(millis);
    }

    @Override
    public ResourceServer getResourceServer() {
        return cacheSession.getResourceServerStore().findById(cached.getResourceServerId());
    }

    @Override
    public Policy getPolicy() {
        if (isUpdated()) return updated.getPolicy();
        return cacheSession.getPolicyStore().findById(cacheSession.getResourceServerStore().findById(cached.getResourceServerId()), cached.getPolicy());
    }

    @Override
    public void setPolicy(Policy policy) {
        getDelegateForUpdate();
        cacheSession.registerPermissionTicketInvalidation(cached.getId(), cached.getOwner(), cached.getRequester(), cached.getResourceId(), updated.getResource().getName(), cached.getScopeId(), cached.getResourceServerId());
        updated.setPolicy(policy);
    }

    @Override
    public Resource getResource() {
        return cacheSession.getResourceStore().findById(getResourceServer(), cached.getResourceId());
    }

    @Override
    public Scope getScope() {
        return cacheSession.getScopeStore().findById(getResourceServer(), cached.getScopeId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
