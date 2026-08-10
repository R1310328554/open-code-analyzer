/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.connections.infinispan;

import org.keycloak.provider.ProviderFactory;

/**
 * Infinispan 连接提供者工厂 SPI 接口。
 * <p>
 * 扩展 {@link ProviderFactory}，提供集群健康检查与 JGroups 协调者判定能力，
 * 供 {@link DatabaseClusterEventPollerTask} 等组件决定是否在协调者节点执行特定任务。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface InfinispanConnectionProviderFactory extends ProviderFactory<InfinispanConnectionProvider> {

    /**
     * 检测集群网络分裂（脑裂）。
     * <p>
     * 若检测到可能的网络分裂且本节点不属于获胜分区，返回 {@code false}，
     * 本节点应拒绝处理请求以保证数据安全。
     *
     * @return {@code true} 表示集群健康且可继续处理请求；{@code false} 时须拒绝所有工作
     */
    default boolean isClusterHealthy() {
        return true;
    }

    /**
     * 检查是否支持集群健康检测。
     * <p>
     * 并非所有 JGroups 配置都能发现网络分裂，此方法指示当前配置是否具备该能力。
     *
     * @return {@code true} 表示支持集群健康检查
     */
    default boolean isClusterHealthSupported() {
        return false;
    }

    /**
     * 检查当前节点是否为 JGroups 集群协调者。
     *
     * @return {@code true} 表示本节点是协调者
     */
    default boolean isCoordinator() {
        return false;
    }

    /**
     * 检查是否支持协调者判定。
     * <p>
     * 并非所有配置都使用带 JGroups 集群的嵌入式缓存管理器，
     * 此方法指示 {@link #isCoordinator()} 的返回值是否有意义。
     *
     * @return {@code true} 表示支持协调者检查
     */
    default boolean isCoordinatorSupported() {
        return false;
    }

}
