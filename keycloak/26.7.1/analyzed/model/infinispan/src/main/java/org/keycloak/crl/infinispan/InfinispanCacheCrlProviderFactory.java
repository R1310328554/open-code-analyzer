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

import org.keycloak.Config;
import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.cache.CacheCrlProvider;
import org.keycloak.models.cache.CacheCrlProviderFactory;

import org.infinispan.Cache;

/**
 * {@link CacheCrlProvider} 的 Infinispan 实现工厂。
 * <p>
 * 懒加载 CRL 缓存并注册集群监听器，在收到清除事件时同步清空各节点本地 CRL 缓存。
 */
public class InfinispanCacheCrlProviderFactory implements CacheCrlProviderFactory {

    /** 工厂在 Keycloak SPI 中的标识符。 */
    public static final String PROVIDER_ID = "infinispan";

    /** 集群广播 CRL 缓存清除时使用的事件主题名。 */
    public static final String CRL_CLEAR_CACHE_EVENTS = "CRL_CLEAR_CACHE_EVENTS";

    /** 懒加载初始化的 CRL Infinispan 缓存引用。 */
    private volatile Cache<String, X509CRLEntry> crlCache;

    /** {@inheritDoc} 创建绑定共享 CRL 缓存的提供者实例。 */
    @Override
    public CacheCrlProvider create(KeycloakSession session) {
        lazyInit(session);
        return new InfinispanCacheCrlProvider(session, crlCache);
    }

    /**
     * 双重检查锁定：首次访问时获取 CRL 缓存并注册集群清除监听器。
     *
     * @param session 用于解析 Infinispan 与集群提供者的会话
     */
    private void lazyInit(KeycloakSession session) {
        if (crlCache == null) {
            synchronized (this) {
                if (crlCache == null) {
                    crlCache = session.getProvider(InfinispanConnectionProvider.class).getCache(InfinispanConnectionProvider.CRL_CACHE_NAME);
                    ClusterProvider cluster = session.getProvider(ClusterProvider.class);

                    cluster.registerListener(CRL_CLEAR_CACHE_EVENTS, (ClusterEvent event) -> {
                        crlCache.clear();
                    });
                }
            }
        }
    }

    /** {@inheritDoc} 无额外配置项。 */
    @Override
    public void init(Config.Scope config) {

    }

    /** {@inheritDoc} 无后置初始化逻辑。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** {@inheritDoc} 缓存由连接提供者统一管理生命周期。 */
    @Override
    public void close() {

    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
