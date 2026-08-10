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

package org.keycloak.models.cache.infinispan;

import org.keycloak.Config;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.common.Profile;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.cache.UserCache;
import org.keycloak.models.cache.UserCacheProviderFactory;
import org.keycloak.models.cache.infinispan.entities.Revisioned;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.USER_REVISIONS_CACHE_NAME;

/**
 * 基于 Infinispan 的用户缓存 {@link UserCacheProviderFactory} 工厂实现。
 * <p>
 * 负责创建 {@link UserCacheSession}，并在集群中注册用户缓存失效与清空事件监听器。
 * 在无状态（stateless）模式下默认禁用。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class InfinispanUserCacheProviderFactory implements UserCacheProviderFactory, EnvironmentDependentProviderFactory {

    private static final Logger log = Logger.getLogger(InfinispanUserCacheProviderFactory.class);
    /** 集群广播：清空全部用户缓存条目。 */
    public static final String USER_CLEAR_CACHE_EVENTS = "USER_CLEAR_CACHE_EVENTS";
    /** 集群广播：按 key 失效用户缓存条目。 */
    public static final String USER_INVALIDATION_EVENTS = "USER_INVALIDATION_EVENTS";

    /** 全局共享的用户缓存管理器，懒加载初始化。 */
    protected volatile UserCacheManager userCache;

    /** {@inheritDoc} 创建绑定当前会话的用户缓存会话。 */
    @Override
    public UserCache create(KeycloakSession session) {
        lazyInit(session);
        return new UserCacheSession(userCache, session);
    }

    /** 双重检查锁懒初始化用户缓存管理器及集群监听器。 */
    private void lazyInit(KeycloakSession session) {
        if (userCache == null) {
            synchronized (this) {
                if (userCache == null) {
                    var ispnProvider = session.getProvider(InfinispanConnectionProvider.class);
                    var cluster = session.getProvider(ClusterProvider.class);

                    Cache<String, Revisioned> cache = ispnProvider.getCache(InfinispanConnectionProvider.USER_CACHE_NAME);
                    Cache<String, Long> revisions = ispnProvider.getCache(USER_REVISIONS_CACHE_NAME);
                    userCache = new UserCacheManager(cache, revisions);

                    cluster.registerListener(USER_INVALIDATION_EVENTS, userCache::onInvalidateEvent);
                    cluster.registerListener(USER_CLEAR_CACHE_EVENTS, userCache::onClearEvent);
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

    /** 无状态模式下默认不支持用户缓存，可通过配置显式启用。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return config.getBoolean("enabled", !Profile.isFeatureEnabled(Profile.Feature.STATELESS));
    }
}
