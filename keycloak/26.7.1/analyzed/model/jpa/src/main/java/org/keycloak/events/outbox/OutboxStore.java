/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
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
package org.keycloak.events.outbox;

import java.time.Instant;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.jpa.entities.OutboxEntryEntity;
import org.keycloak.models.jpa.entities.OutboxEntryStatus;

import org.hibernate.LockMode;
import org.hibernate.query.SelectionQuery;
import org.jboss.logging.Logger;

/**
 * {@link OutboxEntryEntity} 的数据访问层。各方法均携带 {@code entryKind}，
 * 使同一 store 实例可服务共享 {@code OUTBOX_ENTRY} 表的多个子系统；
 * 复合索引 {@code (ENTRY_KIND, ...)} 避免跨 kind 热点互相干扰。
 *
 * <p>读模式（drainer、管理统计、保留清理）与写模式（入队、状态转换、批量删除）
 * 在此拆分，运行时 drainer / cleanup 任务组合原语而非内联 SQL。</p>
 */
public class OutboxStore {

    private static final Logger log = Logger.getLogger(OutboxStore.class);

    /**
     * {@code last_error} 列硬上限，与 changelog 中 VARCHAR(2048) 一致。
     * 在此截断，调用方可传入任意长异常消息而无需担心持久化拒绝。
     */
    public static final int MAX_LAST_ERROR_LENGTH = 2048;

    protected final KeycloakSession session;

