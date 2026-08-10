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

import java.util.Objects;
import java.util.function.Function;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.KeycloakModelUtils;

import org.jboss.logging.Logger;

/**
 * realm 或 owner（如接收方客户端）删除后的 outbox 行后台清理任务。
 * <p>
 * 在单次有界事务中批量删除，使管理员删除事务可立即提交，而不背负大规模 {@code DELETE}。
 * </p>
 * <p>
 * 由特性生命周期监听器（如 SSF 的 {@code RealmRemovedEvent} / {@code ClientRemovedEvent}）
 * 提交到 Keycloak 托管执行器。任务通过 {@link KeycloakModelUtils#runJobInTransaction}
 * 开启新会话，不继承调用方事务。
 * </p>
 * <p>
 * 崩溃安全：节点中途宕机时剩余行成为孤儿，由 drainer 的 {@code pendingMaxAge}
 * 兜底或终端行保留策略最终清扫。
 * </p>
 */
public class OutboxCleanupTask implements Runnable {

    private static final Logger log = Logger.getLogger(OutboxCleanupTask.class);

    /** 清理范围：整个 realm 或单个 owner。 */
    public enum Scope {
        REALM, OWNER
    }

    protected final KeycloakSessionFactory factory;
    protected final Function<KeycloakSession, OutboxStore> storeFactory;
    protected final String entryKind;
    protected final Scope scope;
    protected final String key;

    public OutboxCleanupTask(KeycloakSessionFactory factory,
                             Function<KeycloakSession, OutboxStore> storeFactory,
                             String entryKind,
                             Scope scope,
                             String key) {
        this.factory = Objects.requireNonNull(factory, "factory");
        this.storeFactory = Objects.requireNonNull(storeFactory, "storeFactory");
        this.entryKind = Objects.requireNonNull(entryKind, "entryKind");
        this.scope = Objects.requireNonNull(scope, "scope");
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public void run() {
        try {
            int[] deletedHolder = new int[1];
            KeycloakModelUtils.runJobInTransaction(factory, session -> {
                OutboxStore store = storeFactory.apply(session);
                deletedHolder[0] = scope == Scope.REALM
                        ? store.deleteByRealm(entryKind, key)
                        : store.deleteByOwner(entryKind, key);
            });
            int deleted = deletedHolder[0];
            if (deleted > 0) {
                log.debugf("Outbox cleanup complete. entryKind=%s scope=%s key=%s deleted=%d",
                        entryKind, scope, key, deleted);
            }
        } catch (RuntimeException e) {
            // 不向上抛出：执行器会记为未捕获异常。孤儿行由 drainer 保留清理或 pendingMaxAge 兜底。
            log.warnf(e, "Outbox cleanup task failed. entryKind=%s scope=%s key=%s", entryKind, scope, key);
        }
    }
}
