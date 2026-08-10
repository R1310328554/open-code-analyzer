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

package org.keycloak.connections.infinispan;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;

import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.util.concurrent.CompletionStages;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.persistence.manager.PersistenceManager;
import org.infinispan.util.concurrent.BlockingManager;

/**
 * 嵌入式 Infinispan 连接提供者的默认实现。
 * <p>
 * 封装 {@link EmbeddedCacheManager}、拓扑/节点信息，并提供缓存访问、
 * ProtoStream 迁移、调度执行器与序列化器等基础设施能力。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public record DefaultInfinispanConnectionProvider(EmbeddedCacheManager cacheManager,
                                                  TopologyInfo topologyInfo,
                                                  NodeInfo nodeInfo) implements InfinispanConnectionProvider {


    public DefaultInfinispanConnectionProvider {
        Objects.requireNonNull(cacheManager);
        Objects.requireNonNull(topologyInfo);
        Objects.requireNonNull(nodeInfo);
    }

    /** 获取缓存的 PersistenceManager 组件。 */
    private static PersistenceManager persistenceManager(Cache<?, ?> cache) {
        return ComponentRegistry.componentOf(cache, PersistenceManager.class);
    }

    /** 清空持久化存储（CacheStore）中的全部数据。 */
    private static CompletionStage<Void> clearPersistenceManager(PersistenceManager persistenceManager) {
        return persistenceManager.clearAllStores(PersistenceManager.AccessMode.BOTH);
    }

    @Override
    public <K, V> Cache<K, V> getCache(String name, boolean createIfAbsent) {
        return cacheManager.getCache(name, createIfAbsent);
    }

    /** 嵌入式模式下不支持 Hot Rod 远程缓存。 */
    @Override
    public <K, V> RemoteCache<K, V> getRemoteCache(String cacheName) {
        throw new IllegalStateException("Remote stores cannot be used with Embedded Infinispan.");
    }

    @Override
    public TopologyInfo getTopologyInfo() {
        return topologyInfo;
    }

    @Override
    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    /**
     * 将 JBoss Marshalling 编码迁移至 Infinispan ProtoStream。
     * <p>
     * 仅 CacheStore（持久化层）以二进制格式存储数据，需清空后重建。
     * 假设 KC 25 与 KC 26 不会在同一集群中共存（无滚动升级）。
     */
    @Override
    public CompletionStage<Void> migrateToProtoStream() {
        // 仅 CacheStore（持久化）以二进制格式存储，需要删除
        // 假设 KC 25 与 KC 26 之间不支持滚动升级，即不会同集群共存
        var stage = CompletionStages.aggregateCompletionStage();
        Arrays.stream(CLUSTERED_CACHE_NAMES)
                .map(cacheName -> cacheManager.getCache(cacheName, false))
                .filter(Objects::nonNull)
                .map(DefaultInfinispanConnectionProvider::persistenceManager)
                .map(DefaultInfinispanConnectionProvider::clearPersistenceManager)
                .forEach(stage::dependsOn);
        return stage.freeze();
    }

    @Override
    public ScheduledExecutorService getScheduledExecutor() {
        //noinspection removal
        return GlobalComponentRegistry.of(cacheManager).getComponent(ScheduledExecutorService.class, KnownComponentNames.TIMEOUT_SCHEDULE_EXECUTOR);
    }

    @Override
    public BlockingManager getBlockingManager() {
        return GlobalComponentRegistry.componentOf(cacheManager, BlockingManager.class);
    }

    @Override
    public Marshaller getMarshaller() {
        return GlobalComponentRegistry.of(cacheManager).getComponent(Marshaller.class, KnownComponentNames.USER_MARSHALLER);
    }

    @Override
    public void close() {
    }

}
