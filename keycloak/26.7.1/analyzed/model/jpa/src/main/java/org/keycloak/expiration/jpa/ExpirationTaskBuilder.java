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

package org.keycloak.expiration.jpa;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.keycloak.expiration.jpa.impl.DefaultExpirationTask;
import org.keycloak.expiration.jpa.impl.RealmAwareExpirationTask;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ModelException;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

/**
 * 构建 {@link ExpirationTask} 实例的构建器。
 * <p>
 * 必填：{@link #withFactory(KeycloakSessionFactory)}、{@link #withAction(ExpirationAction)}、
 * {@link #withEntityId(String)}、{@link #withExecutor(Executor)}、{@link #withInterval(int, TimeUnit)}。
 * </p>
 * <p>
 * 可选：{@link #withListener(ExpirationListener)}、{@link #withMetrics(boolean)}、
 * {@link #withRealmExpiration(boolean)}、{@link #withMaxRemoval(int)}（默认 128）、
 * {@link #withTimeout(int, TimeUnit)}（默认等于 interval）。
 * </p>
 * <p>示例：</p>
 * <pre>{@code
 * ExpirationTask.builder()
 *     .withFactory(factory)
 *     .withEntityId("authentication-sessions")
 *     .withInterval(600, TimeUnit.SECONDS)
 *     .withTimeout(300, TimeUnit.SECONDS)
 *     .withAction(myAction)
 *     .withExecutor(executor)
 *     .withMetrics(true)
 *     .withRealmExpiration(true)
 *     .withMaxRemoval(128)
 *     .build()
 *     .schedule();
 * }</pre>
 *
 * @see ExpirationTask#builder()
 */
public final class ExpirationTaskBuilder {

    private static final String EXPIRATION_METRIC_NAME = "keycloak.expiration";
    private static final String EXPIRATION_DESCRIPTION = "Keycloak expiration tasks duration";

    private static final String EXPIRATION_REMOVALS_METRIC_NAME = "keycloak.expiration.removals";
    private static final String EXPIRATION_REMOVALS_DESCRIPTION = "Keycloak number of removed entities during an expiration task execution";

    private static final String TYPE_TAG = "type";
    private static final String OUTCOME_TAG = "outcome";

    /** 每批默认最大删除条目数。 */
    static final int DEFAULT_MAX_REMOVAL = 128;

    private ExpirationAction action;
    private ExpirationListener listener;
    private Executor executor;
    private String entityId;
    private int interval;
    private int timeout;
    private int maxRemoval = DEFAULT_MAX_REMOVAL;
    private boolean metrics;
    private boolean realmExpiration;
    private KeycloakSessionFactory factory;

    // 构建状态标记
    private boolean intervalSet;
    private boolean timeoutSet;

    ExpirationTaskBuilder() {
    }

    /** 设置执行实际删除的 {@link ExpirationAction}。 */
    public ExpirationTaskBuilder withAction(ExpirationAction action) {
        this.action = action;
        return this;
    }

    /**
     * 设置可选 {@link ExpirationListener}，每次任务结束后通知。
     * 若同时启用指标，metrics 监听器与用户监听器均会收到回调。
     */
    public ExpirationTaskBuilder withListener(ExpirationListener listener) {
        this.listener = listener;
        return this;
    }

    /** 设置运行清理工作的 {@link Executor}，避免阻塞定时器线程。 */
    public ExpirationTaskBuilder withExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * 设置过期实体类型的唯一标识（如 {@code authentication-sessions}），
     * 用于指标标签、日志及 {@link org.keycloak.storage.configuration.ServerConfigStorageProvider} 协调键前缀。
     */
    public ExpirationTaskBuilder withEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    /**
     * 启用或禁用 Micrometer 指标；启用时注册 {@code keycloak.expiration} 计时器
     * 与 {@code keycloak.expiration.removals} 分布摘要，按实体类型与 outcome 打标签。
     */
    public ExpirationTaskBuilder withMetrics(boolean metrics) {
        this.metrics = metrics;
        return this;
    }

    /**
     * 启用按 realm 过期：为 true 时遍历所有 realm 各调用一次 {@link ExpirationAction}；
     * 为 false 时以 {@code null} realmId 调用一次。
     */
    public ExpirationTaskBuilder withRealmExpiration(boolean realmExpiration) {
        this.realmExpiration = realmExpiration;
        return this;
    }

