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
package com.alibaba.csp.sentinel.slots.block.flow.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleManager;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * 热点参数流控规则管理器：加载、监听与按资源聚合规则。
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 0.2.0
 */
public final class ParamFlowRuleManager {

    private static final RuleManager<ParamFlowRule> PARAM_FLOW_RULES = new RuleManager<>();
    private final static RulePropertyListener PROPERTY_LISTENER = new RulePropertyListener();
    private static SentinelProperty<List<ParamFlowRule>> currentProperty = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(PROPERTY_LISTENER);
    }

    /**
     * 加载热点参数流控规则，全量替换已有规则。
     *
     * @param rules 新规则列表
     */
    public static void loadRules(List<ParamFlowRule> rules) {
        try {
            currentProperty.updateValue(rules);
        } catch (Throwable e) {
            RecordLog.info("[ParamFlowRuleManager] Failed to load rules", e);
        }
    }

    /**
     * 注册 {@link SentinelProperty} 作为规则来源并监听变更；
     * 也可直接调用 {@link #loadRules(List)} 设置规则。
     *
     * @param property 动态属性
     */
    public static void register2Property(SentinelProperty<List<ParamFlowRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (PROPERTY_LISTENER) {
            currentProperty.removeListener(PROPERTY_LISTENER);
            property.addListener(PROPERTY_LISTENER);
            currentProperty = property;
            RecordLog.info("[ParamFlowRuleManager] New property has been registered to hot param rule manager");
        }
    }

    public static List<ParamFlowRule> getRulesOfResource(String resourceName) {
        return new ArrayList<>(PARAM_FLOW_RULES.getRules(resourceName));
    }

    public static boolean hasRules(String resourceName) {
        return PARAM_FLOW_RULES.hasConfig(resourceName);
    }

    /**
     * 获取全部规则的副本。
     *
     * @return 规则列表副本
     */
    public static List<ParamFlowRule> getRules() {
        return PARAM_FLOW_RULES.getRules();
    }

    static class RulePropertyListener implements PropertyListener<List<ParamFlowRule>> {

        @Override
        public void configUpdate(List<ParamFlowRule> list) {
            Map<String, List<ParamFlowRule>> rules = aggregateAndPrepareParamRules(list);
            PARAM_FLOW_RULES.updateRules(rules);
            RecordLog.info("[ParamFlowRuleManager] Parameter flow rules received: {}", PARAM_FLOW_RULES);
        }

        @Override
        public void configLoad(List<ParamFlowRule> list) {
            Map<String, List<ParamFlowRule>> rules = aggregateAndPrepareParamRules(list);
            PARAM_FLOW_RULES.updateRules(rules);
            RecordLog.info("[ParamFlowRuleManager] Parameter flow rules received: {}", PARAM_FLOW_RULES);
        }

        private Map<String, List<ParamFlowRule>> aggregateAndPrepareParamRules(List<ParamFlowRule> list) {
            Map<String, List<ParamFlowRule>> newRuleMap = ParamFlowRuleUtil.buildParamRuleMap(list);
            if (newRuleMap == null || newRuleMap.isEmpty()) {
                // 无参数流控规则时清空全部参数指标
                ParameterMetricStorage.getMetricsMap().clear();
                RecordLog.info("[ParamFlowRuleManager] No parameter flow rules, clearing all parameter metrics");
                return newRuleMap;
            }

            // 清理已移除资源或规则的参数指标
            for (Map.Entry<String, List<ParamFlowRule>> entry : PARAM_FLOW_RULES.getOriginalRules().entrySet()) {
                String resource = entry.getKey();
                if (!newRuleMap.containsKey(resource)) {
                    ParameterMetricStorage.clearParamMetricForResource(resource);
                    continue;
                }
                List<ParamFlowRule> newRuleList = newRuleMap.get(resource);
                List<ParamFlowRule> oldRuleList = new ArrayList<>(entry.getValue());
                oldRuleList.removeAll(newRuleList);
                for (ParamFlowRule rule : oldRuleList) {
                    ParameterMetric parameterMetric = ParameterMetricStorage.getParamMetricForResource(resource);
                    if (parameterMetric != null) {
                        parameterMetric.clearForRule(rule);
                    }
                }
            }

            return newRuleMap;
        }
    }

    private ParamFlowRuleManager() {}
}
