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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.keycloak.common.util.Retry;
import org.keycloak.models.AbstractKeycloakTransaction;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.KeycloakTransaction;
import org.keycloak.models.utils.KeycloakModelUtils;

import org.infinispan.commons.util.concurrent.AggregateCompletionStage;
import org.infinispan.commons.util.concurrent.CompletionStages;

/**
 * 收集 {@link NonBlockingTransaction} 并以非阻塞方式提交/回滚的 {@link KeycloakTransaction} 实现。
 * <p>
 * 先并发发起 Infinispan 缓存请求，再阻塞执行数据库写入，最后等待全部缓存操作完成。
 * 此类非线程安全。
 */
public class DefaultInfinispanTransactionProvider extends AbstractKeycloakTransaction implements InfinispanTransactionProvider {
    /** 数据库更新重试的总超时时间。 */
    private static final Duration UPDATE_TIMEOUT = Duration.of(10, ChronoUnit.SECONDS);
    /** 数据库更新重试的基础间隔（毫秒）。 */
    private static final int UPDATE_BASE_INTERVAL_MILLIS = 1;

    /** 已注册的非阻塞事务列表。 */
    private final List<NonBlockingTransaction> transactionList = new ArrayList<>(4);
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    public DefaultInfinispanTransactionProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void registerTransaction(NonBlockingTransaction transaction) {
        transactionList.add(Objects.requireNonNull(transaction));
    }

    @Override
    public void close() {
        transactionList.clear();
    }

    @Override
    protected void commitImpl() {
        final AggregateCompletionStage<Void> stage = CompletionStages.aggregateCompletionStage();
        final DatabaseWrites databaseWrites = new DatabaseWrites();

        // 并发发送所有缓存请求，并将待执行的数据库写入入队
        transactionList.forEach(transaction -> transaction.asyncCommit(stage, databaseWrites));

        // 缓存请求已全部发出，在单一事务中阻塞执行数据库变更
        commitDatabaseUpdates(databaseWrites);

        // 最后等待所有缓存更新完成
        CompletionStages.join(stage.freeze());
    }

    /**
     * 在当前事务的 prepare 阶段尝试将所有数据库写入移入主 JTA 事务。
     * <p>
     * 成功时可避免额外读库与独立事务；仅当行被并发修改时才可能失败（应属罕见情况）。
     */
    public void prepareStep() {
        List<NonBlockingTransaction> dbTransactions = new ArrayList<>(1);

        for (NonBlockingTransaction t : transactionList) {
            if (t.supportsLockingDatabaseEntities()) {
                if (t.lockDatabaseEntities()) {
                    dbTransactions.add(t);
                } else {
                    // 所有 DB 实体须成功加锁，否则不安全，直接放弃 prepare
                    return;
                }
            }
        }

        if (dbTransactions.isEmpty()) {
            return;
        }

        final AggregateCompletionStage<Void> stage = CompletionStages.aggregateCompletionStage();
        final DatabaseWrites databaseWrites = new DatabaseWrites();

        // 对已锁定实体的事务并发提交缓存侧变更
        dbTransactions.forEach(transaction -> transaction.asyncCommit(stage, databaseWrites));
        transactionList.removeAll(dbTransactions);

        databaseWrites.run(session);

        // 等待缓存更新完成
        CompletionStages.join(stage.freeze());

    }

    @Override
    protected void rollbackImpl() {
        final AggregateCompletionStage<Void> stage = CompletionStages.aggregateCompletionStage();
        transactionList.forEach(transaction -> transaction.asyncRollback(stage));
        CompletionStages.join(stage.freeze());
    }

    /** 带退避重试地执行排队的数据库写入。 */
    private void commitDatabaseUpdates(DatabaseWrites databaseWrites) {
        if (databaseWrites.isEmpty()) {
            return;
        }
        Retry.executeWithBackoff(
                iteration -> KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), databaseWrites),
                UPDATE_TIMEOUT, UPDATE_BASE_INTERVAL_MILLIS);
    }

    /** 收集并在单一 JTA 事务中批量执行 {@link DatabaseUpdate}。 */
    private static class DatabaseWrites implements KeycloakSessionTask, Consumer<DatabaseUpdate> {
        private final List<DatabaseUpdate> databaseUpdateList = new ArrayList<>(2);

        boolean isEmpty() {
            return databaseUpdateList.isEmpty();
        }

        @Override
        public void run(KeycloakSession session) {
            databaseUpdateList.forEach(update -> update.write(session));
        }

        @Override
        public void accept(DatabaseUpdate databaseUpdate) {
            databaseUpdateList.add(databaseUpdate);
        }

        @Override
        public String getTaskName() {
            return "Database Update";
        }
    }
}
