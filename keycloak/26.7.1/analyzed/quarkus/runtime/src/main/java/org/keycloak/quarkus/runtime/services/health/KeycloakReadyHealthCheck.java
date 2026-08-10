/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.quarkus.runtime.services.health;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.agroal.runtime.health.DataSourceHealthCheck;
import io.smallrye.context.api.ManagedExecutorConfig;
import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

/**
 * Keycloak Healthcheck Readiness Probe.
 * <p>
 * Converts the standard <code>DataSourceHealthCheck</code> that waits for a connection and checks if it's valid, to an async check with a
 * dedicated pool to control load shedding.
 * <p>
 *
 * @see <a href="https://github.com/keycloak/keycloak-community/pull/55">Healthcheck API Design</a>
 */
 * Keycloak 数据库连接就绪探针：将阻塞式 {@link DataSourceHealthCheck} 包装为带专用线程池的异步检查，以控制负载 shedding 并记录持续失败起始时间。

@Readiness
@ApplicationScoped
public class KeycloakReadyHealthCheck implements AsyncHealthCheck {

    /** 健康响应 data 字段名：持续失败起始时间。 */
    public static final String FAILING_SINCE = "Failing since";

    /**
     * Date formatter, the same as used by Quarkus. This enables users to quickly compare the date printed
     * by the probe with the logs.
     */
 * 与 Quarkus 日志格式一致，便于将探针输出与日志时间对照。

    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS").withZone(ZoneId.systemDefault());

    @Inject
    DataSourceHealthCheck dataSourceHealthCheck;

    @Inject
    @ManagedExecutorConfig(maxAsync = 1, maxQueued = 20)
    ManagedExecutor executor;

    /** 数据源检查首次失败的时间点。 */
    private final AtomicReference<Instant> failingSince = new AtomicReference<>();

    /** 在 ManagedExecutor 上异步执行数据源健康检查。 */
    @Override
    public Uni<HealthCheckResponse> call() {
        Uni<HealthCheckResponse> uni = Uni.createFrom().item(this::syncCheck);
        return uni.runSubscriptionOn(executor);
    }

    /** 同步调用 Quarkus DataSourceHealthCheck 并附加失败时间戳。 */
    private HealthCheckResponse syncCheck() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Keycloak database connections async health check").up();
        HealthCheckResponse activeCheckResult = dataSourceHealthCheck.call();
        if (activeCheckResult.getStatus() == HealthCheckResponse.Status.DOWN) {
            builder.down();
            Instant failingTime = failingSince.updateAndGet(KeycloakReadyHealthCheck::createInstanceIfNeeded);
            builder.withData(FAILING_SINCE, DATE_FORMATTER.format(failingTime));
        } else {
            failingSince.set(null);
        }
        return builder.build();
    }

    /** 若 instant 为 null 则返回当前时间，用于记录首次失败时刻。 */
    static Instant createInstanceIfNeeded(Instant instant) {
        return Objects.requireNonNullElseGet(instant, Instant::now);
    }
}
