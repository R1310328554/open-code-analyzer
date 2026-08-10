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

package org.keycloak.models.sessions.infinispan.transaction;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import org.infinispan.commons.util.concurrent.AggregateCompletionStage;

/**
 * 非阻塞事务接口。
 * <p>
 * commit 与 rollback 不得阻塞调用线程，须将 {@link CompletionStage} 注册到
 * {@link AggregateCompletionStage}；调用方负责提供聚合阶段并等待其完成。
 */
public interface NonBlockingTransaction {

    /**
     * 异步提交事务。
     * <p>
     * 实现不得阻塞当前线程，可将零个或多个 {@link CompletionStage} 加入 {@code stage}。
     * 阻塞式/数据库操作应通过 {@code databaseUpdates} 入队，稍后统一执行。
     *
     * @param stage           用于收集 {@link CompletionStage} 的 {@link AggregateCompletionStage}
     * @param databaseUpdates 用于登记阻塞式数据库更新的 {@link Consumer}
     */
    void asyncCommit(AggregateCompletionStage<Void> stage, Consumer<DatabaseUpdate> databaseUpdates);

    /**
     * 异步回滚事务。
     * <p>
     * 实现不得阻塞当前线程，可将零个或多个 {@link CompletionStage} 加入 {@code stage}。
     *
     * @param stage 用于收集 {@link CompletionStage} 的 {@link AggregateCompletionStage}
     */
    void asyncRollback(AggregateCompletionStage<Void> stage);

    /**
     * 该事务是否涉及数据库实体。
     *
     * @return 为 {@code true} 时表示存在需持久化到数据库的实体
     */
    default boolean supportsLockingDatabaseEntities() {
        return false;
    }

    /**
     * 锁定即将更新的数据库实体。
     *
     * @return 为 {@code true} 表示无需锁定或已全部锁定，事务回滚概率极低
     */
    default boolean lockDatabaseEntities() {
        return false;
    }
}