    public OutboxStore(KeycloakSession session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    protected EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    // -- 入队 ---------------------------------------------------------------

    /**
     * 插入新的 PENDING 行，按 {@code (entryKind, ownerId, correlationId)} 去重。
     * 返回已持久化（或已存在）行的 id，供至少一次入队路径关联。
     */
    public String enqueuePending(String entryKind,
                                 String realmId,
                                 String ownerId,
                                 String containerId,
                                 String correlationId,
                                 String entryType,
                                 String payload,
                                 String metadata) {
        return enqueueInStatus(OutboxEntryStatus.PENDING, entryKind, realmId, ownerId, containerId,
                correlationId, entryType, payload, metadata);
    }

    /**
     * 插入 HELD 行——上游通道在入队时处于暂停态（如 SSF stream 暂停），
     * 在 {@link #releaseHeldForOwner} 之前不应被 drainer 处理。去重语义同 {@link #enqueuePending}。
     */
    public String enqueueHeld(String entryKind,
                              String realmId,
                              String ownerId,
                              String containerId,
                              String correlationId,
                              String entryType,
                              String payload,
                              String metadata) {
        return enqueueInStatus(OutboxEntryStatus.HELD, entryKind, realmId, ownerId, containerId,
                correlationId, entryType, payload, metadata);
    }

    protected String enqueueInStatus(OutboxEntryStatus status,
                                     String entryKind,
                                     String realmId,
                                     String ownerId,
                                     String containerId,
                                     String correlationId,
                                     String entryType,
                                     String payload,
                                     String metadata) {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(realmId, "realmId");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(correlationId, "correlationId");
        Objects.requireNonNull(entryType, "entryType");
        Objects.requireNonNull(payload, "payload");

        // 乐观本地快路径：本事务已写入或行缓存中有时，跳过 INSERT 并返回已有 id。
        // 正确性不依赖此优化——下方 ON CONFLICT DO NOTHING 在存储引擎层去重。
        OutboxEntryEntity existing = findByOwnerAndCorrelationId(entryKind, ownerId, correlationId);
        if (existing != null) {
            log.debugf("Outbox enqueue deduplicated. entryKind=%s ownerId=%s correlationId=%s existingId=%s status=%s",
                    entryKind, ownerId, correlationId, existing.getId(), existing.getStatus());
            return existing.getId();
        }

        Instant now = Instant.now();
        String id = generateEntryId();
        // 竞态安全的 INSERT。ON CONFLICT DO NOTHING（HQL，Hibernate 6.5+）在存储引擎层解决去重竞态：
        // 并发插入相同 (entryKind, ownerId, correlationId) 三元组时本 INSERT 无操作（executeUpdate 返回 0），
        // 而非抛出 ConstraintViolationException 导致 JTA 事务 rollback-only。
        //
        // next_attempt_at 仅对 drainer 锁定的 PENDING 行有意义；HELD 行忽略该列但 NOT NULL，故设为 now。
        int inserted = getEntityManager()
                .createNamedQuery("OutboxEntryEntity.insertIfAbsent")
                .setParameter("id", id)
                .setParameter("entryKind", entryKind)
                .setParameter("realmId", realmId)
                .setParameter("ownerId", ownerId)
                .setParameter("containerId", containerId)
                .setParameter("correlationId", correlationId)
                .setParameter("entryType", entryType)
                .setParameter("payload", payload)
                .setParameter("metadata", metadata)
                .setParameter("status", status)
                .setParameter("attempts", 0)
                .setParameter("nextAttemptAt", now)
                .setParameter("createdAt", now)
                .executeUpdate();

        if (inserted == 0) {
            // 去重竞态失败： sibling 行已在存储中并将被 drain，事件至多一次捕获。重新查询并返回其 id。
            OutboxEntryEntity racingRow = findByOwnerAndCorrelationId(entryKind, ownerId, correlationId);
            log.debugf("Outbox enqueue lost dedup race; sibling already inserted. "
                    + "entryKind=%s realmId=%s ownerId=%s correlationId=%s racingId=%s",
                    entryKind, realmId, ownerId, correlationId,
                    racingRow != null ? racingRow.getId() : "(unresolved)");
            return racingRow != null ? racingRow.getId() : id;
        }

        log.debugf("Outbox enqueued. id=%s status=%s entryKind=%s realmId=%s ownerId=%s containerId=%s correlationId=%s entryType=%s",
                id, status, entryKind, realmId, ownerId, containerId, correlationId, entryType);
        return id;
    }

    protected String generateEntryId() {
        return UUID.randomUUID().toString();
    }

    public OutboxEntryEntity findById(String id) {
        return getEntityManager().find(OutboxEntryEntity.class, id);
    }

    public OutboxEntryEntity findByOwnerAndCorrelationId(String entryKind, String ownerId, String correlationId) {
        try {
            return getEntityManager()
                    .createNamedQuery("OutboxEntryEntity.findByOwnerAndCorrelationId", OutboxEntryEntity.class)
                    .setParameter("entryKind", entryKind)
                    .setParameter("ownerId", ownerId)
                    .setParameter("correlationId", correlationId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    // -- Drainer 读 -----------------------------------------------------

    /**
     * 在当前事务中锁定最多 {@code limit} 条到期 PENDING 行用于投递。
     * 使用 {@code FOR UPDATE SKIP LOCKED}，集群 drainer 不会争抢同一行。
     */
    public List<OutboxEntryEntity> lockDueForDrain(String entryKind, int limit) {
        Objects.requireNonNull(entryKind, "entryKind");
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive, got " + limit);
        }
        var query = getEntityManager()
                .createNamedQuery("OutboxEntryEntity.findDueForDrain", OutboxEntryEntity.class)
                .setParameter("entryKind", entryKind)
                .setParameter("status", OutboxEntryStatus.PENDING)
                .setParameter("now", Instant.now())
                .setMaxResults(limit)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE);
        // 跳过已被其他 tick/节点锁定的行，由 sibling drainer 在其 pass 中处理。
        try {
            query.unwrap(SelectionQuery.class).setHibernateLockMode(LockMode.UPGRADE_SKIPLOCKED);
        } catch (RuntimeException e) {
            log.debugf(e, "Could not set UPGRADE_SKIPLOCKED on outbox drain query — proceeding without skip-locked");
        }
        return query.getResultList();
    }

    // -- 行状态转换 ---------------------------------------------------

    /** 标记投递成功：递增 attempts、设为 DELIVERED、写入 deliveredAt、清空 last_error。 */
    public void markDelivered(OutboxEntryEntity entity) {
        entity.setAttempts(entity.getAttempts() + 1);
        entity.setStatus(OutboxEntryStatus.DELIVERED);
        entity.setDeliveredAt(Instant.now());
        entity.setLastError(null);
        getEntityManager().merge(entity);
    }

    /** 记录可重试失败：递增 attempts、设置 nextAttemptAt 与 last_error。 */
    public void recordFailure(OutboxEntryEntity entity, Instant nextAttemptAt, String lastError) {
        entity.setAttempts(entity.getAttempts() + 1);
        entity.setNextAttemptAt(nextAttemptAt);
        entity.setLastError(truncateError(lastError));
        getEntityManager().merge(entity);
    }

    /** 标记死信：递增 attempts、设为 DEAD_LETTER、写入 last_error。 */
    public void markDeadLetter(OutboxEntryEntity entity, String lastError) {
        entity.setAttempts(entity.getAttempts() + 1);
        entity.setStatus(OutboxEntryStatus.DEAD_LETTER);
        entity.setLastError(truncateError(lastError));
        getEntityManager().merge(entity);
    }

    /**
     * 批量将 {@code createdAt} 早于 cutoff 的 {@link OutboxEntryStatus#QUEUED queued} 行
     * 提升为 {@code DEAD_LETTER}，作为 drainer 兜底，避免 PENDING/HELD 行永久滞留。
     * <p>不递增 {@code attempts}——这些行并非真正重试失败，原因写入 {@code last_error}。</p>
     */
    public int promoteStaleQueuedToDeadLetter(String entryKind, Instant cutoff, String reason) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(cutoff, "cutoff");
        return getEntityManager()
                .createNamedQuery("OutboxEntryEntity.promoteStaleQueuedToDeadLetter")
                .setParameter("entryKind", entryKind)
                .setParameter("dead", OutboxEntryStatus.DEAD_LETTER)
                .setParameter("statuses", OutboxEntryStatus.QUEUED)
                .setParameter("olderThan", cutoff)
                .setParameter("reason", truncateError(reason))
                .executeUpdate();
    }

    // -- 统计（管理端点） -------------------------------------------

    /** 按 realm 统计各状态行数。 */
    public Map<OutboxEntryStatus, Long> countStatusesForRealm(String entryKind, String realmId) {
        return groupedCountQuery("OutboxEntryEntity.countByEntryKindRealmAndStatus",
                entryKind, "realmId", realmId);
    }

    /** 按 owner 统计各状态行数。 */
    public Map<OutboxEntryStatus, Long> countStatusesForOwner(String entryKind, String ownerId) {
        return groupedCountQuery("OutboxEntryEntity.countByEntryKindOwnerAndStatus",
                entryKind, "ownerId", ownerId);
    }

    /** 按 realm 查询各状态下最旧的 createdAt。 */
    public Map<OutboxEntryStatus, Instant> oldestCreatedAtPerStatusForRealm(String entryKind, String realmId) {
        return groupedInstantQuery("OutboxEntryEntity.oldestCreatedAtByEntryKindRealmAndStatus",
                entryKind, "realmId", realmId);
    }

    /** 按 owner 查询各状态下最旧的 createdAt。 */
    public Map<OutboxEntryStatus, Instant> oldestCreatedAtPerStatusForOwner(String entryKind, String ownerId) {
        return groupedInstantQuery("OutboxEntryEntity.oldestCreatedAtByEntryKindOwnerAndStatus",
                entryKind, "ownerId", ownerId);
    }

    @SuppressWarnings("unchecked")
    private Map<OutboxEntryStatus, Long> groupedCountQuery(String namedQuery, String entryKind,
                                                           String scopeParam, String scopeValue) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(scopeValue, scopeParam);
        List<Object[]> rows = getEntityManager()
                .createNamedQuery(namedQuery)
                .setParameter("entryKind", entryKind)
                .setParameter(scopeParam, scopeValue)
                .getResultList();
        Map<OutboxEntryStatus, Long> counts = new EnumMap<>(OutboxEntryStatus.class);
        for (Object[] row : rows) {
            counts.put((OutboxEntryStatus) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    @SuppressWarnings("unchecked")
    private Map<OutboxEntryStatus, Instant> groupedInstantQuery(String namedQuery, String entryKind,
                                                                String scopeParam, String scopeValue) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(scopeValue, scopeParam);
        List<Object[]> rows = getEntityManager()
                .createNamedQuery(namedQuery)
                .setParameter("entryKind", entryKind)
                .setParameter(scopeParam, scopeValue)
                .getResultList();
        Map<OutboxEntryStatus, Instant> oldest = new EnumMap<>(OutboxEntryStatus.class);
        for (Object[] row : rows) {
            oldest.put((OutboxEntryStatus) row[0], (Instant) row[1]);
        }
        return oldest;
    }

    // -- 接收方驱动读（POLL） --------------------------------------

    /**
     * 为接收方拉取（如 SSF POLL）锁定最多 {@code limit} 条 PENDING 行。
     * 使用 {@code FOR UPDATE SKIP LOCKED}，并发请求同一 owner 时不阻塞。
     * 与 {@link #lockDueForDrain(String, int)} 不同，不检查 {@code next_attempt_at}——
     * 接收方按需拉取，不受退避 schedule 限制。
     */
    public List<OutboxEntryEntity> lockPendingForOwner(String entryKind, String ownerId, int limit) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(ownerId, "ownerId");
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive, got " + limit);
        }
        var query = getEntityManager()
                .createNamedQuery("OutboxEntryEntity.findPendingForOwner", OutboxEntryEntity.class)
                .setParameter("entryKind", entryKind)
                .setParameter("ownerId", ownerId)
                .setParameter("status", OutboxEntryStatus.PENDING)
                .setMaxResults(limit)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE);
        try {
            query.unwrap(SelectionQuery.class).setHibernateLockMode(LockMode.UPGRADE_SKIPLOCKED);
        } catch (RuntimeException e) {
            log.debugf(e, "Could not set UPGRADE_SKIPLOCKED on owner-pending query — proceeding without skip-locked");
        }
        return query.getResultList();
    }

    /**
     * 统计某 owner 在指定状态下的行数，供接收方在返回短批次后判断是否还有更多条目。
     */
    public long countForOwnerByStatus(String entryKind, String ownerId, OutboxEntryStatus status) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(status, "status");
        return getEntityManager()
                .createNamedQuery("OutboxEntryEntity.countByEntryKindOwnerStatus", Long.class)
                .setParameter("entryKind", entryKind)
                .setParameter("ownerId", ownerId)
                .setParameter("status", status)
                .getSingleResult();
    }

    /**
     * 接收方 ACK：将匹配 correlationId 的 PENDING 行转为 DELIVERED。
     * 幂等且静默限定范围——非本 owner 或已终端的行不会出现在结果中。
     *
     * @return 已转为 DELIVERED 的 correlationId 集合。
     */
    public Set<String> ackPendingForOwner(String entryKind, String ownerId, Collection<String> correlationIds) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(ownerId, "ownerId");
        if (correlationIds == null || correlationIds.isEmpty()) {
            return Set.of();
        }
        List<OutboxEntryEntity> rows = getEntityManager()
                .createNamedQuery("OutboxEntryEntity.findPendingForOwnerByCorrelationIds", OutboxEntryEntity.class)
                .setParameter("entryKind", entryKind)
                .setParameter("ownerId", ownerId)
                .setParameter("correlationIds", correlationIds)
                .setParameter("status", OutboxEntryStatus.PENDING)
                .getResultList();
        if (rows.isEmpty()) {
            return Set.of();
        }
        Set<String> acked = new LinkedHashSet<>(rows.size());
        for (OutboxEntryEntity row : rows) {
            markDelivered(row);
            acked.add(row.getCorrelationId());
        }
        log.debugf("Outbox ack. entryKind=%s ownerId=%s ackedCount=%d", entryKind, ownerId, acked.size());
        return acked;
    }

    /**
     * 接收方 NACK：匹配 PENDING 行转为 DEAD_LETTER 并携带原因。
     * 接收方拉取流程中死信仅经此显式 NACK 路径到达（无发送方侧重试计数）。
     * 幂等语义同 {@link #ackPendingForOwner}。
     *
     * @return 已转为 DEAD_LETTER 的 correlationId 集合。
     */
    public Set<String> nackPendingForOwner(String entryKind, String ownerId, Map<String, String> reasonByCorrelationId) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(ownerId, "ownerId");
        if (reasonByCorrelationId == null || reasonByCorrelationId.isEmpty()) {
            return Set.of();
        }
        List<OutboxEntryEntity> rows = getEntityManager()
                .createNamedQuery("OutboxEntryEntity.findPendingForOwnerByCorrelationIds", OutboxEntryEntity.class)
                .setParameter("entryKind", entryKind)
                .setParameter("ownerId", ownerId)
                .setParameter("correlationIds", reasonByCorrelationId.keySet())
                .setParameter("status", OutboxEntryStatus.PENDING)
                .getResultList();
        if (rows.isEmpty()) {
            return Set.of();
        }
        Set<String> nacked = new LinkedHashSet<>(rows.size());
        for (OutboxEntryEntity row : rows) {
            String reason = reasonByCorrelationId.get(row.getCorrelationId());
            markDeadLetter(row, reason != null ? reason : "receiver nack");
            nacked.add(row.getCorrelationId());
        }
        log.debugf("Outbox nack. entryKind=%s ownerId=%s nackedCount=%d", entryKind, ownerId, nacked.size());
        return nacked;
    }

    // -- Owner 生命周期（暂停/恢复/禁用/迁移） -----------------------------

    /**
     * 将 owner 下所有 {@link OutboxEntryStatus#HELD HELD} 行批量转回
     * {@link OutboxEntryStatus#PENDING PENDING}，{@code next_attempt_at = now}，
     * 供 drainer 下一 tick 处理。与 {@link #holdPendingForOwner} 对称。
     *
     * @return 从 HELD 转出的行数。
     */
    public int releaseHeldForOwner(String entryKind, String ownerId) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(ownerId, "ownerId");
        int released = getEntityManager()
                .createNamedQuery("OutboxEntryEntity.releaseHeldForOwner")
                .setParameter("entryKind", entryKind)
                .setParameter("ownerId", ownerId)
                .setParameter("pending", OutboxEntryStatus.PENDING)
                .setParameter("held", OutboxEntryStatus.HELD)
                .setParameter("now", Instant.now())
                .executeUpdate();
        if (released > 0) {
            log.debugf("Outbox released %d held row(s) for entryKind=%s ownerId=%s", released, entryKind, ownerId);
        }
        return released;
    }

    /**
     * 将 owner 下所有 PENDING 行批量转为 HELD——上游通道暂停时“停放”队列
     * （如 SSF stream paused/disabled）。
     *
     * @return PENDING → HELD 的行数。
     */
    public int holdPendingForOwner(String entryKind, String ownerId) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(ownerId, "ownerId");
        int held = getEntityManager()
                .createNamedQuery("OutboxEntryEntity.holdPendingForOwner")
                .setParameter("entryKind", entryKind)
                .setParameter("ownerId", ownerId)
                .setParameter("held", OutboxEntryStatus.HELD)
                .setParameter("pending", OutboxEntryStatus.PENDING)
                .executeUpdate();
        if (held > 0) {
            log.debugf("Outbox held %d pending row(s) for entryKind=%s ownerId=%s", held, entryKind, ownerId);
        }
        return held;
    }

    /**
     * 将 owner 下所有 queued（PENDING + HELD）行死信化并写入原因。
     * 用于上游禁止 hold（如 SSF stream disabled）时必须丢弃而非停放的场景。
     */
    public int deadLetterQueuedForOwner(String entryKind, String ownerId, String reason) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(reason, "reason");
        int updated = getEntityManager()
                .createNamedQuery("OutboxEntryEntity.deadLetterQueuedForOwner")
                .setParameter("entryKind", entryKind)
                .setParameter("ownerId", ownerId)
                .setParameter("dead", OutboxEntryStatus.DEAD_LETTER)
                .setParameter("statuses", OutboxEntryStatus.QUEUED)
                .setParameter("reason", truncateError(reason))
                .executeUpdate();
        if (updated > 0) {
            log.debugf("Outbox dead-lettered %d queued row(s) for entryKind=%s ownerId=%s", updated, entryKind, ownerId);
        }
        return updated;
    }

    /**
     * 将 owner 下 {@code entryType} 不在 {@code allowedTypes} 中的 queued 行死信化。
     * 用于上游收窄接受类型集（如 SSF receiver 缩小 {@code events_requested}），
     * 使已签名但类型被剔除的行停止投递且保留审计轨迹。
     * <p>{@code allowedTypes} 为空时回退 {@link #deadLetterQueuedForOwner}，
     * 因 SQL {@code NOT IN ()} 语义因实现而异。</p>
     */
    public int deadLetterQueuedForOwnerNotMatchingTypes(String entryKind, String ownerId,
                                                        Collection<String> allowedTypes,
                                                        String reason) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(allowedTypes, "allowedTypes");
        Objects.requireNonNull(reason, "reason");
        if (allowedTypes.isEmpty()) {
            return deadLetterQueuedForOwner(entryKind, ownerId, reason);
        }
        int updated = getEntityManager()
                .createNamedQuery("OutboxEntryEntity.deadLetterQueuedForOwnerNotMatchingTypes")
                .setParameter("entryKind", entryKind)
                .setParameter("ownerId", ownerId)
                .setParameter("dead", OutboxEntryStatus.DEAD_LETTER)
                .setParameter("statuses", OutboxEntryStatus.QUEUED)
                .setParameter("allowedTypes", allowedTypes)
                .setParameter("reason", truncateError(reason))
                .executeUpdate();
        if (updated > 0) {
            log.debugf("Outbox dead-lettered %d queued row(s) for entryKind=%s ownerId=%s with entryType outside the allow-list",
                    updated, entryKind, ownerId);
        }
        return updated;
    }

    /**
     * 将 owner 下 queued 行从 currentKind 迁移到 newKind（如 SSF push ↔ poll 切换）。
     * 终端行（DELIVERED、DEAD_LETTER）保留在原 kind 下作为审计/去重 artifact。
     *
     * @return entryKind 被迁移的行数。
     */
    public int migrateEntryKindForOwner(String currentKind, String newKind, String ownerId) {
        Objects.requireNonNull(currentKind, "currentKind");
        Objects.requireNonNull(newKind, "newKind");
        Objects.requireNonNull(ownerId, "ownerId");
        int migrated = getEntityManager()
                .createNamedQuery("OutboxEntryEntity.migrateEntryKindForOwner")
                .setParameter("currentKind", currentKind)
                .setParameter("newKind", newKind)
                .setParameter("ownerId", ownerId)
                .setParameter("statuses", OutboxEntryStatus.QUEUED)
                .executeUpdate();
        if (migrated > 0) {
            log.debugf("Outbox migrated %d row(s) for ownerId=%s from %s to %s", migrated, ownerId, currentKind, newKind);
        }
        return migrated;
    }

    // -- 管理 / 级联删除 -------------------------------------------

    /** 按 realm 删除指定 kind 的全部行。 */
    public int deleteByRealm(String entryKind, String realmId) {
        return scopedDelete("OutboxEntryEntity.deleteByEntryKindAndRealm",
                entryKind, "realmId", realmId);
    }

    /** 按 owner 删除指定 kind 的全部行。 */
    public int deleteByOwner(String entryKind, String ownerId) {
        return scopedDelete("OutboxEntryEntity.deleteByEntryKindAndOwner",
                entryKind, "ownerId", ownerId);
    }

    public int deleteByRealmAndStatus(String entryKind, String realmId, OutboxEntryStatus status) {
        Objects.requireNonNull(status, "status");
        return getEntityManager()
                .createNamedQuery("OutboxEntryEntity.deleteByEntryKindRealmAndStatus")
                .setParameter("entryKind", entryKind)
                .setParameter("realmId", realmId)
                .setParameter("status", status)
                .executeUpdate();
    }

    public int deleteByRealmAndStatusOlderThan(String entryKind, String realmId,
                                               OutboxEntryStatus status, Instant cutoff) {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(cutoff, "cutoff");
        return getEntityManager()
                .createNamedQuery("OutboxEntryEntity.deleteByEntryKindRealmAndStatusOlderThan")
                .setParameter("entryKind", entryKind)
                .setParameter("realmId", realmId)
                .setParameter("status", status)
                .setParameter("olderThan", cutoff)
                .executeUpdate();
    }

    public int deleteByOwnerAndStatus(String entryKind, String ownerId, OutboxEntryStatus status) {
        Objects.requireNonNull(status, "status");
        return getEntityManager()
                .createNamedQuery("OutboxEntryEntity.deleteByEntryKindOwnerAndStatus")
                .setParameter("entryKind", entryKind)
                .setParameter("ownerId", ownerId)
                .setParameter("status", status)
                .executeUpdate();
    }

    public int deleteByOwnerAndStatusOlderThan(String entryKind, String ownerId,
                                               OutboxEntryStatus status, Instant cutoff) {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(cutoff, "cutoff");
        return getEntityManager()
                .createNamedQuery("OutboxEntryEntity.deleteByEntryKindOwnerAndStatusOlderThan")
                .setParameter("entryKind", entryKind)
                .setParameter("ownerId", ownerId)
                .setParameter("status", status)
                .setParameter("olderThan", cutoff)
                .executeUpdate();
    }

    /**
     * 批量删除 realm 下全部 queued 行，供 realm 级“清空队列”管理端点使用。
     */
    public int deleteQueuedByRealm(String entryKind, String realmId) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(realmId, "realmId");
        return getEntityManager()
                .createNamedQuery("OutboxEntryEntity.deleteQueuedByEntryKindAndRealm")
                .setParameter("entryKind", entryKind)
                .setParameter("realmId", realmId)
                .setParameter("statuses", OutboxEntryStatus.QUEUED)
                .executeUpdate();
    }

    public int deleteQueuedByOwner(String entryKind, String ownerId) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(ownerId, "ownerId");
        return getEntityManager()
                .createNamedQuery("OutboxEntryEntity.deleteQueuedByEntryKindAndOwner")
                .setParameter("entryKind", entryKind)
                .setParameter("ownerId", ownerId)
                .setParameter("statuses", OutboxEntryStatus.QUEUED)
                .executeUpdate();
    }

    // -- 保留清理（drainer housekeeping） ---------------------------

    /** 删除 createdAt 早于 cutoff 的 DELIVERED 行。 */
    public int purgeDeliveredOlderThan(String entryKind, Instant cutoff) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(cutoff, "cutoff");
        int purged = getEntityManager()
                .createNamedQuery("OutboxEntryEntity.purgeByEntryKindStatusOlderThanDelivered")
                .setParameter("entryKind", entryKind)
                .setParameter("status", OutboxEntryStatus.DELIVERED)
                .setParameter("olderThan", cutoff)
                .executeUpdate();
        if (purged > 0) {
            log.debugf("Outbox purged %d DELIVERED row(s) older than %s for entryKind=%s", purged, cutoff, entryKind);
        }
        return purged;
    }

    /** 删除 createdAt 早于 cutoff 的 DEAD_LETTER 行。 */
    public int purgeDeadLetterOlderThan(String entryKind, Instant cutoff) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(cutoff, "cutoff");
        int purged = getEntityManager()
                .createNamedQuery("OutboxEntryEntity.purgeByEntryKindStatusOlderThanCreated")
                .setParameter("entryKind", entryKind)
                .setParameter("status", OutboxEntryStatus.DEAD_LETTER)
                .setParameter("olderThan", cutoff)
                .executeUpdate();
        if (purged > 0) {
            log.debugf("Outbox purged %d DEAD_LETTER row(s) older than %s for entryKind=%s", purged, cutoff, entryKind);
        }
        return purged;
    }

    // -- 辅助方法 -----------------------------------------------------------

    /** 按命名查询执行 scoped DELETE。 */
    private int scopedDelete(String namedQuery, String entryKind, String scopeParam, String scopeValue) {
        Objects.requireNonNull(entryKind, "entryKind");
        Objects.requireNonNull(scopeValue, scopeParam);
        return getEntityManager()
                .createNamedQuery(namedQuery)
                .setParameter("entryKind", entryKind)
                .setParameter(scopeParam, scopeValue)
                .executeUpdate();
    }

    /**
     * 将错误消息截断至列宽并追加省略号；{@code null} 原样返回（供 {@code markDelivered} 清空错误）。
     */
    protected String truncateError(String error) {
        if (error == null) {
            return null;
        }
        if (error.length() <= MAX_LAST_ERROR_LENGTH) {
            return error;
        }
        return error.substring(0, MAX_LAST_ERROR_LENGTH - 3) + "...";
    }
}
