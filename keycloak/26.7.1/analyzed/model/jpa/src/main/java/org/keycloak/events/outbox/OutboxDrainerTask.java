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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.jpa.entities.OutboxEntryEntity;
import org.keycloak.timer.ScheduledTask;
import org.keycloak.utils.KeycloakSessionUtil;

import org.jboss.logging.Logger;

/**
 * 针对一种 {@code entryKind} 的通用 outbox drainer：锁定到期 PENDING 行，
 * 交给对应 {@link OutboxDeliveryHandler} 投递，并按 {@link OutboxDeliveryOutcome} 转换状态。
 * <p>
 * 每种 kind 一个 drainer 实例；调度时包装为 {@code ClusterAwareScheduledTaskRunner}，
 * HA 部署中同一 interval 仅一个节点执行 drain。
 * </p>
 * <p>
 * 单 tick 内并发成本低：store 通过 {@code FOR UPDATE SKIP LOCKED} 悲观加锁，
 * 每行在提交前即进入终端态或带未来 {@code next_attempt_at} 的 PENDING。
 * </p>
 * <p>每 tick 在 drain 后的 housekeeping：</p>
 * <ul>
 *   <li>将 {@code createdAt} 超过 {@link OutboxConfig#pendingMaxAge()} 的行提升为 DEAD_LETTER。</li>
 *   <li>清理超过 {@link OutboxConfig#deliveredRetention()} 的 DELIVERED 行。</li>
 *   <li>清理超过 {@link OutboxConfig#deadLetterRetention()} 的 DEAD_LETTER 行。</li>
 * </ul>
 */
public class OutboxDrainerTask implements ScheduledTask {

    private static final Logger log = Logger.getLogger(OutboxDrainerTask.class);

    protected final OutboxConfig config;
    protected final OutboxDeliveryHandler handler;
    protected final Function<KeycloakSession, OutboxStore> storeFactory;

    public OutboxDrainerTask(OutboxConfig config,
                             OutboxDeliveryHandler handler,
                             Function<KeycloakSession, OutboxStore> storeFactory) {
        this.config = Objects.requireNonNull(config, "config");
        this.handler = Objects.requireNonNull(handler, "handler");
        this.storeFactory = Objects.requireNonNull(storeFactory, "storeFactory");
        if (!Objects.equals(config.entryKind(), handler.entryKind())) {
            throw new IllegalArgumentException(
                    "config.entryKind=" + config.entryKind()
                            + " does not match handler.entryKind=" + handler.entryKind());
        }
    }

    @Override
    public void run(KeycloakSession session) {
        // 将 drainer 的 KeycloakSession 放入线程局部变量，供尚未重构为显式 session 参数的协作者使用。
        KeycloakSession previous = KeycloakSessionUtil.getKeycloakSession();
        KeycloakSessionUtil.setKeycloakSession(session);
        try {
            OutboxStore store = storeFactory.apply(session);
            drain(session, store);
            promoteStaleQueuedToDeadLetter(store);
            purgeDeliveredOlderThanRetention(store);
            purgeDeadLetterOlderThanRetention(store);
        } finally {
            KeycloakSessionUtil.setKeycloakSession(previous);
        }
    }

    /** 锁定并处理一批到期行。 */
    protected void drain(KeycloakSession session, OutboxStore store) {
        List<OutboxEntryEntity> due = store.lockDueForDrain(config.entryKind(), config.batchSize());
        if (due.isEmpty()) {
            return;
        }
        log.debugf("Outbox drainer processing %d due row(s) for entryKind=%s", due.size(), config.entryKind());
        for (OutboxEntryEntity row : due) {
            processOne(session, store, row);
        }
    }

