/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.gateway.common.rule;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.GatewayRegexCache;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleUtil;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParameterMetric;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParameterMetricStorage;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网关流控规则管理器，负责加载规则并转换为参数流控规则。
 *
 * @author Eric Zhao
 * @since 1.6.0
 */
public final class GatewayRuleManager {

    /**
     * 网关流控规则映射：(resource, [rules...])。
     */
    private static final Map<String, Set<GatewayFlowRule>> GATEWAY_RULE_MAP = new ConcurrentHashMap<>();

    private static final Map<String, List<ParamFlowRule>> CONVERTED_PARAM_RULE_MAP = new ConcurrentHashMap<>();

    private static final GatewayRulePropertyListener LISTENER = new GatewayRulePropertyListener();
    private static final Set<Integer> FIELD_REQUIRED_SET = new HashSet<>(
            Arrays.asList(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM,
                    SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER,
                    SentinelGatewayConstants.PARAM_PARSE_STRATEGY_COOKIE)
    );
    private static SentinelProperty<Set<GatewayFlowRule>> currentProperty = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(LISTENER);
    }

    private GatewayRuleManager() {
    }

    public static void register2Property(SentinelProperty<Set<GatewayFlowRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[GatewayRuleManager] Registering new property to gateway flow rule manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    /**
     * 将所有给定网关规则加载到内存，并替换之前的规则。
     *
     * @param rules 规则集合
     * @return 若已更新则返回 true，否则 false
     */
    public static boolean loadRules(Set<GatewayFlowRule> rules) {
        return currentProperty.updateValue(rules);
    }

    public static Set<GatewayFlowRule> getRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        for (Set<GatewayFlowRule> ruleSet : GATEWAY_RULE_MAP.values()) {
            rules.addAll(ruleSet);
        }
        return rules;
    }

    public static Set<GatewayFlowRule> getRulesForResource(String resourceName) {
        if (StringUtil.isBlank(resourceName)) {
            return new HashSet<>();
        }
        Set<GatewayFlowRule> set = GATEWAY_RULE_MAP.get(resourceName);
        if (set == null) {
            return new HashSet<>();
        }
        return new HashSet<>(set);
    }

    /**
     * <p>获取所有已转换的参数流控规则。</p>
     * <p>注意：调用方不应修改返回的列表与规则。</p>
     *
     * @param resourceName 有效资源名
     * @return 已转换的参数流控规则
     */
    public static List<ParamFlowRule> getConvertedParamRules(String resourceName) {
        if (StringUtil.isBlank(resourceName)) {
            return new ArrayList<>();
        }
        return CONVERTED_PARAM_RULE_MAP.get(resourceName);
    }

    public static boolean isValidRule(GatewayFlowRule rule) {
        if (rule == null || StringUtil.isBlank(rule.getResource()) || rule.getResourceMode() < 0
                || rule.getGrade() < 0 || rule.getCount() < 0 || rule.getBurst() < 0 || rule.getControlBehavior() < 0) {
            return false;
        }
        if (rule.getControlBehavior() == RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER
                && rule.getMaxQueueingTimeoutMs() < 0) {
            return false;
        }
        if (rule.getIntervalSec() <= 0) {
            return false;
        }
        GatewayParamFlowItem item = rule.getParamItem();
        if (item != null) {
            return isValidParamItem(item);
        }
        return true;
    }

    static boolean isValidParamItem(/*@NonNull*/ GatewayParamFlowItem item) {
        if (item.getParseStrategy() < 0) {
            return false;
        }
        // 校验特定解析策略所需的字段名。
        if (FIELD_REQUIRED_SET.contains(item.getParseStrategy()) && StringUtil.isBlank(item.getFieldName())) {
            return false;
        }
        return StringUtil.isEmpty(item.getPattern()) || item.getMatchStrategy() >= 0;
    }

    private static final class GatewayRulePropertyListener implements PropertyListener<Set<GatewayFlowRule>> {

        @Override
        public void configUpdate(Set<GatewayFlowRule> conf) {
            applyGatewayRuleInternal(conf);
            RecordLog.info("[GatewayRuleManager] Gateway flow rules received: {}", GATEWAY_RULE_MAP);
        }

        @Override
        public void configLoad(Set<GatewayFlowRule> conf) {
            applyGatewayRuleInternal(conf);
            RecordLog.info("[GatewayRuleManager] Gateway flow rules loaded: {}", GATEWAY_RULE_MAP);
        }

        private int getIdxInternal(Map<String, Integer> idxMap, String resourceName) {
            // 准备索引映射。
            if (!idxMap.containsKey(resourceName)) {
                idxMap.put(resourceName, 0);
            }
            return idxMap.get(resourceName);
        }

        private void cacheRegexPattern(/*@NonNull*/ GatewayParamFlowItem item) {
            String pattern = item.getPattern();
            if (StringUtil.isNotEmpty(pattern) &&
                    item.getMatchStrategy() == SentinelGatewayConstants.PARAM_MATCH_STRATEGY_REGEX) {
                if (GatewayRegexCache.getRegexPattern(pattern) == null) {
                    GatewayRegexCache.addRegexPattern(pattern);
                }
            }
        }

        private synchronized void applyGatewayRuleInternal(Set<GatewayFlowRule> conf) {
            if (conf == null || conf.isEmpty()) {
                applyToConvertedParamMap(new HashSet<ParamFlowRule>());
                GATEWAY_RULE_MAP.clear();
                return;
            }
            Map<String, Set<GatewayFlowRule>> gatewayRuleMap = new ConcurrentHashMap<>();
            Map<String, Integer> idxMap = new HashMap<>();
            Set<ParamFlowRule> paramFlowRules = new HashSet<>();
            Map<String, List<GatewayFlowRule>> noParamMap = new HashMap<>();

            for (GatewayFlowRule rule : conf) {
                if (!isValidRule(rule)) {
                    RecordLog.warn("[GatewayRuleManager] Ignoring invalid rule when loading new rules: " + rule);
                    continue;
                }
                String resourceName = rule.getResource();
                if (rule.getParamItem() == null) {
                    // 暂存无参数配置的规则，稍后处理。
                    List<GatewayFlowRule> noParamList = noParamMap.get(resourceName);
                    if (noParamList == null) {
                        noParamList = new ArrayList<>();
                        noParamMap.put(resourceName, noParamList);
                    }
                    noParamList.add(rule);
                } else {
                    int idx = getIdxInternal(idxMap, resourceName);
                    // 转换为参数流控规则。
                    if (paramFlowRules.add(GatewayRuleConverter.applyToParamRule(rule, idx))) {
                        idxMap.put(rule.getResource(), idx + 1);
                    }
                    cacheRegexPattern(rule.getParamItem());
                }
                // 写入网关规则映射。
                Set<GatewayFlowRule> ruleSet = gatewayRuleMap.get(resourceName);
                if (ruleSet == null) {
                    ruleSet = new HashSet<>();
                    gatewayRuleMap.put(resourceName, ruleSet);
                }
                ruleSet.add(rule);
            }
            // 处理无参数模式的规则。
            for (Map.Entry<String, List<GatewayFlowRule>> e : noParamMap.entrySet()) {
                List<GatewayFlowRule> rules = e.getValue();
                if (rules == null || rules.isEmpty()) {
                    continue;
                }
                for (GatewayFlowRule rule : rules) {
                    int idx = getIdxInternal(idxMap, e.getKey());
                    // 始终使用同一索引（最后一个位置）。
                    paramFlowRules.add(GatewayRuleConverter.applyNonParamToParamRule(rule, idx));
                }
            }

            applyToConvertedParamMap(paramFlowRules);

            GATEWAY_RULE_MAP.clear();
            GATEWAY_RULE_MAP.putAll(gatewayRuleMap);
        }

        private void applyToConvertedParamMap(Set<ParamFlowRule> paramFlowRules) {
            Map<String, List<ParamFlowRule>> newRuleMap = ParamFlowRuleUtil.buildParamRuleMap(
                    new ArrayList<>(paramFlowRules));
            if (newRuleMap == null || newRuleMap.isEmpty()) {
                // 无参数流控规则，清除所有指标。
                for (String resource : CONVERTED_PARAM_RULE_MAP.keySet()) {
                    ParameterMetricStorage.clearParamMetricForResource(resource);
                }
                RecordLog.info("[GatewayRuleManager] No gateway rules, clearing parameter metrics of previous rules");
                CONVERTED_PARAM_RULE_MAP.clear();
                return;
            }

            // 清除不再使用的参数指标。
            for (Map.Entry<String, List<ParamFlowRule>> entry : CONVERTED_PARAM_RULE_MAP.entrySet()) {
                String resource = entry.getKey();
                if (!newRuleMap.containsKey(resource)) {
                    ParameterMetricStorage.clearParamMetricForResource(resource);
                    continue;
                }
                List<ParamFlowRule> newRuleList = newRuleMap.get(resource);
                List<ParamFlowRule> oldRuleList = new ArrayList<>(entry.getValue());
                oldRuleList.removeAll(newRuleList);
                for (ParamFlowRule rule : oldRuleList) {
                    ParameterMetric metric = ParameterMetricStorage.getParamMetricForResource(resource);
                    if (null != metric) {
                        metric.clearForRule(rule);
                    }
                }
            }

            // 写入转换后的规则映射。
            CONVERTED_PARAM_RULE_MAP.clear();
            CONVERTED_PARAM_RULE_MAP.putAll(newRuleMap);

            RecordLog.info("[GatewayRuleManager] Converted internal param rules: {}", CONVERTED_PARAM_RULE_MAP);
        }
    }
}
