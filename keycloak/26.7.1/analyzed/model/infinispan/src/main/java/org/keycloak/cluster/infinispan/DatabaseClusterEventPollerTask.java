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

package org.keycloak.cluster.infinispan;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.keycloak.cluster.StoredClusterEvent;
import org.keycloak.cluster.jpa.JpaClusterEventStoreProvider;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.connections.infinispan.InfinispanConnectionProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.sessions.infinispan.CacheDecorators;
import org.keycloak.timer.ScheduledTask;

import org.infinispan.Cache;
import org.infinispan.commons.marshall.Marshaller;
import org.jboss.logging.Logger;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.WORK_CACHE_NAME;

/**
 * 定时轮询 CLUSTER_EVENT 数据库表，拉取发往本集群的跨数据中心事件，
 * 将其重放到本地 Infinispan work 缓存，并删除已消费的数据库行。
 * <p>
 * 与 {@link DatabaseAwareClusterProviderFactory} 配合，实现基于数据库表的中继式集群通信。
 */
public class DatabaseClusterEventPollerTask implements ScheduledTask {

    private static final Logger logger = Logger.getLogger(DatabaseClusterEventPollerTask.class);

    /** 单次从数据库读取的事件批次大小。 */
    private static final int BATCH_SIZE = 100;
    /** 过期事件保留时长，超过此时间的未消费事件将被清理。 */
    private static final long STALE_EVENT_RETENTION_MS = TimeUnit.MINUTES.toMillis(5);

    /** 本集群名称，用于过滤 CLUSTER_EVENT 表中的目标集群。 */
    private final String clusterName;
    /** ProtoStream 序列化器，用于反序列化事件载荷。 */
    private final Marshaller marshaller;
    /** 下次执行过期事件清理的时间戳（毫秒）。 */
    private long staleEventHorizon;

    /**
     * @param clusterName 本集群标识
     * @param marshaller  事件序列化器
     */
    public DatabaseClusterEventPollerTask(String clusterName, Marshaller marshaller) {
        Objects.requireNonNull(clusterName);
        Objects.requireNonNull(marshaller);
        this.clusterName = clusterName;
        this.marshaller = marshaller;
    }

    /** 定时任务入口：处理新事件并清理过期事件。 */
    @Override
    public void run(KeycloakSession session) {
        JpaClusterEventStoreProvider store = new JpaClusterEventStoreProvider(session);
        processEvents(session, store, clusterName, marshaller);
        cleanupStaleEvents(session, store);
    }

    /**
     * 从数据库批量读取事件，反序列化后写入本地 work 缓存，并删除已处理的行。
     */
    static void processEvents(KeycloakSession session, JpaClusterEventStoreProvider store, String clusterName, Marshaller marshaller) {
        if (!shouldProcessEvents(session)) return;

        List<StoredClusterEvent> events = store.readEvents(clusterName, BATCH_SIZE);
        if (events.isEmpty()) {
            return;
        }

        Cache<String, Object> workCache = session.getProvider(InfinispanConnectionProvider.class)
                .getCache(WORK_CACHE_NAME);

        List<String> processedIds = new java.util.ArrayList<>();
        for (StoredClusterEvent event : events) {
            WrapperClusterEvent wrappedEvent;
            try {
                wrappedEvent = (WrapperClusterEvent) marshaller.objectFromByteBuffer(event.eventData());
            } catch (IOException | ClassNotFoundException e) {
                logger.warnf(e, "Failed to deserialize cluster event %s, skipping", event.id());
                processedIds.add(event.id());
                continue;
            }

            String key = SecretGenerator.getInstance().generateSecureID();

            if (logger.isTraceEnabled()) {
                logger.tracef("Replaying cluster event from DB: key=%s, event=%s", key, wrappedEvent);
            }

            CacheDecorators.ignoreReturnValues(workCache)
                    .put(key, wrappedEvent, 120, TimeUnit.SECONDS);
            processedIds.add(event.id());
        }

        if (!processedIds.isEmpty()) {
            store.deleteEvents(clusterName, processedIds);
            if (logger.isDebugEnabled()) {
                logger.debugf("Consumed and deleted %d cluster event(s) for cluster '%s'", processedIds.size(), clusterName);
            }
        }
    }

    /** 定期删除超过保留期的陈旧事件，频率低于事件处理本身。 */
    private void cleanupStaleEvents(KeycloakSession session, JpaClusterEventStoreProvider store) {
        if (staleEventHorizon < Time.currentTimeMillis()) {
            if (!shouldProcessEvents(session)) return;
            store.deleteEventsOlderThan(Time.currentTimeMillis() - STALE_EVENT_RETENTION_MS);
            // 过期事件清理频率应低于事件处理频率
            staleEventHorizon = Time.currentTimeMillis() + STALE_EVENT_RETENTION_MS;
        }
    }

    /**
     * 判断是否应由本节点执行轮询：仅在协调者节点运行，避免多节点重复消费。
     */
    private static boolean shouldProcessEvents(KeycloakSession session) {
        InfinispanConnectionProviderFactory providerFactory = (InfinispanConnectionProviderFactory) session.getKeycloakSessionFactory().getProviderFactory(InfinispanConnectionProvider.class);
        // 协调者节点执行即可，节省其他节点的带宽
        return !providerFactory.isCoordinatorSupported() || providerFactory.isCoordinator();
    }
}
