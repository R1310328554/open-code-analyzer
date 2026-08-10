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
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.models.cache.infinispan.authorization.entities.CachedResourceServer;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.PolicyEnforcementMode;

/**
 * 资源服务器（ResourceServer）的 Infinispan 缓存适配器，实现 {@link ResourceServer} 与 {@link CachedModel}。
 * <p>
 * 读操作返回 {@link CachedResourceServer} 快照；写操作加载 DB 委托并注册资源服务器失效。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ResourceServerAdapter implements ResourceServer, CachedModel<ResourceServer> {
    /** 缓存的资源服务器快照实体。 */
    protected CachedResourceServer cached;
    /** 所属授权缓存会话。 */
    protected StoreFactoryCacheSession cacheSession;
    /** 数据库委托模型，写操作时懒加载。 */
    protected ResourceServer updated;

    /** 构造资源服务器缓存适配器。 */
    public ResourceServerAdapter(CachedResourceServer cached,
                                 StoreFactoryCacheSession cacheSession) {
        this.cached = cached;
        this.cacheSession = cacheSession;
    }

    /** 获取用于更新的数据库委托，首次调用时注册资源服务器失效。 */
    @Override
    public ResourceServer getDelegateForUpdate() {
        if (updated == null) {
            cacheSession.registerResourceServerInvalidation(cached.getId());
            updated = cacheSession.getResourceServerStoreDelegate().findById(cached.getId());
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
        updated = cacheSession.getResourceServerStoreDelegate().findById(cached.getId());
        if (updated == null) throw new IllegalStateException("Not found in database");
        return true;
    }


    @Override
    public String getId() {
        if (isUpdated()) return updated.getId();
        return cached.getId();
    }

    @Override
    public boolean isAllowRemoteResourceManagement() {
        if (isUpdated()) return updated.isAllowRemoteResourceManagement();
        return cached.isAllowRemoteResourceManagement();
    }

    @Override
    public void setAllowRemoteResourceManagement(boolean allowRemoteResourceManagement) {
        getDelegateForUpdate();
        updated.setAllowRemoteResourceManagement(allowRemoteResourceManagement);
    }

    @Override
    public PolicyEnforcementMode getPolicyEnforcementMode() {
        if (isUpdated()) return updated.getPolicyEnforcementMode();
        return cached.getPolicyEnforcementMode();
    }

    @Override
    public void setPolicyEnforcementMode(PolicyEnforcementMode enforcementMode) {
        getDelegateForUpdate();
        updated.setPolicyEnforcementMode(enforcementMode);

    }

    @Override
    public DecisionStrategy getDecisionStrategy() {
        if (isUpdated()) return updated.getDecisionStrategy();
        return cached.getDecisionStrategy();
    }

    @Override
    public void setDecisionStrategy(DecisionStrategy decisionStrategy) {
        getDelegateForUpdate();
        updated.setDecisionStrategy(decisionStrategy);
    }

    /** 资源服务器 ID 即关联客户端 ID。 */
    @Override
    public String getClientId() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceServer)) return false;

        ResourceServer that = (ResourceServer) o;
        return that.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }


}
