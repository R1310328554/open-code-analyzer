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
package org.keycloak.quarkus.runtime.services.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.connections.infinispan.InfinispanConnectionProviderFactory;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;

import io.smallrye.health.api.AsyncHealthCheck;
import org.eclipse.microprofile.health.Readiness;

/**
 * 集群就绪健康检查的 CDI 生产者：引导完成且 Infinispan 支持集群健康探测时才注册
 * {@link KeycloakClusterReadyHealthCheck}；否则不暴露该 Readiness Bean。
 */
@ApplicationScoped
public class KeycloakClusterReadyHealthCheckProducer {

    /** 懒加载的单例健康检查实例。 */
    private AsyncHealthCheck instance;
    /** 生产者是否已完成初始化（含不支持集群健康的情况）。 */
    private boolean ready;
    @Inject
    QuarkusKeycloakSessionFactory sessionFactory;

    /** 引导完成后按需创建集群健康检查 Bean；引导中返回 null。 */
    @Produces
    @Readiness
    @Dependent
    public AsyncHealthCheck createHealthCheck() {
        if (ready) {
            // JVM 分支预测可优化此路径，避免反复进入同步块
            return instance;
        }
        if (!sessionFactory.isBootstrapCompleted()) {
            return null;
        }
        synchronized (this) {
            if (ready) {
                return instance;
            }
            var factory = (InfinispanConnectionProviderFactory) sessionFactory.getProviderFactory(InfinispanConnectionProvider.class);
            if (factory.isClusterHealthSupported()) {
                instance = new KeycloakClusterReadyHealthCheck(factory);
            }
            ready = true;
        }

        return instance;
    }
}
