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
package com.alibaba.csp.sentinel.cluster.flow.rule;

import com.alibaba.csp.sentinel.cluster.flow.statistic.ClusterMetricStatistics;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.CurrentConcurrencyManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.metric.ClusterMetric;
import com.alibaba.csp.sentinel.cluster.server.ServerConstants;
import com.alibaba.csp.sentinel.cluster.server.connection.ConnectionManager;
import com.alibaba.csp.sentinel.cluster.server.util.ClusterRuleUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Function;
import com.alibaba.csp.sentinel.util.function.Predicate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 集群流控规则管理器：按 namespace 监听 {@link FlowRule} 动态属性并维护内存规则表。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterFlowRuleManager {

    /**
     * 默认属性供应器：为 namespace 创建 {@link DynamicSentinelProperty} 以手动管理规则。
     */
    public static final Function<String, SentinelProperty<List<FlowRule>>> DEFAULT_PROPERTY_SUPPLIER =
            new Function<String, SentinelProperty<List<FlowRule>>>() {
                @Override
                public SentinelProperty<List<FlowRule>> apply(String namespace) {
                    return new DynamicSentinelProperty<>();
                }
            };

    /**
     * (flowId, 集群流控规则)
     */
    private static final Map<Long, FlowRule> FLOW_RULES = new ConcurrentHashMap<>();
    /**
     * (namespace, flowId 集合)
     */
    private static final Map<String, Set<Long>> NAMESPACE_FLOW_ID_MAP = new ConcurrentHashMap<>();
    /**
     * <p>(flowId, namespace) 映射，校验规则 {@code ruleId} 时用于查询连接数：</p>
     * <pre>
     * ruleId -> namespace -> connection group -> connected count
     * </pre>
     */
    private static final Map<Long, String> FLOW_NAMESPACE_MAP = new ConcurrentHashMap<>();

    /**
     * (namespace, 属性与监听器包装)
     */
    private static final Map<String, NamespaceFlowProperty<FlowRule>> PROPERTY_MAP = new ConcurrentHashMap<>();
    /**
     * 按 namespace 提供集群流控规则动态属性的供应器。
     */
    private static volatile Function<String, SentinelProperty<List<FlowRule>>> propertySupplier
            = DEFAULT_PROPERTY_SUPPLIER;

    private static final Object UPDATE_LOCK = new Object();

    static {
        initDefaultProperty();
    }

    private static void initDefaultProperty() {
        // 服务端始终支持 default namespace，预注册默认属性
        SentinelProperty<List<FlowRule>> defaultProperty = new DynamicSentinelProperty<>();
        String defaultNamespace = ServerConstants.DEFAULT_NAMESPACE;
        registerPropertyInternal(defaultNamespace, defaultProperty);
    }

    public static void setPropertySupplier(Function<String, SentinelProperty<List<FlowRule>>> propertySupplier) {
        AssertUtil.notNull(propertySupplier, "flow rule property supplier cannot be null");
        ClusterFlowRuleManager.propertySupplier = propertySupplier;
    }

    /**
     * 监听指定 namespace 的集群 {@link FlowRule} 动态属性。
     *
     * @param namespace namespace to register
     */
    public static void register2Property(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        if (propertySupplier == null) {
            RecordLog.warn(
                    "[ClusterFlowRuleManager] Cluster flow property supplier is absent, cannot register property");
            return;
        }
        SentinelProperty<List<FlowRule>> property = propertySupplier.apply(namespace);
        if (property == null) {
            RecordLog.warn(
                    "[ClusterFlowRuleManager] Wrong created property from cluster flow property supplier, ignoring");
            return;
        }
        synchronized (UPDATE_LOCK) {
            RecordLog.info("[ClusterFlowRuleManager] Registering new property to cluster flow rule manager"
                    + " for namespace <{}>", namespace);
            registerPropertyInternal(namespace, property);
        }
    }

    /**
     * namespace 尚无属性时注册集群 {@link FlowRule} 动态属性。
     *
     * @param namespace namespace to register
     */
    public static void registerPropertyIfAbsent(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        if (!PROPERTY_MAP.containsKey(namespace)) {
            synchronized (UPDATE_LOCK) {
                if (!PROPERTY_MAP.containsKey(namespace)) {
                    register2Property(namespace);
                }
            }
        }
    }

    private static void registerPropertyInternal(/*@NonNull*/ String namespace, /*@Valid*/
                                                              SentinelProperty<List<FlowRule>> property) {
        NamespaceFlowProperty<FlowRule> oldProperty = PROPERTY_MAP.get(namespace);
        if (oldProperty != null) {
            oldProperty.getProperty().removeListener(oldProperty.getListener());
        }
        PropertyListener<List<FlowRule>> listener = new FlowRulePropertyListener(namespace);
        property.addListener(listener);
        PROPERTY_MAP.put(namespace, new NamespaceFlowProperty<>(namespace, property, listener));
        Set<Long> flowIdSet = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (flowIdSet == null) {
            resetNamespaceFlowIdMapFor(namespace);
        }
    }

    /**
     * 移除指定 namespace 的集群流控规则属性。
     *
     * @param namespace valid namespace
     */
    public static void removeProperty(String namespace) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        synchronized (UPDATE_LOCK) {
            NamespaceFlowProperty<FlowRule> property = PROPERTY_MAP.get(namespace);
            if (property != null) {
                property.getProperty().removeListener(property.getListener());
                PROPERTY_MAP.remove(namespace);
            }
            RecordLog.info("[ClusterFlowRuleManager] Removing property from cluster flow rule manager"
                    + " for namespace <{}>", namespace);
        }
    }

    private static void removePropertyListeners() {
        for (NamespaceFlowProperty<FlowRule> property : PROPERTY_MAP.values()) {
            property.getProperty().removeListener(property.getListener());
        }
    }

    private static void restorePropertyListeners() {
        for (NamespaceFlowProperty<FlowRule> p : PROPERTY_MAP.values()) {
            p.getProperty().removeListener(p.getListener());
            p.getProperty().addListener(p.getListener());
        }
    }

    /**
     * 按 flowId 获取集群流控规则。
     *
     * @param id rule ID
     * @return flow rule
     */
    public static FlowRule getFlowRuleById(Long id) {
        if (!ClusterRuleUtil.validId(id)) {
            return null;
        }
        return FLOW_RULES.get(id);
    }

    public static Set<Long> getFlowIdSet(String namespace) {
        if (StringUtil.isEmpty(namespace)) {
            return new HashSet<>();
        }
        Set<Long> set = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (set == null) {
            return new HashSet<>();
        }
        return new HashSet<>(set);
    }

    public static List<FlowRule> getAllFlowRules() {
        return new ArrayList<>(FLOW_RULES.values());
    }

    /**
     * 获取 namespace 下全部集群流控规则。
     *
     * @param namespace valid namespace
     * @return cluster flow rules within the provided namespace
     */
    public static List<FlowRule> getFlowRules(String namespace) {
        if (StringUtil.isEmpty(namespace)) {
            return new ArrayList<>();
        }
        List<FlowRule> rules = new ArrayList<>();
        Set<Long> flowIdSet = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (flowIdSet == null || flowIdSet.isEmpty()) {
            return rules;
        }
        for (Long flowId : flowIdSet) {
            FlowRule rule = FLOW_RULES.get(flowId);
            if (rule != null) {
                rules.add(rule);
            }
        }
        return rules;
    }

    /**
     * 加载 namespace 集群流控规则，覆盖原有规则。
     *
     * @param namespace a valid namespace
     * @param rules     rule list
     */
    public static void loadRules(String namespace, List<FlowRule> rules) {
        AssertUtil.notEmpty(namespace, "namespace cannot be empty");
        NamespaceFlowProperty<FlowRule> property = PROPERTY_MAP.get(namespace);
        if (property != null) {
            property.getProperty().updateValue(rules);
        }
    }

    private static void resetNamespaceFlowIdMapFor(/*@Valid*/ String namespace) {
        NAMESPACE_FLOW_ID_MAP.put(namespace, new HashSet<Long>());
    }

    /**
     * 清空 namespace 下全部规则并重置映射。
     *
     * @param namespace valid namespace
     */
    private static void clearAndResetRulesFor(/*@Valid*/ String namespace) {
        Set<Long> flowIdSet = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (flowIdSet != null && !flowIdSet.isEmpty()) {
            for (Long flowId : flowIdSet) {
                FLOW_RULES.remove(flowId);
                FLOW_NAMESPACE_MAP.remove(flowId);
                if (CurrentConcurrencyManager.containsFlowId(flowId)) {
                    CurrentConcurrencyManager.remove(flowId);
                }
            }
            flowIdSet.clear();
        } else {
            resetNamespaceFlowIdMapFor(namespace);
        }
    }

    private static void clearAndResetRulesConditional(/*@Valid*/ String namespace, Predicate<Long> predicate) {
        Set<Long> oldIdSet = NAMESPACE_FLOW_ID_MAP.get(namespace);
        if (oldIdSet != null && !oldIdSet.isEmpty()) {
            for (Long flowId : oldIdSet) {
                if (predicate.test(flowId)) {
                    FLOW_RULES.remove(flowId);
                    FLOW_NAMESPACE_MAP.remove(flowId);
                    ClusterMetricStatistics.removeMetric(flowId);
                    if (CurrentConcurrencyManager.containsFlowId(flowId)) {
                        CurrentConcurrencyManager.remove(flowId);
                    }
                }
            }
            oldIdSet.clear();
        }
    }

    /**
     * 获取 flowId 对应 namespace 的客户端连接数。
     *
     * @param flowId unique flow ID
     * @return connected count
     */
    public static int getConnectedCount(long flowId) {
        if (flowId <= 0) {
            return 0;
        }
        String namespace = FLOW_NAMESPACE_MAP.get(flowId);
        if (namespace == null) {
            return 0;
        }
        return ConnectionManager.getConnectedCount(namespace);
    }

    public static String getNamespace(long flowId) {
        return FLOW_NAMESPACE_MAP.get(flowId);
    }

    private static void applyClusterFlowRule(List<FlowRule> list, /*@Valid*/ String namespace) {
        if (list == null || list.isEmpty()) {
            clearAndResetRulesFor(namespace);
            return;
        }
        final ConcurrentHashMap<Long, FlowRule> ruleMap = new ConcurrentHashMap<>();

        Set<Long> flowIdSet = new HashSet<>();

        for (FlowRule rule : list) {
            if (!rule.isClusterMode()) {
                continue;
            }
            if (!FlowRuleUtil.isValidRule(rule)) {
                RecordLog.warn(
                        "[ClusterFlowRuleManager] Ignoring invalid flow rule when loading new flow rules: " + rule);
                continue;
            }
            if (StringUtil.isBlank(rule.getLimitApp())) {
                rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
            }

            // 过滤后 flowId 不应为空
            ClusterFlowConfig clusterConfig = rule.getClusterConfig();
            Long flowId = clusterConfig.getFlowId();
            if (flowId == null) {
                continue;
            }
            ruleMap.put(flowId, rule);
            FLOW_NAMESPACE_MAP.put(flowId, namespace);
            flowIdSet.add(flowId);
            if (!CurrentConcurrencyManager.containsFlowId(flowId)) {
                CurrentConcurrencyManager.put(flowId, 0);
            }

            // 为有效 flowId 初始化集群指标
            ClusterMetricStatistics.putMetricIfAbsent(flowId,
                    new ClusterMetric(clusterConfig.getSampleCount(), clusterConfig.getWindowIntervalMs()));
        }

        // 清理不再使用的集群指标
        clearAndResetRulesConditional(namespace, new Predicate<Long>() {
            @Override
            public boolean test(Long flowId) {
                return !ruleMap.containsKey(flowId);
            }
        });

        FLOW_RULES.putAll(ruleMap);
        NAMESPACE_FLOW_ID_MAP.put(namespace, flowIdSet);
    }

    private static final class FlowRulePropertyListener implements PropertyListener<List<FlowRule>> {

        private final String namespace;

        public FlowRulePropertyListener(String namespace) {
            this.namespace = namespace;
        }

        @Override
        public synchronized void configUpdate(List<FlowRule> conf) {
            applyClusterFlowRule(conf, namespace);
            RecordLog.info("[ClusterFlowRuleManager] Cluster flow rules received for namespace <{}>: {}",
                    namespace, FLOW_RULES);
        }

        @Override
        public synchronized void configLoad(List<FlowRule> conf) {
            applyClusterFlowRule(conf, namespace);
            RecordLog.info("[ClusterFlowRuleManager] Cluster flow rules loaded for namespace <{}>: {}",
                    namespace, FLOW_RULES);
        }
    }

    private ClusterFlowRuleManager() {
    }
}
