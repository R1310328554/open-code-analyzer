/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.crl.infinispan;

import org.keycloak.cluster.ClusterProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.cache.CacheCrlProvider;
import org.keycloak.models.cache.infinispan.ClearCacheEvent;

import org.infinispan.Cache;

/**
 * 基于 Infinispan 的 CRL（证书吊销列表）模型缓存清理提供者。
 * <p>
 * 清空本地 CRL 缓存后，通过集群事件通知其他节点同步清除，保证多节点缓存一致性。
 */
public class InfinispanCacheCrlProvider implements CacheCrlProvider {

    /** 当前 Keycloak 会话，用于获取集群通知能力。 */
    private final KeycloakSession session;

    /** 存储 X509 CRL 条目的 Infinispan 缓存。 */
    private final Cache<String, X509CRLEntry> crlCache;

    /**
     * @param session  Keycloak 会话
     * @param crlCache CRL 专用 Infinispan 缓存
     */
    public InfinispanCacheCrlProvider(KeycloakSession session, Cache<String, X509CRLEntry> crlCache) {
        this.session = session;
        this.crlCache = crlCache;
    }

    /** {@inheritDoc} 清空本地 CRL 缓存并广播集群清除事件。 */
    @Override
    public void clearCache() {
        crlCache.clear();
        ClusterProvider cluster = session.getProvider(ClusterProvider.class);
        cluster.notify(InfinispanCacheCrlProviderFactory.CRL_CLEAR_CACHE_EVENTS, ClearCacheEvent.getInstance(), true);
    }

    /** {@inheritDoc} 无额外资源需释放。 */
    @Override
    public void close() {

    }
}
