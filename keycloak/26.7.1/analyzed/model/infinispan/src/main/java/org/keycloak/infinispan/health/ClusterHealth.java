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

package org.keycloak.infinispan.health;

/**
 * Infinispan 集群健康检查接口，用于检测网络分区并决定是否继续处理请求。
 */
public interface ClusterHealth {

    /**
     * 判断本节点是否可继续处理请求。
     * <p>
     * 网络与集群稳定时必须返回 {@code true}。
     * <p>
     * 若检测到网络分区，返回值取决于本节点是否属于“胜出”分区：属于则返回 {@code true}，否则返回 {@code false}。
     * 如何判定胜出分区由具体实现决定。
     *
     * @return 集群健康且本节点可继续处理请求时为 {@code true}，否则为 {@code false}
     */
    boolean isHealthy();

    /**
     * 触发一次集群健康检查。
     * <p>
     * 该方法应仅启动检查逻辑，不得阻塞或等待检查结果。
     */
    void triggerClusterHealthCheck();

    /**
     * 判断当前传输层配置是否支持集群健康检查。
     *
     * @return 当前传输设置无法提供足够信息时为 {@code false}
     */
    boolean isSupported();

}
