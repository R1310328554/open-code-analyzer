/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.ExceptionCircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.ResponseTimeCircuitBreaker;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用默认熔断规则的管理器。
 *
 * @author wuwen
 * @author Eric Zhao
 * @since 2.0.0
 */
public final class DefaultCircuitBreakerRuleManager {

    public static final String DEFAULT_KEY = "*";

    private static volatile Map<String, List<CircuitBreaker>> circuitBreakers = new ConcurrentHashMap<>();

    private static volatile Set<DegradeRule> rules = new HashSet<>();

    /**
     * 此集合中的资源不受默认规则影响。
     */
    private static final Set<String> excludedResource = ConcurrentHashMap.newKeySet();

    private static final DefaultCircuitBreakerRuleManager.RulePropertyListener LISTENER
        = new DefaultCircuitBreakerRuleManager.RulePropertyListener();
    private static SentinelProperty<List<DegradeRule>> currentProperty = new DynamicSentinelProperty<>();

    static {
        currentProperty.addListener(LISTENER);
    }

    /**
     * 监听默认熔断规则的 {@link SentinelProperty}。
     *
     * @param property 要监听的属性
     */
    public static void register2Property(SentinelProperty<List<DegradeRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("Registering new property to DefaultCircuitBreakerRuleManager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    static List<CircuitBreaker> getDefaultCircuitBreakers(String resourceName) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        List<CircuitBreaker> circuitBreakers = DefaultCircuitBreakerRuleManager.circuitBreakers.get(resourceName);
        if (circuitBreakers == null && !rules.isEmpty() && !excludedResource.contains(resourceName)) {
            circuitBreakers = new ArrayList<>();
            for (DegradeRule rule : rules) {
                circuitBreakers.add(DefaultCircuitBreakerRuleManager.newCircuitBreakerFrom(rule));
            }
            DefaultCircuitBreakerRuleManager.circuitBreakers.put(resourceName, circuitBreakers);
            return circuitBreakers;
        }
        return circuitBreakers;
    }

    /**
     * 将不需要默认规则的资源加入排除列表。
     *
     * @param resourceName 不需要默认规则的资源名
     */
    public static void addExcludedResource(String resourceName) {
        if (StringUtil.isEmpty(resourceName)) {
            return;
        }
        excludedResource.add(resourceName);
    }

    public static void removeExcludedResource(String resourceName) {
        if (StringUtil.isEmpty(resourceName)) {
            return;
        }
        excludedResource.remove(resourceName);
    }

    public static void clearExcludedResource() {
        excludedResource.clear();
    }

    /**
     * 加载默认熔断规则，原有规则将被替换。
     *
     * @param rules 要加载的新规则
     */
    public static boolean loadRules(List<DegradeRule> rules) {
        try {
            return currentProperty.updateValue(rules);
        } catch (Throwable e) {
            RecordLog.error("[DefaultCircuitBreakerRuleManager] Unexpected error when loading default rules", e);
            return false;
        }

    }

    public static boolean isValidDefaultRule(DegradeRule rule) {
        if (!DegradeRuleManager.isValidRule(rule)) {
            return false;
        }
        return rule.getResource().equals(DEFAULT_KEY);
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

    static List<CircuitBreaker> getCircuitBreakers(String resourceName) {
        return circuitBreakers.get(resourceName);
    }

    private static class RulePropertyListener implements PropertyListener<List<DegradeRule>> {

        private synchronized void reloadFrom(List<DegradeRule> list) {

            if (list == null || list.isEmpty()) {
                // clearing all rules
                DefaultCircuitBreakerRuleManager.circuitBreakers = new ConcurrentHashMap<>();
                DefaultCircuitBreakerRuleManager.rules = new HashSet<>();
                return;
            }

            Set<DegradeRule> rules = new HashSet<DegradeRule>();
            for (DegradeRule rule : list) {
                if (!isValidDefaultRule(rule)) {
                    RecordLog.warn(
                        "[DefaultCircuitBreakerRuleManager] Ignoring invalid rule when loading new rules: {}", rule);
                } else {
                    if (StringUtil.isBlank(rule.getLimitApp())) {
                        rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
                    }
                    // TODO: Set a special ID for default circuit breaker rule (so that it could be identified)

                    rules.add(rule);
                }
            }

            Map<String, List<CircuitBreaker>> cbMap = new ConcurrentHashMap<String, List<CircuitBreaker>>(8);
            for (String resourceNameKey : DefaultCircuitBreakerRuleManager.circuitBreakers.keySet()) {
                List<CircuitBreaker> cbs = new ArrayList<CircuitBreaker>();
                for (DegradeRule rule : rules) {
                    CircuitBreaker cb = getExistingSameCbOrNew(rule);
                    cbs.add(cb);
                }
                cbMap.put(resourceNameKey, cbs);
            }

            DefaultCircuitBreakerRuleManager.rules = rules;
            DefaultCircuitBreakerRuleManager.circuitBreakers = cbMap;
        }

        @Override
        public void configUpdate(List<DegradeRule> conf) {
            reloadFrom(conf);
            RecordLog.info("[DefaultCircuitBreakerRuleManager] Default circuit breaker rules has been updated to: {}",
                rules);
        }

        @Override
        public void configLoad(List<DegradeRule> conf) {
            reloadFrom(conf);
            RecordLog.info("[DefaultCircuitBreakerRuleManager] Default circuit breaker rules loaded: {}", rules);
        }
    }
}
