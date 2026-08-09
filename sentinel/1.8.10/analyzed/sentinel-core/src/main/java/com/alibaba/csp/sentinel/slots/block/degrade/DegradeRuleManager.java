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
package com.alibaba.csp.sentinel.slots.block.degrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.ExceptionCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.ResponseTimeCircuitBreaker;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * 熔断降级规则（{@link DegradeRule}）的管理器。
 *
 * @author youji.zj
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public final class DegradeRuleManager {

    private static volatile RuleManager<CircuitBreaker> circuitBreakers = new RuleManager<>(DegradeRuleManager::generateCbs, cb -> cb.getRule().isRegex());
    private static volatile RuleManager<DegradeRule> ruleMap = new RuleManager<>();

    private static final RulePropertyListener LISTENER = new RulePropertyListener();
    private static SentinelProperty<List<DegradeRule>> currentProperty
        = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(LISTENER);
    }

    /**
     * 监听 {@link DegradeRule} 的 {@link SentinelProperty}。该属性是 {@link DegradeRule}
     * 的配置来源；也可通过 {@link #loadRules(List)} 直接设置规则。
     *
     * @param property 要监听的属性
     */
    public static void register2Property(SentinelProperty<List<DegradeRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[DegradeRuleManager] Registering new property to degrade rule manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    static List<CircuitBreaker> getCircuitBreakers(String resourceName) {
        return circuitBreakers.getRules(resourceName);
    }

    public static boolean hasConfig(String resource) {
       return circuitBreakers.hasConfig(resource);
    }

    /**
     * <p>获取当前已加载的熔断降级规则。</p>
     * <p>注意：不要直接修改返回列表中的规则，否则行为<strong>未定义</strong>。</p>
     *
     * @return 已加载的熔断降级规则列表；若无规则则返回空列表
     */
    public static List<DegradeRule> getRules() {
        return ruleMap.getRules();
    }
    public static Set<DegradeRule> getRulesOfResource(String resource) {
        AssertUtil.assertNotBlank(resource, "resource name cannot be blank");
        return new HashSet<>(ruleMap.getRules(resource));
    }

    /**
     * 加载 {@link DegradeRule}，原有规则将被替换。
     *
     * @param rules 要加载的新规则
     */
    public static void loadRules(List<DegradeRule> rules) {
        try {
            currentProperty.updateValue(rules);
        } catch (Throwable e) {
            RecordLog.error("[DegradeRuleManager] Unexpected error when loading degrade rules", e);
        }
    }

    /**
     * 为指定资源设置熔断降级规则，该资源原有规则将被替换。
     *
     * @param resourceName 有效的资源名
     * @param rules        要加载的新规则集
     * @return 规则是否实际被更新
     * @since 1.5.0
     */
    public static boolean setRulesForResource(String resourceName, Set<DegradeRule> rules) {
        AssertUtil.notEmpty(resourceName, "resourceName cannot be empty");
        try {
            Map<String, List<DegradeRule>> newRuleMap = ruleMap.getOriginalRules();
            if (rules == null) {
                newRuleMap.remove(resourceName);
            } else {
                Set<DegradeRule> newSet = new HashSet<>();
                for (DegradeRule rule : rules) {
                    if (isValidRule(rule) && resourceName.equals(rule.getResource())) {
                        newSet.add(rule);
                    }
                }
                newRuleMap.put(resourceName, new ArrayList<>(newSet));
            }
            List<DegradeRule> allRules = new ArrayList<>();
            for (List<DegradeRule> set : newRuleMap.values()) {
                allRules.addAll(set);
            }
            return currentProperty.updateValue(allRules);
        } catch (Throwable e) {
            RecordLog.error("[DegradeRuleManager] Unexpected error when setting circuit breaking"
                + " rules for resource: " + resourceName, e);
            return false;
        }
    }

    private static CircuitBreaker getExistingSameCbOrNew(/*@Valid*/ DegradeRule rule) {
        List<CircuitBreaker> cbs = getCircuitBreakers(rule.getResource());
        if (cbs == null || cbs.isEmpty()) {
            return newCircuitBreakerFrom(rule);
        }
        for (CircuitBreaker cb : cbs) {
            if (rule.equals(cb.getRule())) {
                // Reuse the circuit breaker if the rule remains unchanged.
                return cb;
            }
        }
        return newCircuitBreakerFrom(rule);
    }

    /**
     * 根据给定的熔断降级规则创建熔断器实例。
     *
     * @param rule 有效的熔断降级规则
     * @return 基于规则创建的新熔断器；若规则无效或策略不支持则返回 null
     */
    private static CircuitBreaker newCircuitBreakerFrom(/*@Valid*/ DegradeRule rule) {
        switch (rule.getGrade()) {
            case RuleConstant.DEGRADE_GRADE_RT:
                return new ResponseTimeCircuitBreaker(rule);
            case RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO:
            case RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT:
                return new ExceptionCircuitBreaker(rule);
            default:
                return null;
        }
    }

    public static boolean isValidRule(DegradeRule rule) {
        boolean baseValid = rule != null && !StringUtil.isBlank(rule.getResource())
            && rule.getCount() >= 0 && rule.getTimeWindow() > 0;
        if (!baseValid) {
            return false;
        }
        if (rule.getMinRequestAmount() <= 0 || rule.getStatIntervalMs() <= 0) {
            return false;
        }
        if (!RuleManager.checkRegexResourceField(rule)) {
            return false;
        }
        switch (rule.getGrade()) {
            case RuleConstant.DEGRADE_GRADE_RT:
                return rule.getSlowRatioThreshold() >= 0 && rule.getSlowRatioThreshold() <= 1;
            case RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO:
                return rule.getCount() <= 1;
            case RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT:
                return true;
            default:
                return false;
        }
    }

    private static List<CircuitBreaker> generateCbs(List<CircuitBreaker> cbs) {
        return cbs.stream().map(cb -> newCircuitBreakerFrom(cb.getRule())).collect(Collectors.toList());
    }

    private static class RulePropertyListener implements PropertyListener<List<DegradeRule>> {

        private synchronized void reloadFrom(List<DegradeRule> list) {
            Map<String, List<CircuitBreaker>> cbs = buildCircuitBreakers(list);
            Map<String, List<DegradeRule>> rules = buildCircuitBreakerRules(cbs);
            circuitBreakers.updateRules(cbs);
            ruleMap.updateRules(rules);
        }

        @Override
        public void configUpdate(List<DegradeRule> conf) {
            reloadFrom(conf);
            RecordLog.info("[DegradeRuleManager] Degrade rules has been updated to: {}", ruleMap);
        }

        @Override
        public void configLoad(List<DegradeRule> conf) {
            reloadFrom(conf);
            RecordLog.info("[DegradeRuleManager] Degrade rules loaded: {}", ruleMap);
        }

        private Map<String, List<DegradeRule>> buildCircuitBreakerRules(Map<String, List<CircuitBreaker>> cbs) {
            Map<String, List<DegradeRule>> result = new HashMap<>(cbs.size());
            for (Map.Entry<String, List<CircuitBreaker>> entry : cbs.entrySet()) {
                String resource = entry.getKey();
                Set<DegradeRule> rules = entry.getValue().stream().map(CircuitBreaker::getRule).collect(Collectors.toSet());
                result.put(resource, new ArrayList<>(rules));
            }
            return result;
        }

        private Map<String, List<CircuitBreaker>> buildCircuitBreakers(List<DegradeRule> list) {
            Map<String, List<CircuitBreaker>> cbMap = new HashMap<>(8);
            if (list == null || list.isEmpty()) {
                return cbMap;
            }
            for (DegradeRule rule : list) {
                if (!isValidRule(rule)) {
                    RecordLog.warn("[DegradeRuleManager] Ignoring invalid rule when loading new rules: {}", rule);
                    continue;
                }

                if (StringUtil.isBlank(rule.getLimitApp())) {
                    rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
                }
                CircuitBreaker cb = getExistingSameCbOrNew(rule);
                if (cb == null) {
                    RecordLog.warn("[DegradeRuleManager] Unknown circuit breaking strategy, ignoring: {}", rule);
                    continue;
                }

                String resourceName = rule.getResource();

                List<CircuitBreaker> cbList = cbMap.get(resourceName);
                if (cbList == null) {
                    cbList = new ArrayList<>();
                    cbMap.put(resourceName, cbList);
                }
                cbList.add(cb);
            }
            return cbMap;
        }
    }
}
