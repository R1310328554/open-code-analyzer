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

package org.keycloak.connections.infinispan.remote;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;

import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.connections.infinispan.NodeInfo;
import org.keycloak.connections.infinispan.TopologyInfo;

import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.marshall.Marshaller;
import org.infinispan.commons.util.concurrent.CompletionStages;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.factories.KnownComponentNames;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.util.concurrent.BlockingManager;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.skipSessionsCacheIfRequired;

/**
 * 远程 Infinispan 连接提供者：同时持有嵌入式与 Hot Rod 远程缓存管理器。
 * <p>
 * 多站点部署中，嵌入式管理器负责本地缓存与持久化，远程管理器访问跨数据中心共享的集群缓存。
 *
 * @param embeddedCacheManager 本地嵌入式 Infinispan 缓存管理器
 * @param remoteCacheManager   Hot Rod 远程缓存管理器
 * @param topologyInfo         已弃用的拓扑信息（兼容旧 API）
 * @param nodeInfo             当前节点标识信息
 */
public record RemoteInfinispanConnectionProvider(EmbeddedCacheManager embeddedCacheManager,
                                                 RemoteCacheManager remoteCacheManager,
                                                 TopologyInfo topologyInfo,
                                                 NodeInfo nodeInfo) implements InfinispanConnectionProvider {

    /** 紧凑构造器：校验所有依赖非空。 */
    public RemoteInfinispanConnectionProvider {
        Objects.requireNonNull(embeddedCacheManager);
        Objects.requireNonNull(remoteCacheManager);
        Objects.requireNonNull(topologyInfo);
        Objects.requireNonNull(nodeInfo);
    }

    /** {@inheritDoc} 从嵌入式管理器获取本地缓存。 */
    @Override
    public <K, V> Cache<K, V> getCache(String name, boolean createIfAbsent) {
        return embeddedCacheManager.getCache(name, createIfAbsent);
    }

    /** {@inheritDoc} 从远程管理器获取 Hot Rod 缓存。 */
    @Override
    public <K, V> RemoteCache<K, V> getRemoteCache(String name) {
        return remoteCacheManager.getCache(name);
    }

    /** {@inheritDoc} 返回已弃用的拓扑信息对象。 */
    @Override
    public TopologyInfo getTopologyInfo() {
        return topologyInfo;
    }

    /** {@inheritDoc} 返回当前节点的名称、站点与集群信息。 */
    @Override
    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    /** {@inheritDoc} 迁移到 ProtoStream 序列化：异步清空远程集群缓存中的持久化二进制数据。 */
    @Override
    public CompletionStage<Void> migrateToProtoStream() {
        // 仅 CacheStore（持久化层）以二进制格式存储数据，需要清空。
        // 假设 KC 25 与 KC 26 之间不支持滚动升级，即同一集群中不会同时存在两个版本的服务器。
        var stage = CompletionStages.aggregateCompletionStage();
        skipSessionsCacheIfRequired(Arrays.stream(CLUSTERED_CACHE_NAMES))
                .map(this::getRemoteCache)
                .map(RemoteCache::clearAsync)
                .forEach(stage::dependsOn);
        return stage.freeze();
    }

    /** {@inheritDoc} 返回嵌入式管理器中的定时任务执行器。 */
    @Override
    public ScheduledExecutorService getScheduledExecutor() {
        //noinspection removal
        return GlobalComponentRegistry.of(embeddedCacheManager).getComponent(ScheduledExecutorService.class, KnownComponentNames.TIMEOUT_SCHEDULE_EXECUTOR);
    }

    /** {@inheritDoc} 返回用于将阻塞操作卸载到工作线程的 BlockingManager。 */
    @Override
    public BlockingManager getBlockingManager() {
        return GlobalComponentRegistry.componentOf(embeddedCacheManager, BlockingManager.class);
    }

    /** {@inheritDoc} 返回用户配置的 ProtoStream/Java 序列化 Marshaller。 */
    @Override
    public Marshaller getMarshaller() {
        return GlobalComponentRegistry.of(embeddedCacheManager).getComponent(Marshaller.class, KnownComponentNames.USER_MARSHALLER);
    }

    /** {@inheritDoc} 生命周期由工厂管理，此处无需额外清理。 */
    @Override
    public void close() {
        //no-op
    }
}
