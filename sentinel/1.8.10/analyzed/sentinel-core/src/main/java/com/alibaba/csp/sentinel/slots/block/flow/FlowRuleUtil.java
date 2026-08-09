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
package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.controller.DefaultController;
import com.alibaba.csp.sentinel.slots.block.flow.controller.ThrottlingController;
import com.alibaba.csp.sentinel.slots.block.flow.controller.WarmUpController;
import com.alibaba.csp.sentinel.slots.block.flow.controller.WarmUpRateLimiterController;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Function;
import com.alibaba.csp.sentinel.util.function.Predicate;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流控规则工具类：构建规则映射、校验规则合法性并生成流量整形控制器。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class FlowRuleUtil {

    /**
     * 从原始流控规则列表按资源名分组构建规则映射。
     *
     * @param list 原始流控规则列表
     * @return 新构建的规则映射；列表为空或无有效规则时返回空映射
     */
    public static Map<String, List<FlowRule>> buildFlowRuleMap(List<FlowRule> list) {
        return buildFlowRuleMap(list, null);
    }

    /**
     * 从原始流控规则列表按资源名分组构建规则映射，支持过滤器。
     *
     * @param list   原始流控规则列表
     * @param filter 规则过滤器
     * @return 新构建的规则映射；无匹配规则时返回空映射
     */
    public static Map<String, List<FlowRule>> buildFlowRuleMap(List<FlowRule> list, Predicate<FlowRule> filter) {
        return buildFlowRuleMap(list, filter, true);
    }

    /**
     * 从原始流控规则列表按资源名分组构建规则映射，可选排序。
     *
     * @param list       原始流控规则列表
     * @param filter     规则过滤器
     * @param shouldSort 是否对规则排序
     * @return 新构建的规则映射
     */
    public static Map<String, List<FlowRule>> buildFlowRuleMap(List<FlowRule> list, Predicate<FlowRule> filter,
                                                               boolean shouldSort) {
        return buildFlowRuleMap(list, extractResource, filter, shouldSort);
    }

    /**
     * 从原始流控规则列表按自定义分组函数构建规则映射。
     *
     * @param list          原始流控规则列表
     * @param groupFunction 分组键提取函数
     * @param filter        规则过滤器
     * @param shouldSort    是否对规则排序
     * @param <K>           映射键类型
     * @return 新构建的规则映射
     */
    public static <K> Map<K, List<FlowRule>> buildFlowRuleMap(List<FlowRule> list, Function<FlowRule, K> groupFunction,
                                                              Predicate<FlowRule> filter, boolean shouldSort) {
        Map<K, List<FlowRule>> newRuleMap = new ConcurrentHashMap<>();
        if (list == null || list.isEmpty()) {
            return newRuleMap;
        }
        Map<K, Set<FlowRule>> tmpMap = new ConcurrentHashMap<>();

        for (FlowRule rule : list) {
            if (!isValidRule(rule)) {
                RecordLog.warn("[FlowRuleManager] Ignoring invalid flow rule when loading new flow rules: " + rule);
                continue;
            }
            if (filter != null && !filter.test(rule)) {
                continue;
            }
            if (StringUtil.isBlank(rule.getLimitApp())) {
                rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
            }
            TrafficShapingController rater = generateRater(rule);
            rule.setRater(rater);

            K key = groupFunction.apply(rule);
            if (key == null) {
                continue;
            }
            Set<FlowRule> flowRules = tmpMap.get(key);

            if (flowRules == null) {
                // Use hash set here to remove duplicate rules.
                flowRules = new HashSet<>();
                tmpMap.put(key, flowRules);
            }

            flowRules.add(rule);
        }
        Comparator<FlowRule> comparator = new FlowRuleComparator();
        for (Entry<K, Set<FlowRule>> entries : tmpMap.entrySet()) {
            List<FlowRule> rules = new ArrayList<>(entries.getValue());
            if (shouldSort) {
                // Sort the rules.
                Collections.sort(rules, comparator);
            }
            newRuleMap.put(entries.getKey(), rules);
        }

        return newRuleMap;
    }

    private static TrafficShapingController generateRater(/*@Valid*/ FlowRule rule) {
        if (rule.getGrade() == RuleConstant.FLOW_GRADE_QPS) {
            switch (rule.getControlBehavior()) {
                case RuleConstant.CONTROL_BEHAVIOR_WARM_UP:
                    return new WarmUpController(rule.getCount(), rule.getWarmUpPeriodSec(),
                            ColdFactorProperty.coldFactor);
                case RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER:
                    return new ThrottlingController(rule.getMaxQueueingTimeMs(), rule.getCount());
                case RuleConstant.CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER:
                    return new WarmUpRateLimiterController(rule.getCount(), rule.getWarmUpPeriodSec(),
                            rule.getMaxQueueingTimeMs(), ColdFactorProperty.coldFactor);
                case RuleConstant.CONTROL_BEHAVIOR_DEFAULT:
                default:
                    // Default mode or unknown mode: default traffic shaping controller (fast-reject).
            }
        }
        return new DefaultController(rule.getCount(), rule.getGrade());
    }

    /**
     * 校验集群流控 ID 是否有效。
     *
     * @param id 待校验的流控 ID
     * @return 有效返回 true，否则 false
     */
    public static boolean validClusterRuleId(Long id) {
        return id != null && id > 0;
    }

    /**
     * 校验流控规则是否合法。
     *
     * @param rule 待校验的流控规则
     * @return 合法返回 true，否则 false
     */
    public static boolean isValidRule(FlowRule rule) {
        boolean baseValid = rule != null && !StringUtil.isBlank(rule.getResource()) && rule.getCount() >= 0
                && rule.getGrade() >= 0 && rule.getStrategy() >= 0 && rule.getControlBehavior() >= 0;
        if (!baseValid) {
            return false;
        }
        if (!checkRegexField(rule)) {
            return false;
        }
        if (rule.getGrade() == RuleConstant.FLOW_GRADE_QPS) {
            // Check strategy and control (shaping) behavior.
            return checkClusterField(rule) && checkStrategyField(rule) && checkControlBehaviorField(rule);
        } else if (rule.getGrade() == RuleConstant.FLOW_GRADE_THREAD) {
            return checkClusterConcurrentField(rule);
        } else {
            return false;
        }

    }

    /** 校验线程数流控的集群配置字段。 */
    public static boolean checkClusterConcurrentField(/*@NonNull*/ FlowRule rule) {
        if (!rule.isClusterMode()) {
            return true;
        }
        ClusterFlowConfig clusterConfig = rule.getClusterConfig();
        if (clusterConfig == null) {
            return false;
        }
        if (clusterConfig.getClientOfflineTime() <= 0 || clusterConfig.getResourceTimeout() <= 0) {
            return false;
        }

        if (clusterConfig.getAcquireRefuseStrategy() < 0 || clusterConfig.getResourceTimeoutStrategy() < 0) {
            return false;
        }

        if (!validClusterRuleId(clusterConfig.getFlowId())) {
            return false;
        }

        return isWindowConfigValid(clusterConfig.getSampleCount(), clusterConfig.getWindowIntervalMs());
    }

    private static boolean checkClusterField(/*@NonNull*/ FlowRule rule) {
        if (!rule.isClusterMode()) {
            return true;
        }
        ClusterFlowConfig clusterConfig = rule.getClusterConfig();
        if (clusterConfig == null) {
            return false;
        }
        if (!validClusterRuleId(clusterConfig.getFlowId())) {
            return false;
        }
        if (!isWindowConfigValid(clusterConfig.getSampleCount(), clusterConfig.getWindowIntervalMs())) {
            return false;
        }
        switch (clusterConfig.getStrategy()) {
            case ClusterRuleConstant.FLOW_CLUSTER_STRATEGY_NORMAL:
                return true;
            default:
                return false;
        }
    }

    /** 校验滑动窗口采样数与时间间隔是否合法（间隔须能被采样数整除）。 */
    public static boolean isWindowConfigValid(int sampleCount, int windowIntervalMs) {
        return sampleCount > 0 && windowIntervalMs > 0 && windowIntervalMs % sampleCount == 0;
    }

    private static boolean checkStrategyField(/*@NonNull*/ FlowRule rule) {
        if (rule.getStrategy() == RuleConstant.STRATEGY_RELATE || rule.getStrategy() == RuleConstant.STRATEGY_CHAIN) {
            return StringUtil.isNotBlank(rule.getRefResource());
        }
        return true;
    }

    private static boolean checkRegexField(FlowRule rule) {
        if (!RuleManager.checkRegexResourceField(rule)) {
            return false;
        }
        if (rule.isRegex()) {
            return !rule.isClusterMode() && rule.getControlBehavior() == RuleConstant.CONTROL_BEHAVIOR_DEFAULT;
        }
        return true;
    }

    private static boolean checkControlBehaviorField(/*@NonNull*/ FlowRule rule) {
        switch (rule.getControlBehavior()) {
            case RuleConstant.CONTROL_BEHAVIOR_WARM_UP:
                return rule.getWarmUpPeriodSec() > 0;
            case RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER:
                return rule.getMaxQueueingTimeMs() > 0;
            case RuleConstant.CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER:
                return rule.getWarmUpPeriodSec() > 0 && rule.getMaxQueueingTimeMs() > 0;
            default:
                return true;
        }
    }

    private static final Function<FlowRule, String> extractResource = new Function<FlowRule, String>() {
        @Override
        public String apply(FlowRule rule) {
            return rule.getResource();
        }
    };

    private FlowRuleUtil() {
    }
}