    /** 对单行调用 handler 并应用状态转换。 */
    protected void processOne(KeycloakSession session, OutboxStore store, OutboxEntryEntity row) {
        OutboxDeliveryResult result;
        try {
            result = handler.deliver(session, row);
            if (result == null) {
                result = OutboxDeliveryResult.retry("delivery handler returned null result");
            }
        } catch (RuntimeException e) {
            log.warnf(e, "Outbox delivery handler threw — treating as RETRY. id=%s entryKind=%s correlationId=%s",
                    row.getId(), row.getEntryKind(), row.getCorrelationId());
            String message = e.getMessage() == null
                    ? e.getClass().getSimpleName()
                    : e.getClass().getSimpleName() + ": " + e.getMessage();
            result = OutboxDeliveryResult.retry(message);
        }

        switch (result.outcome()) {
            case DELIVERED -> {
                store.markDelivered(row);
                log.debugf("Outbox delivered. id=%s entryKind=%s correlationId=%s attempts=%d",
                        row.getId(), row.getEntryKind(), row.getCorrelationId(), row.getAttempts());
            }
            case RETRY -> handleRetry(store, row, result.errorMessage());
            case DEAD_LETTER -> {
                String reason = result.errorMessage() != null ? result.errorMessage()
                        : "handler returned DEAD_LETTER (attempt " + (row.getAttempts() + 1) + ")";
                store.markDeadLetter(row, reason);
                log.warnf("Outbox dead-lettered by handler. id=%s entryKind=%s correlationId=%s reason=%s",
                        row.getId(), row.getEntryKind(), row.getCorrelationId(), reason);
            }
            case ORPHANED -> {
                String reason = result.errorMessage() != null ? result.errorMessage()
                        : "handler returned ORPHANED (destination no longer exists)";
                store.markDeadLetter(row, reason);
                log.warnf("Outbox dead-lettered as orphan. id=%s entryKind=%s correlationId=%s",
                        row.getId(), row.getEntryKind(), row.getCorrelationId());
            }
        }
    }

    /** 调度重试或次数耗尽时转入死信。 */
    protected void handleRetry(OutboxStore store, OutboxEntryEntity row, String errorMessage) {
        int nextAttempts = row.getAttempts() + 1;
        String reason = errorMessage != null ? errorMessage : "delivery failed";
        if (config.backoff().isExhausted(nextAttempts)) {
            log.warnf("Outbox dead-lettered after %d attempts. id=%s entryKind=%s correlationId=%s",
                    nextAttempts, row.getId(), row.getEntryKind(), row.getCorrelationId());
            store.markDeadLetter(row, reason);
            return;
        }
        Instant nextAttemptAt = config.backoff().computeNextAttemptAt(Instant.now(), nextAttempts);
        log.debugf("Outbox scheduling retry. id=%s attempts=%d nextAttemptAt=%s",
                row.getId(), nextAttempts, nextAttemptAt);
        store.recordFailure(row, nextAttemptAt, reason);
    }

    /** 将滞留过久的 QUEUED 行提升为 DEAD_LETTER。 */
    protected void promoteStaleQueuedToDeadLetter(OutboxStore store) {
        Duration pendingMaxAge = config.pendingMaxAge();
        if (pendingMaxAge == null || pendingMaxAge.isZero() || pendingMaxAge.isNegative()) {
            return;
        }
        Instant cutoff = Instant.now().minus(pendingMaxAge);
        int promoted = store.promoteStaleQueuedToDeadLetter(config.entryKind(), cutoff,
                "queued exceeded pendingMaxAge");
        if (promoted > 0) {
            log.infof("Outbox promoted %d stale queued row(s) to DEAD_LETTER (entryKind=%s, pendingMaxAge=%s)",
                    promoted, config.entryKind(), pendingMaxAge);
        }
    }

    /** 按 deliveredRetention 清理已投递行。 */
    protected void purgeDeliveredOlderThanRetention(OutboxStore store) {
        Duration retention = config.deliveredRetention();
        if (retention == null || retention.isZero() || retention.isNegative()) {
            return;
        }
        store.purgeDeliveredOlderThan(config.entryKind(), Instant.now().minus(retention));
    }

    /** 按 deadLetterRetention 清理死信行。 */
    protected void purgeDeadLetterOlderThanRetention(OutboxStore store) {
        Duration retention = config.deadLetterRetention();
        if (retention == null || retention.isZero() || retention.isNegative()) {
            return;
        }
        store.purgeDeadLetterOlderThan(config.entryKind(), Instant.now().minus(retention));
    }
}
