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

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.jpa.entities.OutboxEntryEntity;

/**
 * 按 {@code entryKind} 注册的投递插件：将 {@link OutboxEntryEntity} 载荷送达目的地。
 * <p>
 * drainer 为通用组件；对每条到期行调用 {@link #deliver(KeycloakSession, OutboxEntryEntity)}，
 * 并根据返回的 {@link OutboxDeliveryResult} 转换行状态。
 * </p>
 * <p>
 * 每种 kind 一个 handler；drainer 按行的 {@code entryKind} 查找。
 * {@code payload} 与 {@code metadata} 列对 store 均为不透明文本。
 * </p>
 * <p>
 * 同步设计——handler 在成功、可重试失败或终端失败时返回。
 * 长轮询或 fire-and-forget 语义应在载荷已移交（如写入外部 broker）后
 * 即返回 {@link OutboxDeliveryResult#delivered()}。
 * </p>
 */
public interface OutboxDeliveryHandler {

    /**
     * 本 handler 负责的 {@code entryKind}，须与所处理行的 {@code entry_kind} 一致。
     */
    String entryKind();

    /**
     * 尝试投递单行。drainer 在调用期间持有悲观写锁；实现应避免无限阻塞，
     * 并尽量不触碰无关数据库行以缩短锁持有时间。
     * <p>
     * 未捕获的 {@link RuntimeException} 视为 {@link OutboxDeliveryOutcome#RETRY}，
     * 异常类名与消息写入 {@code last_error}。
     * </p>
     * <p>
     * 返回结果的 {@code errorMessage}（若有）持久化到 {@code last_error}
     * （{@code VARCHAR(2048)}），应尽可能包含 HTTP 状态、响应片段等诊断信息。
     * </p>
     */
    OutboxDeliveryResult deliver(KeycloakSession session, OutboxEntryEntity row);
}
