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

/**
 * {@link OutboxDeliveryHandler#deliver} 单次调用的逐行结果枚举。
 * <p>drainer 将各 outcome 映射为行状态转换：</p>
 * <ul>
 *   <li>{@link #DELIVERED} → 状态 {@code DELIVERED}，写入 {@code deliveredAt}。</li>
 *   <li>{@link #RETRY} → {@code attempts++}，按 kind 退避曲线推进 {@code next_attempt_at}；
 *       次数耗尽后升为 {@link #DEAD_LETTER}。</li>
 *   <li>{@link #DEAD_LETTER} → 无论剩余重试预算，立即标记为终端失败（如永久目的地错误）。</li>
 *   <li>{@link #ORPHANED} → 目的地已不存在（如接收方客户端被删），作为不可重试的终端失败，
 *       指标中单独统计以便发现 stream/owner 泄漏。</li>
 * </ul>
 */
public enum OutboxDeliveryOutcome {
    /** 投递成功。 */
    DELIVERED,
    /** 可重试的失败。 */
    RETRY,
    /** 不可重试的终端失败。 */
    DEAD_LETTER,
    /** 目的地已消失导致的孤儿行。 */
    ORPHANED
}
