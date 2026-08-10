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

package org.keycloak.cluster.infinispan;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.keycloak.Config;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.cluster.ClusterProviderFactory;
import org.keycloak.common.Profile;
import org.keycloak.common.util.Time;
import org.keycloak.connections.infinispan.DefaultInfinispanConnectionProviderFactory;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.infinispan.util.InfinispanUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.context.Flag;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.Merged;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.MergeEvent;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.jboss.logging.Logger;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.WORK_CACHE_NAME;

/**
 * 嵌入式 Infinispan 集群提供者工厂，支持跨数据中心（Cross-DC）场景。
 * <p>
 * 懒初始化 work 缓存与 {@link InfinispanClusterProvider}，注册缓存条目监听器与
 * JGroups 视图变更监听器，在脑裂恢复和节点离群时清理失效锁条目与本地缓存。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InfinispanClusterProviderFactory implements ClusterProviderFactory, EnvironmentDependentProviderFactory {

    protected static final Logger logger = Logger.getLogger(InfinispanClusterProviderFactory.class);

    /** 懒初始化的 work 缓存引用。 */
    private volatile Cache<String, Object> workCache;
    /** 单例 ClusterProvider 实例（工厂级共享）。 */
    private volatile ClusterProvider clusterProvider;

    /** 本地线程池，用于视图变更回调与缓存清理，避免阻塞 JGroups 线程。 */
    private final ExecutorService localExecutor = Executors.newCachedThreadPool(r -> {
        Thread thread = Executors.defaultThreadFactory().newThread(r);
        thread.setName(this.getClass().getName() + "-" + thread.getName());
        return thread;
    });

    /** JGroups 视图/合并事件监听器。 */
    private ViewChangeListener workCacheListener;

    @Override
    public ClusterProvider create(KeycloakSession session) {
        return lazyInit(session);
    }

    /** 懒初始化并返回共享的 ClusterProvider 实例。 */
    private ClusterProvider lazyInit(KeycloakSession session) {
        if (clusterProvider != null)
            return clusterProvider;

        synchronized (this) {
            if (clusterProvider != null)
                return clusterProvider;
            InfinispanConnectionProvider ispnConnections = session.getProvider(InfinispanConnectionProvider.class);
            this.workCache = ispnConnections.getCache(WORK_CACHE_NAME);

            workCacheListener = new ViewChangeListener();
            workCache.getCacheManager().addListener(workCacheListener);

            var clusterStartupTime = initClusterStartupTime(session);
            var cp = new InfinispanClusterProvider(clusterStartupTime, ispnConnections.getNodeInfo(), workCache, localExecutor);

            // 注册 CacheEntryListener 以接收当前 DC 内的集群事件
            workCache.addListener(cp.new CacheEntryListener());
            logger.debugf("Added listener for infinispan cache: %s", workCache.getName());

            this.clusterProvider = cp;
            return clusterProvider;
        }
    }

    /**
     * 初始化或读取集群启动时间：通过 work 缓存 putIfAbsent 保证全局唯一值。
     */
    protected int initClusterStartupTime(KeycloakSession session) {
        Integer existingClusterStartTime = (Integer) workCache.get(InfinispanClusterProvider.CLUSTER_STARTUP_TIME_KEY);
        if (existingClusterStartTime != null) {
            if (logger.isDebugEnabled()) {
                logger.debugf("Loaded cluster startup time: %s", Time.toDate(existingClusterStartTime).toString());
            }
            return existingClusterStartTime;
        } else {
            // 尚未初始化，尝试写入本节点启动时间
            int serverStartTime = (int) (session.getKeycloakSessionFactory().getServerStartupTimestamp() / 1000);

            existingClusterStartTime = (Integer) workCache.putIfAbsent(InfinispanClusterProvider.CLUSTER_STARTUP_TIME_KEY, serverStartTime);
            if (existingClusterStartTime == null) {
                if (logger.isDebugEnabled()) {
                    logger.debugf("Initialized cluster startup time to %s", Time.toDate(serverStartTime).toString());
                }
                return serverStartTime;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debugf("Loaded cluster startup time: %s", Time.toDate(existingClusterStartTime).toString());
                }
                return existingClusterStartTime;
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
        synchronized (this) {
            if (workCache != null && workCacheListener != null) {
                workCache.removeListener(workCacheListener);
                workCacheListener = null;
                localExecutor.shutdown();
            }
        }
    }

    @Override
    public String getId() {
        return InfinispanUtils.EMBEDDED_PROVIDER_ID;
    }

    /** 仅在嵌入式 Infinispan 且非 stateless 模式下启用。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return InfinispanUtils.isEmbeddedInfinispan() &&
                !Profile.isFeatureEnabled(Profile.Feature.STATELESS);
    }

    /**
     * JGroups 视图变更与脑裂合并监听器：清理失效锁与本地缓存。
     */
    @Listener
    public class ViewChangeListener {

        /** 脑裂合并后清空本地缓存，强制后续请求从 DB 读取最新数据。 */
        @Merged
        public void mergeEvent(MergeEvent event) {
            // 脑裂期间仅同一分区内的 Keycloak 实例能通过 work 缓存通信；合并后需清空本地缓存
            // 以失效可能过期的值，迫使后续请求从 DB 读取
            localExecutor.execute(() ->
                    Arrays.stream(InfinispanConnectionProvider.LOCAL_CACHE_NAMES)
                            .map(name -> workCache.getCacheManager().getCache(name))
                            .filter(cache -> cache.getCacheConfiguration().clustering().cacheMode() == CacheMode.LOCAL)
                            .forEach(Cache::clear)
            );

            if (Profile.isFeatureEnabled(Profile.Feature.PERSISTENT_USER_SESSIONS)) {
                // 启用持久化用户会话时，用户/客户端会话缓存同样需本地清理
                // 会话缓存为分布式模式，此逻辑在各节点本地执行
                localExecutor.execute(() ->
                        Arrays.stream(InfinispanConnectionProvider.USER_AND_CLIENT_SESSION_CACHES)
                                .map(name -> workCache.getCacheManager().getCache(name).getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL))
                                .forEach(Cache::clear)
                );
            }
        }

        /** 节点离群时由协调者清理失效节点持有的分布式锁条目。 */
        @ViewChanged
        public void viewChanged(ViewChangedEvent event) {
            Set<String> removedNodesAddresses = convertAddresses(event.getOldMembers());
            Set<String> newAddresses = convertAddresses(event.getNewMembers());

            // 独立线程执行，避免潜在死锁
            localExecutor.execute(() -> {
                try {
                    // 协调者负责清理离群节点遗留的锁条目
                    if (workCache.getCacheManager().isCoordinator()) {

                        removedNodesAddresses.removeAll(newAddresses);

                        if (removedNodesAddresses.isEmpty()) {
                            return;
                        }

                        logger.debugf("Nodes %s removed from cluster. Removing tasks locked by this nodes", removedNodesAddresses.toString());
                        DefaultInfinispanConnectionProviderFactory.runWithReadLockOnCacheManager(() -> {
                            if (workCache.getStatus() == ComponentStatus.RUNNING) {
                                workCache.entrySet().removeIf(new LockEntryPredicate(removedNodesAddresses));
                            } else {
                                logger.warn("work cache is not running, ignoring event");
                            }
                        });
                    }
                } catch (Throwable t) {
                    logger.error("caught exception in ViewChangeListener", t);
                }
            });
        }

        private Set<String> convertAddresses(Collection<Address> addresses) {
            return addresses.stream().map(Object::toString).collect(Collectors.toSet());
        }
    }
}