    /**
     * 设置每批最大删除数，默认 {@value #DEFAULT_MAX_REMOVAL}。
     *
     * @throws ModelException 值非正时抛出。
     */
    public ExpirationTaskBuilder withMaxRemoval(int maxRemoval) {
        if (maxRemoval <= 0) {
            throw new ModelException("Max removal must be greater than 0");
        }
        this.maxRemoval = maxRemoval;
        return this;
    }

    /**
     * 设置任务运行间隔。
     *
     * @throws ArithmeticException 转换结果溢出 int 时抛出。
     */
    public ExpirationTaskBuilder withInterval(int interval, TimeUnit timeUnit) {
        this.intervalSet = true;
        this.interval = Math.toIntExact(timeUnit.toSeconds(interval));
        return this;
    }

    /**
     * 设置单次运行的事务超时；未设置时默认为 interval。
     *
     * @throws ArithmeticException 转换结果溢出 int 时抛出。
     */
    public ExpirationTaskBuilder withTimeout(int timeout, TimeUnit timeUnit) {
        this.timeoutSet = true;
        this.timeout = Math.toIntExact(timeUnit.toSeconds(timeout));
        return this;
    }

    /** 设置用于各事务创建会话的 {@link KeycloakSessionFactory}。 */
    public ExpirationTaskBuilder withFactory(KeycloakSessionFactory factory) {
        this.factory = factory;
        return this;
    }

    /**
     * 构建 {@link ExpirationTask}。
     *
     * @throws NullPointerException 必填属性未设置。
     * @throws ModelException       interval 未设置或非正。
     */
    public ExpirationTask build() {
        Objects.requireNonNull(factory);
        Objects.requireNonNull(action);
        Objects.requireNonNull(entityId);
        Objects.requireNonNull(executor);
        if (!intervalSet) {
            throw new ModelException("Expiration interval must be set");
        }
        if (interval <= 0) {
            throw new ModelException("Interval must be greater than 0");
        }
        if (!timeoutSet) {
            timeout = interval;
        }
        return realmExpiration ?
                new RealmAwareExpirationTask(factory, executor, action, getListener(), entityId, timeout, interval, maxRemoval) :
                new DefaultExpirationTask(factory, executor, action, getListener(), entityId, timeout, interval, maxRemoval);
    }

    /** 组合 metrics 与用户监听器。 */
    private ExpirationListener getListener() {
        var optionalListener = Optional.ofNullable(listener);
        if (!metrics) {
            return optionalListener.orElse(NoListener.INSTANCE);
        }
        var metricsListener = createMetrics(entityId);
        return optionalListener
                .map(userListener -> (ExpirationListener) new CompositeListener(metricsListener, userListener))
                .orElse(metricsListener);
    }

    /** 创建 Micrometer 指标监听器。 */
    private static Listener createMetrics(String entityId) {
        var timer = Timer.builder(EXPIRATION_METRIC_NAME)
                .description(EXPIRATION_DESCRIPTION)
                .tag(TYPE_TAG, entityId)
                .publishPercentileHistogram()
                .withRegistry(Metrics.globalRegistry);
        var counter = DistributionSummary.builder(EXPIRATION_REMOVALS_METRIC_NAME)
                .description(EXPIRATION_REMOVALS_DESCRIPTION)
                .tag(TYPE_TAG, entityId)
                .withRegistry(Metrics.globalRegistry);
        return new Listener(timer, counter);
    }

    /** Micrometer 指标监听器实现。 */
    private record Listener(Meter.MeterProvider<Timer> timer,
                            Meter.MeterProvider<DistributionSummary> counter) implements ExpirationListener {

        private Listener {
            Objects.requireNonNull(timer);
            Objects.requireNonNull(counter);
        }

        @Override
        public void onTaskRun(String realmId, Outcome outcome, int removed, Duration duration) {
            var tags = Tags.of(OUTCOME_TAG, outcome.name());
            timer.withTags(tags).record(duration);
            counter.withTags(tags).record(removed);
        }
    }

    /** 同时转发 metrics 与用户监听器。 */
    private record CompositeListener(Listener metrics, ExpirationListener userListener) implements ExpirationListener {

        private CompositeListener {
            Objects.requireNonNull(userListener);
        }

        @Override
        public void onTaskRun(String realmId, Outcome outcome, int removed, Duration duration) {
            metrics.onTaskRun(realmId, outcome, removed, duration);
            userListener.onTaskRun(realmId, outcome, removed, duration);
        }
    }

    /** 空监听器占位。 */
    private enum NoListener implements ExpirationListener {
        INSTANCE;

        @Override
        public void onTaskRun(String realmId, Outcome outcome, int removed, Duration duration) {
            // 无操作
        }
    }

}
