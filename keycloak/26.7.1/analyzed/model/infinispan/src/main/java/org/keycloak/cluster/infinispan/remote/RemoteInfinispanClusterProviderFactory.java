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

package org.keycloak.cluster.infinispan.remote;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.keycloak.Config;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.cluster.ClusterProviderFactory;
import org.keycloak.cluster.infinispan.InfinispanClusterProvider;
import org.keycloak.cluster.infinispan.LockEntry;
import org.keycloak.common.util.Retry;
import org.keycloak.common.util.Time;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.infinispan.util.InfinispanUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.commons.util.ByRef;
import org.jboss.logging.Logger;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.WORK_CACHE_NAME;

/**
 * 远程 Infinispan 集群提供者工厂。
 * <p>
 * 在 Hot Rod 模式下懒初始化远端 work 缓存、集群启动时间与 {@link RemoteInfinispanNotificationManager}，
 * 并实现 {@link RemoteInfinispanClusterProvider.SharedData} 供 Provider 共享状态。
 */
public class RemoteInfinispanClusterProviderFactory implements ClusterProviderFactory, RemoteInfinispanClusterProvider.SharedData, EnvironmentDependentProviderFactory {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    /** 远端 work 缓存（Hot Rod）。 */
    private volatile RemoteCache<String, LockEntry> workCache;
    /** 集群启动时间（秒）。 */
    private volatile int clusterStartupTime;
    /** 客户端事件监听器管理器。 */
    private volatile RemoteInfinispanNotificationManager notificationManager;
    /** Infinispan 提供的阻塞执行器。 */
    private volatile Executor executor;

    @Override
    public ClusterProvider create(KeycloakSession session) {
        if (workCache == null) {
            // Keycloak 不保证 postInit() 在 create() 之前调用
            lazyInit(session);
        }
        assert workCache != null;
        assert notificationManager != null;
        assert executor != null;
        return new RemoteInfinispanClusterProvider(this);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        try (var session = factory.create()) {
            lazyInit(session);
        }
    }

    @Override
    public synchronized void close() {
        logger.debug("Closing provider");
        if (notificationManager != null) {
            notificationManager.removeClientListener();
            notificationManager = null;
        }
        // executor 由 Infinispan 管理，不在此 shutdown
        executor = null;
        workCache = null;
    }

    @Override
    public String getId() {
        return InfinispanUtils.REMOTE_PROVIDER_ID;
    }

    /** 仅在远程 Infinispan 模式下启用。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return InfinispanUtils.isRemoteInfinispan();
    }

    /** 懒初始化 Hot Rod 缓存、通知管理器与集群启动时间。 */
    private synchronized void lazyInit(KeycloakSession session) {
        if (workCache != null) {
            return;
        }
        var provider = session.getProvider(InfinispanConnectionProvider.class);
        executor = provider.getExecutor("cluster-provider");
        clusterStartupTime = initClusterStartupTime(provider.getRemoteCache(WORK_CACHE_NAME), (int) (session.getKeycloakSessionFactory().getServerStartupTimestamp() / 1000));
        notificationManager = new RemoteInfinispanNotificationManager(executor, provider.getRemoteCache(WORK_CACHE_NAME), provider.getNodeInfo());
        notificationManager.addClientListener();
        workCache = provider.getRemoteCache(WORK_CACHE_NAME);

        logger.debugf("Provider initialized. Cluster startup time: %s", Time.toDate(clusterStartupTime));
    }

    /** 初始化集群启动时间：putIfAbsent 保证全局唯一值。 */
    private static int initClusterStartupTime(RemoteCache<String, Integer> cache, int serverStartupTime) {
        Integer clusterStartupTime = putIfAbsentWithRetries(cache, InfinispanClusterProvider.CLUSTER_STARTUP_TIME_KEY, serverStartupTime, -1);
        return clusterStartupTime == null ? serverStartupTime : clusterStartupTime;
    }

    /**
     * 带退避重试的 Hot Rod putIfAbsent，应对 transient HotRodClientException。
     *
     * @param taskTimeoutInSeconds 条目 TTL（秒），≤0 表示无过期
     */
    static <V> V putIfAbsentWithRetries(RemoteCache<String, V> workCache, String key, V value, int taskTimeoutInSeconds) {
        ByRef<V> ref = new ByRef<>(null);

        Retry.executeWithBackoff((int iteration) -> {
            try {
                if (taskTimeoutInSeconds > 0) {
                    ref.set(workCache.putIfAbsent(key, value, taskTimeoutInSeconds, TimeUnit.SECONDS));
                } else {
                    ref.set(workCache.putIfAbsent(key, value));
                }
            } catch (HotRodClientException re) {
                logger.warnf(re, "Failed to write key '%s' and value '%s' in iteration '%d' . Retrying", key, value, iteration);

                // 重新抛出，由 Retry 处理并重试
                throw re;
            }

        }, 10, 10);

        return ref.get();
    }

    @Override
    public int clusterStartupTime() {
        return clusterStartupTime;
    }

    @Override
    public RemoteCache<String, LockEntry> cache() {
        return workCache;
    }

    @Override
    public RemoteInfinispanNotificationManager notificationManager() {
        return notificationManager;
    }

    @Override
    public Executor executor() {
        return executor;
    }
}
