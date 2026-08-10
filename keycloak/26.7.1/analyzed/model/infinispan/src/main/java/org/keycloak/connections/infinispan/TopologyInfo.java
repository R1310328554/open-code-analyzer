/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Objects;

import org.keycloak.Config;

import org.infinispan.Cache;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.Transport;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jboss.logging.Logger;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.NameCache;

/**
 * 集群拓扑与粘性会话路由信息的遗留封装类。
 * <p>
 * 节点名与站点名请改用 {@link InfinispanConnectionProvider#getNodeInfo()} 返回的 {@link NodeInfo}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @deprecated 即将移除。请通过 {@link NodeInfo} 获取节点或站点名称。
 */
@Deprecated(since = "26.5", forRemoval = true)
public class TopologyInfo {

    private static final Logger logger = Logger.getLogger(TopologyInfo.class);


    /** 集群环境中的节点名，通常对应 {@code jboss.node.name}；未设置时随机生成。 */
    private final String myNodeName;

    /** 多站点环境下配置的站点名；未配置多站点时为 {@code null}。 */
    private final String mySiteName;

    /** 标记节点名是否为运行时自动生成（影响粘性会话路由）。 */
    private final boolean isGeneratedNodeName;


    /**
     * @deprecated 请改用 {@link #TopologyInfo(EmbeddedCacheManager)}。
     */
    @Deprecated(since = "26.3", forRemoval = true)
    public TopologyInfo(EmbeddedCacheManager cacheManager, Config.Scope config, boolean embedded, String providerId) {
        this(cacheManager);
    }

    /**
     * 从缓存管理器解析本地节点名与站点名。
     *
     * @param cacheManager 嵌入式 Infinispan 缓存管理器
     */
    @Deprecated(since = "26.5", forRemoval = true)
    public TopologyInfo(EmbeddedCacheManager cacheManager) {
        var transportConfig = cacheManager.getCacheManagerConfiguration().transport();
        var transport = GlobalComponentRegistry.componentOf(cacheManager, Transport.class);

        if (transport == null) {
            // 非集群模式：若用户配置了节点名则使用，否则生成随机节点名
            var nodeName = transportConfig.nodeName();
            this.isGeneratedNodeName = nodeName == null || nodeName.isEmpty();
            this.myNodeName = isGeneratedNodeName ? generateNodeName() : nodeName;
        } else {
            // 集群模式：使用配置的节点名，未配置则采用 JGroups 生成的名称
            this.myNodeName = transport.localNodeName();
            this.isGeneratedNodeName = false;
        }
        this.mySiteName = transportConfig.siteId();
    }

    /** 生成带 {@link InfinispanConnectionProvider#NODE_PREFIX} 前缀的随机节点名。 */
    private static String generateNodeName() {
        return InfinispanConnectionProvider.NODE_PREFIX + new SecureRandom().nextInt(1000000);
    }

    /**
     * @deprecated 请改用 {@link NodeInfo#nodeName()}。
     */
    @Deprecated(since = "26.5", forRemoval = true)
    public String getMyNodeName() {
        return myNodeName;
    }

    /**
     * @deprecated 请改用 {@link NodeInfo#siteName()}。
     */
    @Deprecated(since = "26.5", forRemoval = true)
    public String getMySiteName() {
        return mySiteName;
    }

    @Override
    public String toString() {
        return String.format("Node name: %s, Site name: %s", myNodeName, mySiteName);
    }

    /**
     * 在分布式缓存中判断本节点是否为指定键的主所有者；本地缓存恒为 {@code true}。
     *
     * @deprecated 无替代方案，即将移除。
     */
    @Deprecated(since = "26.5", forRemoval = true)
    public boolean amIOwner(Cache<?, ?> cache, Object key) {
        Address myAddress = cache.getCacheManager().getAddress();
        Address objectOwnerAddress = getOwnerAddress(cache, key);
        return Objects.equals(myAddress, objectOwnerAddress);
    }

    /**
     * 获取用作粘性会话标识的路由名；本地模式或无法解析时返回 {@code null}。
     *
     * @deprecated 请改用 {@link org.keycloak.sessions.StickySessionEncoderProvider#sessionIdRoute(String)}。
     */
    @Deprecated(since = "26.5", forRemoval = true)
    public String getRouteName(Cache<?, ?> cache, Object key) {
        if (cache.getCacheConfiguration().clustering().cacheMode().isClustered() && isGeneratedNodeName) {
            logger.warn("Clustered configuration used, but node name is not properly set. Make sure to start server with jboss.node.name property identifying cluster node");
        }

        if (isGeneratedNodeName) {
            return null;
        }

        // 实现参考 Wildfly 粘性会话算法（org.wildfly.clustering.web.infinispan.session.InfinispanRouteLocator）
        Address address = getOwnerAddress(cache, key);

        // 本地模式
        if (address == null ||  (address == Address.LOCAL)) {
            return myNodeName;
        }

        org.jgroups.Address jgroupsAddress = Address.toExtendedUUID(address);
        String name = NameCache.get(jgroupsAddress);

        // 若无逻辑名，则回退到物理地址
        if (name == null) {

            var transport = GlobalComponentRegistry.componentOf(cache.getCacheManager(), Transport.class);
            var channel = ((JGroupsTransport) transport).getChannel();
            var ipAddress = (IpAddress) channel.getProtocolStack().getTransport().localPhysicalAddress();
            // 节点已离群时物理地址可能为 null
            // 此处返回的是 JGroups 的 IP/PORT，语义上可能不够准确
            InetSocketAddress socketAddress = (ipAddress != null) ? new InetSocketAddress(ipAddress.getIpAddress(), ipAddress.getPort()) : new InetSocketAddress(0);
            name = String.format("%s:%s", socketAddress.getHostString(), socketAddress.getPort());

            logger.debugf("Address not found in NameCache. Fallback to %s", name);
        }

        return name;
    }


    /** 解析键在分布拓扑中的主所有者地址。 */
    private Address getOwnerAddress(Cache<?, ?> cache, Object key) {
        DistributionManager dist = cache.getAdvancedCache().getDistributionManager();
        return dist == null ? cache.getCacheManager().getAddress() : dist.getCacheTopology().getDistribution(key).primary();
    }
}
