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
package org.keycloak.models.jpa.entities;

import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import org.hibernate.annotations.Nationalized;

/**
 * 通用持久化 Outbox 行：单条消息落库后由特性域 drainer 异步、至少一次投递。
 * <p>对应 {@code META-INF/jpa-changelog-26.7.0.xml} 创建的 {@code OUTBOX_ENTRY} 表；
 * SSF、webhook 等子系统共享此表，{@code entryKind} 区分消费者，复合索引 {@code (ENTRY_KIND, ...)}
 * 隔离读写 contention，无需分表。</p>
 *
 * <p>双轴分类：</p>
 * <ul>
 *   <li>{@code entryKind} — 子系统（如 "ssf"、"webhook"）。</li>
 *   <li>{@code entryType} — 子系统内具体类型（SSF 的 SET event_type、webhook 事件名等）。</li>
 * </ul>
 *
 * <p>作用域列：</p>
 * <ul>
 *   <li>{@code ownerId} — 主作用域键（SSF 为 clientId，webhook 为 hookId），驱动按 owner 统计/删除/清理。</li>
 *   <li>{@code containerId} — {@code (entryKind, ownerId)} 内可选子分组（SSF 为 stream id），
 *       使流级生命周期操作可 SQL 过滤而非藏在 {@code metadata} 中。</li>
 * </ul>
 *
 * <p>{@code payload} 线格式（签名 JWS、JSON、opaque blob）及可选 {@code metadata} JSON
 * 由子系统 {@code OutboxDeliveryHandler} 定义 — 实体层均视为 opaque 文本。</p>
 *
 * <p>Generic durable outbox row: a single message persisted for asynchronous,
 * at-least-once delivery by a feature-scoped drainer.</p>
 */
