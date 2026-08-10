/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

import org.infinispan.Cache;
import org.infinispan.commons.util.IntSet;
import org.infinispan.commons.util.IntSets;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.distribution.ch.KeyPartitioner;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.TopologyChanged;
import org.infinispan.notifications.cachelistener.event.TopologyChangedEvent;
import org.infinispan.remoting.transport.Address;

/**
 * 会话键亲和性服务：在分布式缓存中尽量生成本地节点所属分段的键。
 * <p>
 * 功能类似 {@link org.infinispan.affinity.KeyAffinityService}，但采用更宽松的最佳努力策略。
 * 无法在本地生成分段键时不会阻塞或抛异常，最多尝试 {@value #MAX_ATTEMPTS} 次后退回随机键。
 * <p>
 * 自动监听拓扑变更，维护本节点拥有的主/备分段信息；优先主分段，无主分段时使用备分段。
 *
 * @param <K> 生成键的类型
 */
@Listener
public class SessionAffinityService<K> implements Supplier<K> {

    /** 生成本地亲和键的最大尝试次数。 */
    public static final int MAX_ATTEMPTS = 32;
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<SessionAffinityService, Topology> UPDATER = AtomicReferenceFieldUpdater.newUpdater(SessionAffinityService.class, Topology.class, "topology");

    /** 底层随机键生成器。 */
    private final Supplier<K> generator;
    /** 将键映射到缓存分段的分区器。 */
    private final KeyPartitioner keyPartitioner;
    /** 本节点在集群中的地址。 */
    private final Address localAddress;
    /** 当前拓扑下的本地分段集合（volatile 保证可见性）。 */
    private volatile Topology topology = new Topology(IntSets.immutableEmptySet(), -1);

    public SessionAffinityService(Supplier<K> generator, KeyPartitioner keyPartitioner, Address localAddress) {
        this.generator = Objects.requireNonNull(generator);
        this.keyPartitioner = Objects.requireNonNull(keyPartitioner);
        this.localAddress = Objects.requireNonNull(localAddress);
    }

    /**
     * 为给定缓存创建亲和键生成器；非集群模式或无生成器时直接返回原生成器。
     */
    public static <T> Supplier<T> create(Cache<T, ?> cache, Supplier<T> generator) {
        if (generator == null || !cache.getCacheConfiguration().clustering().cacheMode().isClustered()) {
            return generator;
        }
        var affinityService = new SessionAffinityService<>(
                generator,
                ComponentRegistry.componentOf(cache, KeyPartitioner.class),
                cache.getCacheManager().getAddress());
        cache.addListener(affinityService);
        var cacheTopology = cache.getAdvancedCache().getDistributionManager().getCacheTopology();
        affinityService.computeTopology(cacheTopology.getWriteConsistentHash(), cacheTopology.getTopologyId());
        return affinityService;
    }

    @Override
    public K get() {
        var currentTopology = topology;
        if (currentTopology.segments.isEmpty()) {
            // 本节点未分配任何分段，直接随机生成
            return generator.get();
        }
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            var key = generator.get();
            if (currentTopology.segments.contains(keyPartitioner.getSegment(key))) {
                return key;
            }
        }
        return generator.get();
    }

    /** 拓扑变更后更新本地分段集合。 */
    @TopologyChanged
    public void handleViewChange(TopologyChangedEvent<K, ?> tce) {
        if (tce.isPre()) {
            return;
        }
        computeTopology(tce.getWriteConsistentHashAtEnd(), tce.getNewTopologyId());
    }

    /** 根据一致性哈希计算本节点拥有的分段，优先主分段。 */
    private void computeTopology(ConsistentHash consistentHash, int topologyId) {
        var segments = consistentHash.getPrimarySegmentsForOwner(localAddress);
        if (segments.isEmpty()) {
            segments = consistentHash.getSegmentsForOwner(localAddress);
        }
        var newTopology = new Topology(IntSets.from(segments), topologyId);
        UPDATER.updateAndGet(this, newTopology::chooseLatest);
    }


    /** 拓扑快照：分段集合与拓扑 ID。 */
    private record Topology(IntSet segments, int topologId) {

        private Topology {
            Objects.requireNonNull(segments);
        }

        /** 保留较新的拓扑版本，避免并发更新覆盖。 */
        public Topology chooseLatest(Topology other) {
            return topologId > other.topologId ? this : other;
        }
    }
}
