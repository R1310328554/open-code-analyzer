/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.server.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.cluster.ClusterConstants;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterParamMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.limit.GlobalRequestLimiter;
import com.alibaba.csp.sentinel.cluster.registry.ConfigSupplierRegistry;
import com.alibaba.csp.sentinel.cluster.server.ServerConstants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * 集群 Token 服务端全局配置管理器：监听传输、namespace 集合与流控统计配置。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterServerConfigManager {

    private static boolean embedded = false;

    /**
     * 服务端全局传输与 namespace 范围配置。
     */
    private static volatile int port = ClusterConstants.DEFAULT_CLUSTER_SERVER_PORT;
    private static volatile int idleSeconds = ServerTransportConfig.DEFAULT_IDLE_SECONDS;
    private static volatile Set<String> namespaceSet = Collections.singleton(ServerConstants.DEFAULT_NAMESPACE);

    /**
     * 服务端全局流控统计配置。
     */
    private static volatile double exceedCount = ServerFlowConfig.DEFAULT_EXCEED_COUNT;
    private static volatile double maxOccupyRatio = ServerFlowConfig.DEFAULT_MAX_OCCUPY_RATIO;
    private static volatile int intervalMs = ServerFlowConfig.DEFAULT_INTERVAL_MS;
    private static volatile int sampleCount = ServerFlowConfig.DEFAULT_SAMPLE_COUNT;
    private static volatile double maxAllowedQps = ServerFlowConfig.DEFAULT_MAX_ALLOWED_QPS;

    /**
     * 按 namespace 的流控配置，格式 (namespace, config)。
     */
    private static final Map<String, ServerFlowConfig> NAMESPACE_CONF = new ConcurrentHashMap<>();

    private static final List<ServerTransportConfigObserver> TRANSPORT_CONFIG_OBSERVERS = new ArrayList<>();

    /**
     * 集群服务端全局传输配置动态属性。
     */
    private static SentinelProperty<ServerTransportConfig> transportConfigProperty = new DynamicSentinelProperty<>();
    /**
     * 集群服务端 namespace 集合动态属性。
     */
    private static SentinelProperty<Set<String>> namespaceSetProperty = new DynamicSentinelProperty<>();
    /**
     * 集群服务端全局流控统计动态属性。
     */
    private static SentinelProperty<ServerFlowConfig> globalFlowProperty = new DynamicSentinelProperty<>();

    private static final PropertyListener<ServerTransportConfig> TRANSPORT_PROPERTY_LISTENER
        = new ServerGlobalTransportPropertyListener();
    private static final PropertyListener<ServerFlowConfig> GLOBAL_FLOW_PROPERTY_LISTENER
        = new ServerGlobalFlowPropertyListener();
    private static final PropertyListener<Set<String>> NAMESPACE_SET_PROPERTY_LISTENER
        = new ServerNamespaceSetPropertyListener();

    static {
        transportConfigProperty.addListener(TRANSPORT_PROPERTY_LISTENER);
        globalFlowProperty.addListener(GLOBAL_FLOW_PROPERTY_LISTENER);
        namespaceSetProperty.addListener(NAMESPACE_SET_PROPERTY_LISTENER);
    }

    /**
     * 注册集群服务端 namespace 集合动态属性。
     *
     * @param property server namespace set dynamic property
     */
    public static void registerNamespaceSetProperty(SentinelProperty<Set<String>> property) {
        AssertUtil.notNull(property, "namespace set dynamic property cannot be null");
        synchronized (NAMESPACE_SET_PROPERTY_LISTENER) {
            RecordLog.info(
                "[ClusterServerConfigManager] Registering new namespace set dynamic property to Sentinel server "
                    + "config manager");
            namespaceSetProperty.removeListener(NAMESPACE_SET_PROPERTY_LISTENER);
            property.addListener(NAMESPACE_SET_PROPERTY_LISTENER);
            namespaceSetProperty = property;
        }
    }

    /**
     * 注册集群服务端传输配置动态属性。
     *
     * @param property server transport configuration dynamic property
     */
    public static void registerServerTransportProperty(SentinelProperty<ServerTransportConfig> property) {
        AssertUtil.notNull(property, "cluster server transport config dynamic property cannot be null");
        synchronized (TRANSPORT_PROPERTY_LISTENER) {
            RecordLog.info(
                "[ClusterServerConfigManager] Registering new server transport dynamic property to Sentinel server "
                    + "config manager");
            transportConfigProperty.removeListener(TRANSPORT_PROPERTY_LISTENER);
            property.addListener(TRANSPORT_PROPERTY_LISTENER);
            transportConfigProperty = property;
        }
    }

    /**
     * 注册集群服务端全局流控统计配置动态属性。
     *
     * @param property server flow configuration dynamic property
     */
    public static void registerGlobalServerFlowProperty(SentinelProperty<ServerFlowConfig> property) {
        AssertUtil.notNull(property, "cluster server flow config dynamic property cannot be null");
        synchronized (GLOBAL_FLOW_PROPERTY_LISTENER) {
            RecordLog.info(
                "[ClusterServerConfigManager] Registering new server global flow dynamic property "
                    + "to Sentinel server config manager");
            globalFlowProperty.removeListener(GLOBAL_FLOW_PROPERTY_LISTENER);
            property.addListener(GLOBAL_FLOW_PROPERTY_LISTENER);
            globalFlowProperty = property;
        }
    }

    /**
     * 将 namespace 集合加载到内存属性。
     *
     * @param namespaceSet valid namespace set
     */
    public static void loadServerNamespaceSet(Set<String> namespaceSet) {
        namespaceSetProperty.updateValue(namespaceSet);
    }

    /**
     * 将传输配置加载到内存属性。
     *
     * @param config valid cluster server transport configuration
     */
    public static void loadGlobalTransportConfig(ServerTransportConfig config) {
        transportConfigProperty.updateValue(config);
    }

    /**
     * 将全局流控统计配置加载到内存属性。
     *
     * @param config valid cluster server flow configuration for global
     */
    public static void loadGlobalFlowConfig(ServerFlowConfig config) {
        globalFlowProperty.updateValue(config);
    }

    /**
     * 加载指定 namespace 的流控配置（当前仍写入全局属性）。
     *
     * @param namespace a valid namespace
     * @param config    valid flow config for the namespace
     */
    public static void loadFlowConfig(String namespace, ServerFlowConfig config) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        // TODO：支持按 namespace 的服务端流控配置
        globalFlowProperty.updateValue(config);
    }

    /**
     * 添加传输配置观察者，端口等变更时回调。
     *
     * @param observer a valid transport config observer
     */
    public static void addTransportConfigChangeObserver(ServerTransportConfigObserver observer) {
        AssertUtil.notNull(observer, "observer cannot be null");
        TRANSPORT_CONFIG_OBSERVERS.add(observer);
    }

    private static class ServerNamespaceSetPropertyListener implements PropertyListener<Set<String>> {

        @Override
        public synchronized void configLoad(Set<String> set) {
            if (set == null || set.isEmpty()) {
                RecordLog.warn("[ClusterServerConfigManager] WARN: empty initial server namespace set");
                return;
            }
            applyNamespaceSetChange(set);
        }

        @Override
        public synchronized void configUpdate(Set<String> set) {
            // TODO：是否需要防抖
            applyNamespaceSetChange(set);
        }
    }

    private static void applyNamespaceSetChange(Set<String> newSet) {
        if (newSet == null) {
            return;
        }
        RecordLog.info("[ClusterServerConfigManager] Server namespace set will be update to: {}", newSet);
        if (newSet.isEmpty()) {
            ClusterServerConfigManager.namespaceSet = Collections.singleton(ServerConstants.DEFAULT_NAMESPACE);
            return;
        }

        newSet = new HashSet<>(newSet);
        // 始终将 default namespace 加入集合
        newSet.add(ServerConstants.DEFAULT_NAMESPACE);

        if (embedded) {
            // 嵌入式模式下服务端自身也是客户端，将应用 namespace 加入集合
            newSet.add(ConfigSupplierRegistry.getNamespaceSupplier().get());
        }

        Set<String> oldSet = ClusterServerConfigManager.namespaceSet;
        if (oldSet != null && !oldSet.isEmpty()) {
            for (String ns : oldSet) {
                // 移除已下线 namespace 的集群规则属性
                if (!newSet.contains(ns)) {
                    ClusterFlowRuleManager.removeProperty(ns);
                    ClusterParamFlowRuleManager.removeProperty(ns);
                }
            }
        }

        ClusterServerConfigManager.namespaceSet = newSet;
        for (String ns : newSet) {
            // 按需注册规则属性
            ClusterFlowRuleManager.registerPropertyIfAbsent(ns);
            ClusterParamFlowRuleManager.registerPropertyIfAbsent(ns);
            // 初始化 namespace 全局 QPS 限流器
            GlobalRequestLimiter.initIfAbsent(ns);
        }
    }

    private static class ServerGlobalTransportPropertyListener implements PropertyListener<ServerTransportConfig> {

        @Override
        public void configLoad(ServerTransportConfig config) {
            if (config == null) {
                RecordLog.warn("[ClusterServerConfigManager] Empty initial server transport config");
                return;
            }
            applyConfig(config);
        }

        @Override
        public void configUpdate(ServerTransportConfig config) {
            applyConfig(config);
        }

        private synchronized void applyConfig(ServerTransportConfig config) {
            if (!isValidTransportConfig(config)) {
                RecordLog.warn(
                    "[ClusterServerConfigManager] Invalid cluster server transport config, ignoring: {}", config);
                return;
            }
            RecordLog.info("[ClusterServerConfigManager] Updating new server transport config: {}", config);
            if (config.getIdleSeconds() != idleSeconds) {
                idleSeconds = config.getIdleSeconds();
            }
            updateTokenServer(config);
        }
    }

    private static void updateTokenServer(ServerTransportConfig config) {
        int newPort = config.getPort();
        AssertUtil.isTrue(newPort > 0, "token server port should be valid (positive)");
        if (newPort == port) {
            return;
        }
        ClusterServerConfigManager.port = newPort;

        for (ServerTransportConfigObserver observer : TRANSPORT_CONFIG_OBSERVERS) {
            observer.onTransportConfigChange(config);
        }
    }

    private static class ServerGlobalFlowPropertyListener implements PropertyListener<ServerFlowConfig> {

        @Override
        public void configUpdate(ServerFlowConfig config) {
            applyGlobalFlowConfig(config);
        }

        @Override
        public void configLoad(ServerFlowConfig config) {
            applyGlobalFlowConfig(config);
        }

        private synchronized void applyGlobalFlowConfig(ServerFlowConfig config) {
            if (!isValidFlowConfig(config)) {
                RecordLog.warn(
                    "[ClusterServerConfigManager] Invalid cluster server global flow config, ignoring: {}", config);
                return;
            }
            RecordLog.info("[ClusterServerConfigManager] Updating new server global flow config: {}", config);
            if (config.getExceedCount() != exceedCount) {
                exceedCount = config.getExceedCount();
            }
            if (config.getMaxOccupyRatio() != maxOccupyRatio) {
                maxOccupyRatio = config.getMaxOccupyRatio();
            }
            if (config.getMaxAllowedQps() != maxAllowedQps) {
                maxAllowedQps = config.getMaxAllowedQps();
                GlobalRequestLimiter.applyMaxQpsChange(maxAllowedQps);
            }
            int newIntervalMs = config.getIntervalMs();
            int newSampleCount = config.getSampleCount();
            if (newIntervalMs != intervalMs || newSampleCount != sampleCount) {
                if (newIntervalMs <= 0 || newSampleCount <= 0 || newIntervalMs % newSampleCount != 0) {
                    RecordLog.warn("[ClusterServerConfigManager] Ignoring invalid flow interval or sample count");
                } else {
                    intervalMs = newIntervalMs;
                    sampleCount = newSampleCount;
                    // 窗口参数变更时重置全部指标
                    ClusterMetricStatistics.resetFlowMetrics();
                    ClusterParamMetricStatistics.resetFlowMetrics();
                }
            }
        }
    }

    public static boolean isValidTransportConfig(ServerTransportConfig config) {
        return config != null && config.getPort() > 0 && config.getPort() <= 65535;
    }

    public static boolean isValidFlowConfig(ServerFlowConfig config) {
        return config != null && config.getMaxOccupyRatio() >= 0 && config.getExceedCount() >= 0
            && config.getMaxAllowedQps() >= 0
            && FlowRuleUtil.isWindowConfigValid(config.getSampleCount(), config.getIntervalMs());
    }

    public static double getExceedCount(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        ServerFlowConfig config = NAMESPACE_CONF.get(namespace);
        if (config != null) {
            return config.getExceedCount();
        }
        return exceedCount;
    }

    public static double getMaxOccupyRatio(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        ServerFlowConfig config = NAMESPACE_CONF.get(namespace);
        if (config != null) {
            return config.getMaxOccupyRatio();
        }
        return maxOccupyRatio;
    }

    public static int getIntervalMs(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        ServerFlowConfig config = NAMESPACE_CONF.get(namespace);
        if (config != null) {
            return config.getIntervalMs();
        }
        return intervalMs;
    }

    /**
     * 获取 namespace 采样桶数；无定制值时使用全局配置。
     *
     * @param namespace valid namespace
     * @return the sample count of namespace; if the namespace does not have customized value, use the global value
     */
    public static int getSampleCount(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        ServerFlowConfig config = NAMESPACE_CONF.get(namespace);
        if (config != null) {
            return config.getSampleCount();
        }
        return sampleCount;
    }

    public static double getMaxAllowedQps() {
        return maxAllowedQps;
    }

    public static double getMaxAllowedQps(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        ServerFlowConfig config = NAMESPACE_CONF.get(namespace);
        if (config != null) {
            return config.getMaxAllowedQps();
        }
        return maxAllowedQps;
    }

    public static double getExceedCount() {
        return exceedCount;
    }

    public static double getMaxOccupyRatio() {
        return maxOccupyRatio;
    }

    public static Set<String> getNamespaceSet() {
        return namespaceSet;
    }

    public static int getPort() {
        return port;
    }

    public static int getIdleSeconds() {
        return idleSeconds;
    }

    public static int getIntervalMs() {
        return intervalMs;
    }

    public static int getSampleCount() {
        return sampleCount;
    }

    public static void setNamespaceSet(Set<String> namespaceSet) {
        applyNamespaceSetChange(namespaceSet);
    }

    public static boolean isEmbedded() {
        return embedded;
    }

    /**
     * <p>设置 Token Server 嵌入式模式标志。</p>
     * <p>注意：开发者不应手动调用；由 Sentinel 启动 Token Server 时初始化。</p>
     *
     * @param embedded whether the token server is currently running in embedded mode
     */
    public static void setEmbedded(boolean embedded) {
        ClusterServerConfigManager.embedded = embedded;
    }

    public static void setMaxAllowedQps(double maxAllowedQps) {
        ClusterServerConfigManager.maxAllowedQps = maxAllowedQps;
    }

    private ClusterServerConfigManager() {}
}