@Entity
@Table(name = "OUTBOX_ENTRY")
@NamedQueries({
        // Drainer 热路径，使用 IDX_OUTBOX_DRAIN 复合索引；上层叠加 PESSIMISTIC_WRITE + SKIP_LOCKED
        @NamedQuery(
                name = "OutboxEntryEntity.findDueForDrain",
                query = "SELECT e FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.status = :status"
                        + "   AND e.nextAttemptAt <= :now"
                        + " ORDER BY e.nextAttemptAt ASC"),
        @NamedQuery(
                name = "OutboxEntryEntity.findByOwnerAndCorrelationId",
                query = "SELECT e FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.ownerId = :ownerId"
                        + "   AND e.correlationId = :correlationId"),
        // Race-safe enqueue. The ON CONFLICT DO NOTHING clause (HQL,
        // available since Hibernate 6.5) makes the unique-constraint
        // dedup happen at the storage engine — concurrent inserts of
        // the same (entryKind, ownerId, correlationId) triple from
        // sibling nodes silently no-op instead of throwing
        // ConstraintViolationException and marking the JTA transaction
        // rollback-only. executeUpdate() returns 0 on conflict, 1 on
        // successful insert; the caller distinguishes via that count
        // and re-fetches via findByOwnerAndCorrelationId on conflict
        // to return the racing row's id. See OutboxStore#enqueueInStatus.
        @NamedQuery(
                name = "OutboxEntryEntity.insertIfAbsent",
                query = "INSERT INTO OutboxEntryEntity"
                        + " (id, entryKind, realmId, ownerId, containerId, correlationId,"
                        + "  entryType, payload, metadata, status, attempts, nextAttemptAt, createdAt)"
                        + " VALUES (:id, :entryKind, :realmId, :ownerId, :containerId, :correlationId,"
                        + "         :entryType, :payload, :metadata, :status, :attempts, :nextAttemptAt, :createdAt)"
                        + " ON CONFLICT DO NOTHING"),
        @NamedQuery(
                name = "OutboxEntryEntity.countByEntryKindRealmAndStatus",
                query = "SELECT e.status, COUNT(e) FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.realmId = :realmId"
                        + " GROUP BY e.status"),
        @NamedQuery(
                name = "OutboxEntryEntity.countByEntryKindOwnerAndStatus",
                query = "SELECT e.status, COUNT(e) FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.ownerId = :ownerId"
                        + " GROUP BY e.status"),
        @NamedQuery(
                name = "OutboxEntryEntity.oldestCreatedAtByEntryKindRealmAndStatus",
                query = "SELECT e.status, MIN(e.createdAt) FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.realmId = :realmId"
                        + " GROUP BY e.status"),
        @NamedQuery(
                name = "OutboxEntryEntity.oldestCreatedAtByEntryKindOwnerAndStatus",
                query = "SELECT e.status, MIN(e.createdAt) FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.ownerId = :ownerId"
                        + " GROUP BY e.status"),
        // Bulk DML primitives — all scoped on (feature) so multi-consumer
        // tables stay isolated.
        @NamedQuery(
                name = "OutboxEntryEntity.deleteByEntryKindAndRealm",
                query = "DELETE FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind AND e.realmId = :realmId"),
        @NamedQuery(
                name = "OutboxEntryEntity.deleteByEntryKindRealmAndStatus",
                query = "DELETE FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.realmId = :realmId"
                        + "   AND e.status = :status"),
        @NamedQuery(
                name = "OutboxEntryEntity.deleteByEntryKindRealmAndStatusOlderThan",
                query = "DELETE FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.realmId = :realmId"
                        + "   AND e.status = :status"
                        + "   AND e.createdAt < :olderThan"),
        @NamedQuery(
                name = "OutboxEntryEntity.deleteByEntryKindAndOwner",
                query = "DELETE FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind AND e.ownerId = :ownerId"),
        @NamedQuery(
                name = "OutboxEntryEntity.deleteByEntryKindOwnerAndStatus",
                query = "DELETE FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.ownerId = :ownerId"
                        + "   AND e.status = :status"),
        @NamedQuery(
                name = "OutboxEntryEntity.deleteByEntryKindOwnerAndStatusOlderThan",
                query = "DELETE FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.ownerId = :ownerId"
                        + "   AND e.status = :status"
                        + "   AND e.createdAt < :olderThan"),
        @NamedQuery(
                name = "OutboxEntryEntity.deleteQueuedByEntryKindAndRealm",
                query = "DELETE FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.realmId = :realmId"
                        + "   AND e.status IN :statuses"),
        @NamedQuery(
                name = "OutboxEntryEntity.deleteQueuedByEntryKindAndOwner",
                query = "DELETE FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.ownerId = :ownerId"
                        + "   AND e.status IN :statuses"),
        @NamedQuery(
                name = "OutboxEntryEntity.purgeByEntryKindStatusOlderThanDelivered",
                query = "DELETE FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.status = :status"
                        + "   AND e.deliveredAt < :olderThan"),
        @NamedQuery(
                name = "OutboxEntryEntity.purgeByEntryKindStatusOlderThanCreated",
                query = "DELETE FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.status = :status"
                        + "   AND e.createdAt < :olderThan"),
        // Backstop: promote rows that have been waiting too long to a
        // terminal state so the dead-letter retention purge can sweep
        // them. Bumps neither attempts nor deliveredAt — these rows
        // never actually retried; they aged out.
        @NamedQuery(
                name = "OutboxEntryEntity.promoteStaleQueuedToDeadLetter",
                query = "UPDATE OutboxEntryEntity e"
                        + "    SET e.status = :dead, e.lastError = :reason"
                        + "  WHERE e.entryKind = :entryKind"
                        + "    AND e.status IN :statuses"
                        + "    AND e.createdAt < :olderThan"),
        // Receiver-driven read paths (e.g. SSF POLL endpoint). The
        // per-owner read is on-demand — no next_attempt_at gate; the
        // ack/nack lookup additionally filters by correlation id set
        // so receivers can't poke at rows they don't own.
        @NamedQuery(
                name = "OutboxEntryEntity.findPendingForOwner",
                query = "SELECT e FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.ownerId = :ownerId"
                        + "   AND e.status = :status"
                        + " ORDER BY e.createdAt ASC"),
        @NamedQuery(
                name = "OutboxEntryEntity.findPendingForOwnerByCorrelationIds",
                query = "SELECT e FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.ownerId = :ownerId"
                        + "   AND e.correlationId IN :correlationIds"
                        + "   AND e.status = :status"),
        @NamedQuery(
                name = "OutboxEntryEntity.countByEntryKindOwnerStatus",
                query = "SELECT COUNT(e) FROM OutboxEntryEntity e"
                        + " WHERE e.entryKind = :entryKind"
                        + "   AND e.ownerId = :ownerId"
                        + "   AND e.status = :status"),
        // Owner-scoped lifecycle: pause/resume, disable, narrowed
        // type allow-list. Drives consumer-side lifecycle operations
        // such as SSF stream pause / resume / disable / events_requested
        // narrowing.
        @NamedQuery(
                name = "OutboxEntryEntity.releaseHeldForOwner",
                query = "UPDATE OutboxEntryEntity e"
                        + "    SET e.status = :pending, e.nextAttemptAt = :now"
                        + "  WHERE e.entryKind = :entryKind"
                        + "    AND e.ownerId = :ownerId"
                        + "    AND e.status = :held"),
        @NamedQuery(
                name = "OutboxEntryEntity.holdPendingForOwner",
                query = "UPDATE OutboxEntryEntity e"
                        + "    SET e.status = :held"
                        + "  WHERE e.entryKind = :entryKind"
                        + "    AND e.ownerId = :ownerId"
                        + "    AND e.status = :pending"),
        @NamedQuery(
                name = "OutboxEntryEntity.deadLetterQueuedForOwner",
                query = "UPDATE OutboxEntryEntity e"
                        + "    SET e.status = :dead, e.lastError = :reason"
                        + "  WHERE e.entryKind = :entryKind"
                        + "    AND e.ownerId = :ownerId"
                        + "    AND e.status IN :statuses"),
        @NamedQuery(
                name = "OutboxEntryEntity.deadLetterQueuedForOwnerNotMatchingTypes",
                query = "UPDATE OutboxEntryEntity e"
                        + "    SET e.status = :dead, e.lastError = :reason"
                        + "  WHERE e.entryKind = :entryKind"
                        + "    AND e.ownerId = :ownerId"
                        + "    AND e.status IN :statuses"
                        + "    AND e.entryType NOT IN :allowedTypes"),
        // Migrating an owner's queued rows to a different entryKind
        // (e.g. SSF receiver flipping push <-> poll). Terminal rows
        // (DELIVERED, DEAD_LETTER) are left alone — they're audit/dedup
        // artifacts of the previous channel and migrating them would
        // confuse correlation-id dedup on the new channel.
        @NamedQuery(
                name = "OutboxEntryEntity.migrateEntryKindForOwner",
                query = "UPDATE OutboxEntryEntity e"
                        + "    SET e.entryKind = :newKind"
                        + "  WHERE e.entryKind = :currentKind"
                        + "    AND e.ownerId = :ownerId"
                        + "    AND e.status IN :statuses")
})
public class OutboxEntryEntity {

