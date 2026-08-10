/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.expiration.jpa.impl;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.keycloak.expiration.jpa.ExpirationAction;
import org.keycloak.expiration.jpa.ExpirationListener;
import org.keycloak.expiration.jpa.ExpirationTask;
import org.keycloak.expiration.jpa.Outcome;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.timer.TimerProvider;

import org.jboss.logging.Logger;

/**
 * {@link ExpirationTask} 的抽象基类，负责定时调度、线程池委托与并发互斥。
 * <p>
 * 子类实现 {@link #doWork()} 完成具体清理逻辑。基类保证：
 * <ul>
 *     <li>{@link #run()} 将工作提交到 {@link Executor}，避免阻塞 Timer 线程。</li>
 *     <li>通过 {@code inProgress} 跳过重叠执行：上一轮未完成时新触发直接返回。</li>
 *     <li>清理期间临时设置会话工厂事务超时，结束后恢复为 0（无限制）。</li>
 * </ul>
 */
abstract class BaseExpirationTask implements ExpirationTask {

    protected final static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    /** 单次清理事务允许的最长秒数。 */
    private final int transactionTimeoutSeconds;
    /** 执行清理的线程池，与 Timer 线程解耦。 */
    private final Executor executor;
    /** 标记当前是否已有清理任务在运行，防止并发重入。 */
    private final AtomicBoolean inProgress;
    /** 用于创建会话与访问 TimerProvider 的工厂。 */
    protected final KeycloakSessionFactory factory;
    /** 实体类型标识，用于日志与定时器任务名（如 {@code login-failure}）。 */
    protected final String entityId;
    /** 定时器触发间隔（秒）。 */
    protected final int intervalSeconds;
    /** 每批最多删除的过期记录数，控制单次事务体量。 */
    protected final int maxRemoval;
    /** 实际执行删除的 {@link ExpirationAction} 策略。 */
    protected final ExpirationAction action;
    /** 任务结束后的指标/审计回调。 */
    protected final ExpirationListener listener;

    /** 构造基类任务，所有依赖通过构造器注入且不可为 null。 */
    BaseExpirationTask(KeycloakSessionFactory factory, Executor executor, ExpirationAction action, ExpirationListener listener, String entityId, int transactionTimeoutSeconds, int intervalSeconds, int maxRemoval) {
        this.factory = Objects.requireNonNull(factory);
        this.executor = Objects.requireNonNull(executor);
        this.action = Objects.requireNonNull(action);
        this.listener = Objects.requireNonNull(listener);
        this.entityId = Objects.requireNonNull(entityId);
        this.transactionTimeoutSeconds = transactionTimeoutSeconds;
        this.intervalSeconds = intervalSeconds;
        this.maxRemoval = maxRemoval;
        this.inProgress = new AtomicBoolean();
    }

    /** 由 Timer 回调：异步提交清理，不占用定时器线程。 */
    @Override
    public final void run() {
        executor.execute(this::removeExpired);
    }

    /** 向 {@link TimerProvider} 注册固定间隔的周期性任务。 */
    @Override
    public void schedule() {
        try (var session = factory.create()) {
            var intervalMillis = TimeUnit.SECONDS.toMillis(intervalSeconds);
            session.getProvider(TimerProvider.class)
                    .schedule(this, intervalMillis, intervalMillis, "expiration-" + entityId);
        }
    }

    /** 子类实现的实际清理逻辑（全局或按 realm 迭代）。 */
    abstract void doWork();

    /**
     * 根据成功/失败标志推导任务结果。
     * 部分批次成功后又抛异常时为 {@link Outcome#PARTIAL}。
     */
    protected static Outcome computeOutcome(boolean success, boolean failed) {
        return failed ? (success ? Outcome.PARTIAL : Outcome.FAILED) : Outcome.OK;
    }

    /** 互斥入口：设置事务超时、调用 {@link #doWork()}，finally 中恢复状态。 */
    private void removeExpired() {
        if (!inProgress.compareAndSet(false, true)) {
            logger.debugf("Skipping expiration task for '%s'. Already in progress", entityId);
            return;
        }
        KeycloakModelUtils.setTransactionLimit(factory, transactionTimeoutSeconds);
        try {
            doWork();
        } catch (Exception e) {
            logger.warnf(e, "Exception during cleanup of expired '%s'", entityId);
        } finally {
            KeycloakModelUtils.setTransactionLimit(factory, 0);
            inProgress.set(false);
        }
    }
}
