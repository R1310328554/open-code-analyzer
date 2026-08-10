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

package org.keycloak.models.cache.infinispan.authorization;

import org.keycloak.Config;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.cache.authorization.CachedStoreFactoryProvider;
import org.keycloak.models.cache.authorization.CachedStoreProviderFactory;
import org.keycloak.models.cache.infinispan.entities.Revisioned;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.AUTHORIZATION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.AUTHORIZATION_REVISIONS_CACHE_NAME;
import static org.keycloak.models.cache.infinispan.InfinispanCacheRealmProviderFactory.REALM_CLEAR_CACHE_EVENTS;

/**
 * 授权存储的 Infinispan 缓存工厂，实现 {@link CachedStoreProviderFactory}。
 * <p>
 * 创建 {@link StoreFactoryCacheSession}，并在集群中注册授权缓存失效、清空及 realm 清空事件监听器。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class InfinispanCacheStoreFactoryProviderFactory implements CachedStoreProviderFactory {

    private static final Logger log = Logger.getLogger(InfinispanCacheStoreFactoryProviderFactory.class);
    /** 集群广播：清空全部授权缓存条目。 */
    public static final String AUTHORIZATION_CLEAR_CACHE_EVENTS = "AUTHORIZATION_CLEAR_CACHE_EVENTS";
    /** 集群广播：按 key 失效授权缓存条目。 */
    public static final String AUTHORIZATION_INVALIDATION_EVENTS = "AUTHORIZATION_INVALIDATION_EVENTS";

    /** 全局共享的授权存储缓存管理器，懒加载初始化。 */
    protected volatile StoreFactoryCacheManager storeCache;

    /** {@inheritDoc} 创建绑定当前会话的授权缓存会话。 */
    @Override
    public CachedStoreFactoryProvider create(KeycloakSession session) {
        lazyInit(session);
        return new StoreFactoryCacheSession(storeCache, session);
    }

    /** 双重检查锁懒初始化授权缓存管理器及集群监听器。 */
    private void lazyInit(KeycloakSession session) {
        if (storeCache == null) {
            synchronized (this) {
                if (storeCache == null) {
                    var ispnProvider = session.getProvider(InfinispanConnectionProvider.class);
                    var cluster = session.getProvider(ClusterProvider.class);

                    Cache<String, Revisioned> cache = ispnProvider.getCache(AUTHORIZATION_CACHE_NAME);
                    Cache<String, Long> revisions = ispnProvider.getCache(AUTHORIZATION_REVISIONS_CACHE_NAME);
                    storeCache = new StoreFactoryCacheManager(cache, revisions);

                    cluster.registerListener(AUTHORIZATION_INVALIDATION_EVENTS, storeCache::onInvalidateEvent);
                    cluster.registerListener(AUTHORIZATION_CLEAR_CACHE_EVENTS, storeCache::onClearEvent);
                    cluster.registerListener(REALM_CLEAR_CACHE_EVENTS, storeCache::onClearEvent);
                    log.debug("Registered cluster listeners");
                }
            }
        }
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "default";
    }

}
