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

package org.keycloak.keys.infinispan;

import org.keycloak.Config;
import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.cache.CachePublicKeyProvider;
import org.keycloak.models.cache.CachePublicKeyProviderFactory;

import org.infinispan.Cache;

/**
 * Infinispan 公钥缓存清理 SPI 工厂。
 * <p>
 * 延迟初始化 keys 缓存并注册集群级失效/全量清除监听器，
 * 与 {@link InfinispanPublicKeyStorageProvider} 协同维护分布式公钥缓存一致性。
 */
public class InfinispanCachePublicKeyProviderFactory implements CachePublicKeyProviderFactory {

    /** SPI 提供者 ID。 */
    public static final String PROVIDER_ID = "infinispan";

    /** 单键公钥失效事件的集群广播主题。 */
    public static final String PUBLIC_KEY_STORAGE_INVALIDATION_EVENT = "PUBLIC_KEY_STORAGE_INVALIDATION_EVENT";

    /** 全量公钥缓存清除事件的集群广播主题。 */
    public static final String KEYS_CLEAR_CACHE_EVENTS = "KEYS_CLEAR_CACHE_EVENTS";

    /** 公钥条目缓存（延迟初始化）。 */
    private volatile Cache<String, PublicKeysEntry> keysCache;

    @Override
    public CachePublicKeyProvider create(KeycloakSession session) {
        lazyInit(session);
        return new InfinispanCachePublicKeyProvider(session, keysCache);
    }

    /** 双重检查锁定初始化缓存并注册集群失效/清除监听器。 */
    private void lazyInit(KeycloakSession session) {
        if (keysCache == null) {
            synchronized (this) {
                if (keysCache == null) {
                    this.keysCache = session.getProvider(InfinispanConnectionProvider.class).getCache(InfinispanConnectionProvider.KEYS_CACHE_NAME);

                    ClusterProvider cluster = session.getProvider(ClusterProvider.class);
                    cluster.registerListener(PUBLIC_KEY_STORAGE_INVALIDATION_EVENT, (ClusterEvent event) -> {

                        PublicKeyStorageInvalidationEvent invalidationEvent = (PublicKeyStorageInvalidationEvent) event;
                        keysCache.remove(invalidationEvent.getCacheKey());

                    });

                    cluster.registerListener(KEYS_CLEAR_CACHE_EVENTS, (ClusterEvent event) -> {

                        keysCache.clear();

                    });
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
        return PROVIDER_ID;
    }
}
