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

package org.keycloak.models.sessions.infinispan;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.sessions.infinispan.changes.PersistentSessionUpdateTask;
import org.keycloak.models.sessions.infinispan.entities.AuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.EmbeddedClientSessionKey;

/**
 * {@link AuthenticatedClientSessionModel} 变更的事务性上下文管理器。
 * <p>
 * 在当前事务内收集所有修改（变更日志），仅在提交成功后才应用到持久化存储。
 */
public interface ClientSessionManager {

    /**
     * 为指定客户端会话向变更日志追加更新任务。
     * <p>
     * 事务提交时，任务会合并并应用到持久化的 {@link AuthenticatedClientSessionEntity}，
     * 从而更新对应的 {@link AuthenticatedClientSessionModel}。
     *
     * @param key  目标客户端会话标识
     * @param task 对持久化实体执行的变更操作
     * @throws NullPointerException 若 {@code key} 或 {@code task} 为 {@code null}
     */
    void addChange(EmbeddedClientSessionKey key, PersistentSessionUpdateTask<AuthenticatedClientSessionEntity> task);

    /**
     * 重置并替换指定会话的 {@link AuthenticatedClientSessionEntity} 状态。
     * <p>
     * 丢弃此前通过 {@code addChange} 累积的变更，执行给定任务以设置完整新状态（如 {@code restartClientSession}）。
     *
     * @param key  目标客户端会话标识
     * @param task 必须设置实体完整新状态的操作
     * @throws NullPointerException 若 {@code key} 或 {@code task} 为 {@code null}
     */
    void restartEntity(EmbeddedClientSessionKey key, PersistentSessionUpdateTask<AuthenticatedClientSessionEntity> task);

}
