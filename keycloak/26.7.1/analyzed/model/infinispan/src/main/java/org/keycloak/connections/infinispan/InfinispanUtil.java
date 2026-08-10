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

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.spi.infinispan.impl.embedded.CacheConfigurator;

import org.infinispan.commons.time.TimeService;
import org.infinispan.commons.util.FileLookup;
import org.infinispan.commons.util.FileLookupFactory;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.global.TransportConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.factories.impl.BasicComponentRegistry;
import org.infinispan.factories.impl.ComponentRef;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.util.EmbeddedTimeService;
import org.jboss.logging.Logger;
import org.jgroups.JChannel;

/**
 * Infinispan 连接相关的静态工具类：JGroups 传输配置、缓存配置委托、
 * 以及将 Infinispan 时间服务替换为 Keycloak {@link Time} 的测试/集成辅助方法。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InfinispanUtil {

    protected static final Logger logger = Logger.getLogger(InfinispanUtil.class);

    /** 条件替换（replace）操作的最大重试次数。 */
    public static final int MAXIMUM_REPLACE_RETRIES = 25;

    /**
     * 获取当前会话关联的拓扑信息。
     *
     * @deprecated 即将移除，请改用 {@link InfinispanConnectionProvider#getNodeInfo()}。
     * @see TopologyInfo
     */
    @Deprecated
    public static TopologyInfo getTopologyInfo(KeycloakSession session) {
        return session.getProvider(InfinispanConnectionProvider.class).getTopologyInfo();
    }


    /** JGroups 通道初始化时的进程级同步锁，避免并发修改系统属性。 */
    private static final Object CHANNEL_INIT_SYNCHRONIZER = new Object();

    /**
     * 配置 Infinispan 全局传输层（JGroups 通道、节点名、站点与 JMX 域）。
     *
     * @deprecated 26.3 起弃用，无替代方案，将在后续版本移除。
     */
    @Deprecated(since = "26.3", forRemoval = true)
    public static void configureTransport(GlobalConfigurationBuilder gcb, String nodeName, String siteName, String jgroupsUdpMcastAddr,
                                          String jgroupsBindAddr, String jgroupsConfigPath) {
        if (nodeName == null) {
            gcb.transport().defaultTransport();
        } else {
            FileLookup fileLookup = FileLookupFactory.newInstance();

            synchronized (CHANNEL_INIT_SYNCHRONIZER) {
                String originalMcastAddr = System.getProperty(InfinispanConnectionProvider.JGROUPS_UDP_MCAST_ADDR);
                if (jgroupsUdpMcastAddr == null) {
                    System.getProperties().remove(InfinispanConnectionProvider.JGROUPS_UDP_MCAST_ADDR);
                } else {
                    System.setProperty(InfinispanConnectionProvider.JGROUPS_UDP_MCAST_ADDR, jgroupsUdpMcastAddr);
                }
                var originalBindAddr = System.getProperty(InfinispanConnectionProvider.JGROUPS_BIND_ADDR);
                if (jgroupsBindAddr == null) {
                    System.getProperties().remove(InfinispanConnectionProvider.JGROUPS_BIND_ADDR);
                } else {
                    System.setProperty(InfinispanConnectionProvider.JGROUPS_BIND_ADDR, jgroupsBindAddr);
                }
                try {
                    JChannel channel = new JChannel(fileLookup.lookupFileLocation(jgroupsConfigPath, InfinispanUtil.class.getClassLoader()).openStream());
                    channel.setName(nodeName);
                    JGroupsTransport transport = new JGroupsTransport(channel);

                    TransportConfigurationBuilder transportBuilder = gcb.transport()
                            .nodeName(nodeName)
                            .siteId(siteName)
                            .transport(transport);

                    // 使用与当前站点对应的集群名；不同数据中心的节点不应共享同一 JGroups 集群
                    if (siteName != null) {
                        transportBuilder.clusterName(siteName);
                    }


                    transportBuilder.jmx()
                            .domain(InfinispanConnectionProvider.JMX_DOMAIN + "-" + nodeName)
                            .enable();

                    logger.infof("Configured jgroups transport with the channel name: %s", nodeName);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    if (originalMcastAddr == null) {
                        System.getProperties().remove(InfinispanConnectionProvider.JGROUPS_UDP_MCAST_ADDR);
                    } else {
                        System.setProperty(InfinispanConnectionProvider.JGROUPS_UDP_MCAST_ADDR, originalMcastAddr);
                    }
                    if (originalBindAddr == null) {
                        System.getProperties().remove(InfinispanConnectionProvider.JGROUPS_BIND_ADDR);
                    } else {
                        System.setProperty(InfinispanConnectionProvider.JGROUPS_BIND_ADDR, originalBindAddr);
                    }
                }
            }
        }
    }

    /**
     * 创建默认缓存配置构建器。
     *
     * @deprecated 请改用 {@link CacheConfigurator#createCacheConfigurationBuilder()}。
     */
    @Deprecated(since = "26.3", forRemoval = true)
    public static ConfigurationBuilder createCacheConfigurationBuilder() {
        return CacheConfigurator.createCacheConfigurationBuilder();
    }

    /**
     * 获取 Action Token 缓存的配置（手动逐出、最大条目数与空闲过期）。
     *
     * @deprecated 请改用 {@link CacheConfigurator} 中的对应方法。
     */
    @Deprecated(since = "26.3", forRemoval = true)
    public static ConfigurationBuilder getActionTokenCacheConfig() {
        var cb = CacheConfigurator.createCacheConfigurationBuilder();

        cb.memory()
                .whenFull(EvictionStrategy.MANUAL)
                .maxCount(InfinispanConnectionProvider.ACTION_TOKEN_CACHE_DEFAULT_MAX);
        cb.expiration()
                .maxIdle(InfinispanConnectionProvider.ACTION_TOKEN_MAX_IDLE_SECONDS, TimeUnit.SECONDS)
                .wakeUpInterval(InfinispanConnectionProvider.ACTION_TOKEN_WAKE_UP_INTERVAL_SECONDS, TimeUnit.SECONDS);

        return cb;
    }

    /**
     * 获取 CRL（证书吊销列表）缓存配置。
     *
     * @deprecated 请改用 {@link CacheConfigurator#getCrlCacheConfig()}。
     */
    @Deprecated(since = "26.3", forRemoval = true)
    public static ConfigurationBuilder getCrlCacheConfig() {
        return CacheConfigurator.getCrlCacheConfig();
    }

    /**
     * 获取修订号（revision）缓存配置。
     *
     * @deprecated 请改用 {@link CacheConfigurator#getRevisionCacheConfig(long)}。
     */
    @Deprecated(since = "26.3", forRemoval = true)
    public static ConfigurationBuilder getRevisionCacheConfig(long maxEntries) {
        return CacheConfigurator.getRevisionCacheConfig(maxEntries);
    }

    /**
     * 将 Infinispan 的 {@link TimeService} 替换为遵循 Keycloak {@link Time} 的实现，
     * 便于在测试中控制虚拟时间。
     *
     * @param cacheManager 需注入 Keycloak 时间服务的嵌入式缓存管理器
     * @return 用于恢复原有 TimeService 的可运行回调
     */
    public static Runnable setTimeServiceToKeycloakTime(EmbeddedCacheManager cacheManager) {
        TimeService previousTimeService = replaceComponent(cacheManager, TimeService.class, KEYCLOAK_TIME_SERVICE, true);
        AtomicReference<TimeService> ref = new AtomicReference<>(previousTimeService);
        return () -> {
            if (ref.get() == null) {
                logger.warn("Calling revert of the TimeService when testing TimeService was already reverted");
                return;
            }

            logger.info("Revert set KeycloakIspnTimeService to the infinispan cacheManager");

            replaceComponent(cacheManager, TimeService.class, ref.getAndSet(null), true);
        };
    }

    /**
     * 源自 org.infinispan.test.TestingUtil：在运行中的缓存管理器全局组件注册表中替换组件。
     *
     * @param cacheMgr             目标缓存管理器
     * @param componentType        待替换的组件类型
     * @param replacementComponent 新组件实例
     * @param rewire               为 {@code true} 时在替换后调用 rewire 重建依赖图
     * @return 被替换掉的原始组件实例
     */
    private static <T> T replaceComponent(EmbeddedCacheManager cacheMgr, Class<T> componentType, T replacementComponent, boolean rewire) {
        GlobalComponentRegistry cr = GlobalComponentRegistry.of(cacheMgr);
        BasicComponentRegistry bcr = cr.getComponent(BasicComponentRegistry.class);
        ComponentRef<T> old = bcr.getComponent(componentType);
        bcr.replaceComponent(componentType.getName(), replacementComponent, true);
        if (rewire) {
            cr.rewire();
            cr.rewireNamedRegistries();
        }
        return old != null ? old.wired() : null;
    }

    /**
     * 委托 Keycloak {@link Time#currentTimeMillis()} 的 Infinispan TimeService 实现。
     * 使缓存过期、调度等逻辑与 Keycloak 可测试时间保持一致。
     */
    public static final TimeService KEYCLOAK_TIME_SERVICE = new EmbeddedTimeService() {

        private long getCurrentTimeMillis() {
            return Time.currentTimeMillis();
        }

        @Override
        public long wallClockTime() {
            return getCurrentTimeMillis();
        }

        @Override
        public long time() {
            return TimeUnit.MILLISECONDS.toNanos(getCurrentTimeMillis());
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(getCurrentTimeMillis());
        }
    };
}
