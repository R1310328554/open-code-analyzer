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

import java.util.EnumSet;
import java.util.Set;

/**
 * 通用发件箱（{@code OUTBOX_ENTRY}）表记录的生命周期状态。
 *
 * <p>状态机如下：
 * <pre>
 *     PENDING - 投递成功 -> DELIVERED
 *     PENDING - 重试耗尽 -> DEAD_LETTER
 *     PENDING - 上游暂停 -> HELD
 *     HELD    - 上游恢复 -> PENDING
 *     PENDING / HELD - 管理员「清除排队项」-> （删除）
 *     DEAD_LETTER - 管理员「重试」-> PENDING（重置 attempts、next_attempt_at）
 * </pre>
 *
 * <p>不支持暂停的功能永远不会产生 {@link #HELD} 行；该状态是通用的，
 * 因此 drainer 无需知道哪个功能使用了 pause/resume。
 *
 * <p>{@link #DELIVERED} 状态的行会短暂保留，用于审计与幂等（correlation-id 去重）。
 * {@link #DEAD_LETTER} 状态的行在按功能配置的 dead-letter 保留窗口内保留后再清理。
 * {@link #QUEUED} 集合涵盖管理员「purge queued」操作所针对的非终态。
 */
public enum OutboxEntryStatus {

    /**
     * 条目已排队等待投递。drainer 会选取此状态且 {@code next_attempt_at} 已到的行。
     */
    PENDING,

    /**
     * 条目已被目标端接受（handler 返回 {@code DELIVERED}），无需进一步处理。
     */
    DELIVERED,

    /**
     * 所有重试已耗尽（或行已超过 pendingMaxAge 兜底时限）仍未成功投递，需管理员介入。
     */
    DEAD_LETTER,

    /**
     * 条目因上游（如 SSF stream）处于暂停状态而被搁置。drainer 必须跳过此状态的行，
     * 直至上游恢复并将这些行按原始到达顺序批量转回 {@link #PENDING}。
     */
    HELD;

    /**
     * 表示仍在排队、尚未到达终态的状态集合。作为 {@code DELETE .../events/queued}
     * 管理端点（及触发它们的 disable-on-save 清理）的单一事实来源，以便将来扩展
     * 非终态集合时自动延伸「purge queued」语义而无需改 API。
     */
    public static final Set<OutboxEntryStatus> QUEUED = EnumSet.of(PENDING, HELD);
}
