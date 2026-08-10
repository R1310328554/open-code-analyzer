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

package org.keycloak.quarkus.runtime.services.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;

import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

/**
 * Readiness health check that reports DOWN while the server bootstrap is in progress and UP once initialization completes.
 */
 * 就绪（Readiness）探针：引导进行中报告 DOWN，初始化完成后报告 UP。

@Readiness
@ApplicationScoped
public class BootstrapReadyHealthCheck implements AsyncHealthCheck {

    /** 引导完成后的固定 UP 响应，避免重复构建。 */
    private static final HealthCheckResponse UP = builder().up().build();
    /** 引导完成后缓存为 true，后续探针快速返回 UP。 */
    private boolean bootstrapCompleted;

    @Inject
    QuarkusKeycloakSessionFactory factory;

    @Override
    public Uni<HealthCheckResponse> call() {
        // JVM 分支预测可优化此路径，避免反复读取 volatile 状态
        if (bootstrapCompleted) {
            return ready();
        }
        if (factory.isBootstrapCompleted()) {
            bootstrapCompleted = true;
            return ready();
        }
        return Uni.createFrom().item(builder().down().build());
    }

    private Uni<HealthCheckResponse> ready() {
        return Uni.createFrom().item(UP);
    }

    private static HealthCheckResponseBuilder builder() {
        return HealthCheckResponse.named("Keycloak Initialized");
    }
}