    /** Outbox 行 UUID。 */
    @Id
    @Column(name = "ID", length = 36)
    protected String id;

    /** 子系统/特性标识（如 ssf、webhook）。 */
    @Column(name = "ENTRY_KIND", nullable = false, length = 64)
    protected String entryKind;

    /** 所属 Realm ID。 */
    @Column(name = "REALM_ID", nullable = false, length = 36)
    protected String realmId;

    /** 主作用域键（clientId、hookId 等）。 */
    @Column(name = "OWNER_ID", nullable = false, length = 64)
    protected String ownerId;

    /**
     * {@code (entryKind, ownerId)} 内可选子分组。
     * SSF 场景为 receiver 的 stream id，便于按流过滤/暂停/清理；
     * 不需要子分组的 kind 留 null。
     */
    @Column(name = "CONTAINER_ID", length = 64)
    protected String containerId;

    /** 业务关联 ID，用于幂等入队与 ack/nack 匹配。 */
    @Column(name = "CORRELATION_ID", nullable = false, length = 255)
    protected String correlationId;

    /** 子系统内具体事件/消息类型。 */
    @Column(name = "ENTRY_TYPE", nullable = false, length = 256)
    protected String entryType;

    /** 待投递载荷（opaque 文本，格式由 handler 定义）。 */
    @Nationalized
    @Column(name = "PAYLOAD", nullable = false)
    protected String payload;

    /** 可选元数据 JSON。 */
    @Nationalized
    @Column(name = "METADATA")
    protected String metadata;

    /** 投递状态（PENDING、DELIVERED、DEAD_LETTER 等）。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 16)
    protected OutboxEntryStatus status;

    /** 已尝试投递次数。 */
    @Column(name = "ATTEMPTS", nullable = false)
    protected int attempts;

    /** 下次重试时间；drainer 按此排序拉取。 */
    @Column(name = "NEXT_ATTEMPT_AT", nullable = false)
    protected Instant nextAttemptAt;

    /** 最近一次投递失败原因。 */
    @Column(name = "LAST_ERROR", length = 2048)
    protected String lastError;

    /** 入队时间。 */
    @Column(name = "CREATED_AT", nullable = false)
    protected Instant createdAt;

    /** 成功投递时间；终态 DELIVERED 时设置。 */
    @Column(name = "DELIVERED_AT")
    protected Instant deliveredAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntryKind() {
        return entryKind;
    }

    public void setEntryKind(String entryKind) {
        this.entryKind = entryKind;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public OutboxEntryStatus getStatus() {
        return status;
    }

    public void setStatus(OutboxEntryStatus status) {
        this.status = status;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(Instant nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutboxEntryEntity)) return false;
        OutboxEntryEntity that = (OutboxEntryEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
