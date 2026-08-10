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

package org.keycloak.infinispan.health.impl;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.keycloak.infinispan.health.ClusterHealth;
import org.keycloak.jgroups.protocol.KEYCLOAK_JDBC_PING2;
import org.keycloak.jgroups.protocol.KEYCLOAK_JDBC_PING2.HealthStatus;

import org.infinispan.factories.annotations.Inject;
import org.infinispan.factories.scopes.Scope;
import org.infinispan.factories.scopes.Scopes;
import org.infinispan.remoting.transport.Transport;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.util.concurrent.BlockingManager;
import org.jboss.logging.Logger;

/**
 * 基于 {@link KEYCLOAK_JDBC_PING2} 的 {@link ClusterHealth} 实现。
 * <p>
 * 各节点在数据库中注册后，可通过 JDBC_PING 协议检测是否发生网络分区。
 * <p>
 * {@link KEYCLOAK_JDBC_PING2#healthStatus()} 包含完整算法说明：若返回 {@link HealthStatus#ERROR}，
 * 健康状态不变，依赖 Quarkus/Agroal 就绪探针；若返回 {@link HealthStatus#NO_COORDINATOR}，
 * 则将状态置为不健康——通常为临时情况，数据库表中至少应有一名协调者。
 *
 * @see KEYCLOAK_JDBC_PING2#healthStatus()
 */
@Scope(Scopes.GLOBAL)
public class JdbcPingClusterHealthImpl implements ClusterHealth {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    /** 防止并发健康检查重叠执行的互斥锁。 */
    private final ReentrantLock lock = new ReentrantLock();
    /** 当前节点是否认为集群健康的 volatile 标志。 */
    private volatile boolean healthy = true;
    /** 异步触发健康检查的执行器封装；为 null 表示当前环境不支持。 */
    private volatile HealthRunner runner;

    /**
     * 从 Infinispan 注入传输层与阻塞管理器，探测 JDBC_PING 协议是否可用。
     *
     * @param transport       Infinispan 集群传输；本地模式时为 null
     * @param blockingManager   用于创建 cluster-health 专用执行器
     */
    @Inject
    public void inject(Transport transport, BlockingManager blockingManager) {
        // 通过注入方法持有依赖，避免额外字段
        if (transport == null) {
            logger.debug("Cluster health check disabled. Local mode");
            return;
        }
        if (!(transport instanceof JGroupsTransport jgrp)) {
            logger.debug("JGroups Transport not found. Unable to check cluster health.");
            return;
        }
        KEYCLOAK_JDBC_PING2 ping = jgrp.getChannel().getProtocolStack().findProtocol(KEYCLOAK_JDBC_PING2.class);
        if (ping == null) {
            logger.warn("Stack 'jdbc-ping' not used. Unable to check cluster health.");
            return;
        }

        logger.debug("Cluster Health check available");
        init(ping, blockingManager.asExecutor("cluster-health"));
    }

    /** 初始化异步健康检查运行器（供测试或手动注入使用）。 */
    public void init(KEYCLOAK_JDBC_PING2 discovery, Executor executor) {
        runner = new HealthRunner(discovery, executor, this::checkHealth);
    }

    /** 根据 JDBC_PING 健康状态更新 {@link #healthy} 标志。 */
    private void checkHealth(KEYCLOAK_JDBC_PING2 ping) {
        assert ping != null;
        if (!lock.tryLock()) {
            // 已有检查进行中，跳过本次
            return;
        }
        try {
            var status = ping.healthStatus();
            switch (status) {
                case HEALTHY:
                    logger.debug("Set cluster health status to healthy");
                    healthy = true;
                    break;
                case NO_COORDINATOR:
                    logger.warn("Unable to check the cluster health because no coordinator has been found.");
                    // fallthrough
                case UNHEALTHY:
                    logger.debug("Set cluster health status to unhealthy");
                    healthy = false;
                    break;
                case ERROR:
                    logger.debug("Error querying the database. Skip updating the cluster health status.");
                    break;
            }
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isHealthy() {
        return healthy;
    }

    /** {@inheritDoc} 在专用执行器上异步提交健康检查。 */
    @Override
    public void triggerClusterHealthCheck() {
        if (runner != null) {
            runner.trigger();
        }
    }

    /** {@inheritDoc} 仅当 JDBC_PING 协议栈可用时返回 true。 */
    @Override
    public boolean isSupported() {
        return runner != null;
    }

    /** 将健康检查任务提交到 Infinispan 阻塞执行器的轻量封装。 */
    private record HealthRunner(KEYCLOAK_JDBC_PING2 discovery, Executor executor, Consumer<KEYCLOAK_JDBC_PING2> check) {

        HealthRunner {
            Objects.requireNonNull(discovery);
            Objects.requireNonNull(executor);
            Objects.requireNonNull(check);
        }

        /** 异步触发一次健康检查。 */
        void trigger() {
            executor.execute(() -> check.accept(discovery));
        }
    }
}
