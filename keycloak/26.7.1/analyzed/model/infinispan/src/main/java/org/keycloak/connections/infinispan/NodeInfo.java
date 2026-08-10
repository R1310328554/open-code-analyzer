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

package org.keycloak.connections.infinispan;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Transport;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.NODE_PREFIX;

/**
 * 不可变记录，描述当前 Keycloak 节点在 Infinispan 集群中的标识信息。
 * <p>
 * 包含节点名、多站点（multi-site）环境下的站点名，以及 JGroups 集群名。
 *
 * @param nodeName    节点逻辑名称（JGroups 通道名或配置项）
 * @param siteName    站点标识，非多站点部署时为 {@code null}
 * @param clusterName JGroups/Infinispan 集群名称
 */
public record NodeInfo(String nodeName, String siteName, String clusterName) {

    /** 紧凑构造器：节点名不可为空。 */
    public NodeInfo {
        Objects.requireNonNull(nodeName);
    }

    /**
     * 从嵌入式缓存管理器的传输配置与运行时 Transport 组件构建 {@link NodeInfo}。
     * <p>
     * 若配置中未指定节点名，则使用 Transport 本地名或随机生成的前缀名。
     *
     * @param cacheManager 嵌入式 Infinispan 缓存管理器
     * @return 当前节点的拓扑标识信息
     */
    public static NodeInfo of(EmbeddedCacheManager cacheManager) {
        var transportConfig = cacheManager.getCacheManagerConfiguration().transport();
        var nodeName = transportConfig.nodeName();
        var clusterName = transportConfig.clusterName();

        if (nodeName != null) {
            return new NodeInfo(nodeName, transportConfig.siteId(), clusterName);
        }

        var transport = GlobalComponentRegistry.componentOf(cacheManager, Transport.class);
        nodeName = transport == null ?
                NODE_PREFIX + ThreadLocalRandom.current().nextInt(1000000) :
                transport.localNodeName();
        return new NodeInfo(nodeName, transportConfig.siteId(), clusterName);
    }

    /** 格式化输出节点、站点与集群信息，便于日志诊断。 */
    public String printInfo() {
        return "Node name: %s, Site name: %s, Cluster name: %s".formatted(nodeName, siteName, clusterName);
    }
}
