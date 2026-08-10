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

package org.keycloak.models.sessions.infinispan.expiration;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.keycloak.models.RealmModel;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryExpired;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryRemoved;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryExpiredEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryRemovedEvent;
import org.infinispan.commons.hash.MurmurHash3;
import org.jboss.logging.Logger;

/**
 * 基于外部 Infinispan 的一致性哈希，用于在集群节点间分配 realm 过期检查职责。
 * <p>
 * 各 Keycloak 实例定期向远程缓存写入心跳，并通过 Hot Rod 客户端监听器维护成员列表。
 * <p>
 * 网络分区时可能出现多个实例被分配到同一 realm，此时依赖数据库锁保证一致性。
 * <p>
 * 节点上下线信息非实时，某些迭代中部分 realm 可能未被检查。
 */
@ClientListener(includeCurrentState = true)
class ConsistentHash {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private static final int MIN_HEARTBEAT_PERIOD_SECONDS = 30;
    private static final int LIFESPAN_MULTIPLIER = 3;
    /** 每轮过期检查周期内发送的心跳次数。 */
    private static final int HEARTBEATS_PER_EXPIRATION_ROUND = 4;
    private static final int STOP_TIMEOUT_MILLISECONDS = 500;
    /** 成员键前缀，用于区分 Keycloak 节点条目。 */
    private static final String MEMBER_KEY_PREFIX = "node:";

    /** 当前已知集群成员（node:uuid 形式）的有序集合快照来源。 */
    private final Set<String> membership = ConcurrentHashMap.newKeySet();
    private final String nodeUUID;
    private final String nodeName;
    private final int heartBeatPeriodSeconds;
    private final int heartBeatLifespan;
    private final ScheduledExecutorService scheduledExecutorService;
    private final RemoteCache<String, String> cache;
    private volatile ScheduledFuture<?> schedule;


    private ConsistentHash(ScheduledExecutorService scheduledExecutorService, RemoteCache<String, String> cache, String nodeUUID, String nodeName, int heartBeatPeriodSeconds, int heartBeatLifespan) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.nodeName = nodeName;
        this.heartBeatPeriodSeconds = heartBeatPeriodSeconds;
        this.cache = cache;
        this.nodeUUID = MEMBER_KEY_PREFIX + nodeUUID;
        this.heartBeatLifespan = heartBeatLifespan;
    }

    static ConsistentHash create(RemoteCache<String, String> cache, ScheduledExecutorService scheduledExecutorService, String nodeUUID, String nodeName, int expirationPeriodSeconds) {
        int period = Math.max(MIN_HEARTBEAT_PERIOD_SECONDS, expirationPeriodSeconds / HEARTBEATS_PER_EXPIRATION_ROUND);
        int lifespan = period * LIFESPAN_MULTIPLIER;
        return new ConsistentHash(Objects.requireNonNull(scheduledExecutorService), Objects.requireNonNull(cache), nodeUUID, nodeName, period, lifespan);
    }

    void start() {
        if (schedule != null) {
            return;
        }
        sendHeartBeat();
        schedule = scheduledExecutorService.scheduleAtFixedRate(this::sendHeartBeat, heartBeatPeriodSeconds, heartBeatPeriodSeconds, TimeUnit.SECONDS);
        cache.addClientListener(this);
    }

    void stop() {
        var existing = schedule;
        if (existing == null) {
            return;
        }
        cache.removeClientListener(this);
        existing.cancel(true);
        schedule = null;
        try {
            cache.removeAsync(nodeUUID).get(STOP_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            log.debugf("Exception caught during stop", e);
        }
    }

    /** 返回基于当前成员快照的 realm 过滤器：仅本节点 hash 命中的 realm 为 true。 */
    Predicate<RealmModel> consistentHashSnapshot() {
        return new HashingPredicate(membership.stream().sorted().toList(), nodeUUID);
    }

    /** 当前已知集群成员数量。 */
    int size() {
        return membership.size();
    }

    /** Hot Rod 监听器：新节点心跳写入时加入成员集。 */
    @ClientCacheEntryCreated
    public void onKeycloakConnected(ClientCacheEntryCreatedEvent<String> event) {
        addKeycloakNode(event.getKey());
    }

    /** 心跳续期时刷新成员集。 */
    @ClientCacheEntryModified
    public void onHeartbeat(ClientCacheEntryModifiedEvent<String> event) {
        addKeycloakNode(event.getKey());
    }

    /** 心跳 TTL 到期时从成员集移除节点。 */
    @ClientCacheEntryExpired
    public void onMissingHeartbeat(ClientCacheEntryExpiredEvent<String> event) {
        removeKeycloakNode(event.getKey());
    }

    /** 节点主动下线或条目被删除时移除成员。 */
    @ClientCacheEntryRemoved
    public void onKeycloakDisconnect(ClientCacheEntryRemovedEvent<String> event) {
        removeKeycloakNode(event.getKey());
    }

    private void addKeycloakNode(String uuid) {
        if (uuid.startsWith(MEMBER_KEY_PREFIX)) {
            log.debugf("Adding a keycloak instance with ID: %s", uuid);
            membership.add(uuid);
        }
    }

    private void removeKeycloakNode(String uuid) {
        if (uuid.startsWith(MEMBER_KEY_PREFIX)) {
            log.debugf("Removing keycloak instance with ID: %s", uuid);
            membership.remove(uuid);
        }
    }

    /** 向远程缓存写入带 TTL 的心跳并注册自身为成员。 */
    private void sendHeartBeat() {
        cache.putAsync(nodeUUID, nodeName, heartBeatLifespan, TimeUnit.SECONDS);
        addKeycloakNode(nodeUUID);
    }

    /** 对 realm ID 做 MurmurHash3 取模，判断本节点是否为该 realm 的负责者。 */
    private record HashingPredicate(List<String> members, String myUUID) implements Predicate<RealmModel> {

        @Override
        public boolean test(RealmModel realm) {
            var size = members.size();
            assert size > 0;
            var index = Math.abs(MurmurHash3.getInstance().hash(realm.getId())) % size;
            return myUUID.equals(members.get(index));
        }
    }
}
