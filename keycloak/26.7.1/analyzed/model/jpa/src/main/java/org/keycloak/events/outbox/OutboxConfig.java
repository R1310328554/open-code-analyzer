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

/**
 * 每种 {@code entryKind} 在 {@link OutboxDrainerTask} 与保留清理中的调优参数。
 * <p>
 * 每种已注册的 entryKind 对应一份配置，SSF 与 webhook 等可独立设置批大小、
 * 退避曲线与保留窗口。
 * </p>
 * <p>
 * {@code deadLetterRetention}、{@code deliveredRetention}、{@code pendingMaxAge}
 * 为 {@code null} 或非正 {@link Duration} 时，禁用对应清理或兜底策略（永久保留）。
 * </p>
 * <p>
 * {@code pendingMaxAge} 将超过该时长的 {@code QUEUED} 行提升为 {@code DEAD_LETTER}，
 * 防止行无限滞留。应显著大于 {@link OutboxBackoff#getMaxNaturalRetryDuration()}，
 * 且短于 {@code deadLetterRetention}，以便提升后的行在死信清理前留有审计窗口。
 * </p>
 */
public record OutboxConfig(
        String entryKind,
        int batchSize,
        OutboxBackoff backoff,
        Duration deadLetterRetention,
        Duration deliveredRetention,
        Duration pendingMaxAge) {

    public OutboxConfig {
        if (entryKind == null || entryKind.isBlank()) {
            throw new IllegalArgumentException("entryKind must not be blank");
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive, got " + batchSize);
        }
        if (backoff == null) {
            throw new IllegalArgumentException("backoff must not be null");
        }
    }
}
