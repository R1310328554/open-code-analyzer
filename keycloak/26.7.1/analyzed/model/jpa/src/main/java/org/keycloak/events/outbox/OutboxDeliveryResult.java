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
 * {@link OutboxDeliveryHandler#deliver} 返回的逐行投递结果。
 * <p>
 * 将 {@link OutboxDeliveryOutcome} 与运维可见的 {@code errorMessage} 绑定，
 * drainer 将其持久化到行的 {@code last_error} 列。
 * </p>
 * <p>
 * 处理器应尽可能将诊断信息（HTTP 状态、响应片段、异常类名等）写入
 * {@code errorMessage}（列宽 {@code VARCHAR(2048)}），便于管理端与日志排查。
 * </p>
 * <p>
 * {@link #delivered()} / {@link #orphaned()} 的消息为 null；成功投递时 drainer 会清空
 * {@code last_error}。
 * </p>
 */
public record OutboxDeliveryResult(OutboxDeliveryOutcome outcome,
                                   String errorMessage) {

    /** 投递成功，无错误信息。 */
    public static OutboxDeliveryResult delivered() {
        return new OutboxDeliveryResult(OutboxDeliveryOutcome.DELIVERED, null);
    }

    /** 可重试失败，携带错误描述。 */
    public static OutboxDeliveryResult retry(String errorMessage) {
        return new OutboxDeliveryResult(OutboxDeliveryOutcome.RETRY, errorMessage);
    }

    /** 终端失败（死信），携带原因。 */
    public static OutboxDeliveryResult deadLetter(String errorMessage) {
        return new OutboxDeliveryResult(OutboxDeliveryOutcome.DEAD_LETTER, errorMessage);
    }

    /** 孤儿行（目的地不存在），无附加消息。 */
    public static OutboxDeliveryResult orphaned() {
        return new OutboxDeliveryResult(OutboxDeliveryOutcome.ORPHANED, null);
    }

    /** 孤儿行，携带原因说明。 */
    public static OutboxDeliveryResult orphaned(String errorMessage) {
        return new OutboxDeliveryResult(OutboxDeliveryOutcome.ORPHANED, errorMessage);
    }
}
