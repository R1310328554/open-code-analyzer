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

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.metric.MetricTimerListener;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleManager;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 一个资源可配置多条流控规则，生效顺序如下：
 * <ol>
 * <li>指定调用来源的规则</li>
 * <li>未指定调用来源的规则</li>
 * </ol>
 * </p>
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @author Weihua
 */
public class FlowRuleManager {

    private static volatile RuleManager<FlowRule> flowRules = new RuleManager<>();

    private static final FlowPropertyListener LISTENER = new FlowPropertyListener();
    private static SentinelProperty<List<FlowRule>> currentProperty = new DynamicSentinelProperty<List<FlowRule>>();

    /** the corePool size of SCHEDULER must be set at 1, so the two task ({@link #startMetricTimerListener()} can run orderly by the SCHEDULER **/
    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1,
        new NamedThreadFactory("sentinel-metrics-record-task", true));

    static {
        currentProperty.addListener(LISTENER);
        startMetricTimerListener();
    }

    /**
     * <p>启动 {@link MetricTimerListener} 定时任务：</p>
     * <ol>
     *     <li>flushInterval &gt; 0 时，按该间隔周期性运行；</li>
     *     <li>flushInterval ≤ 0 或配置无效时，不启动定时器。</li>
     * </ol>
     */
    private static void startMetricTimerListener() {
        long flushInterval = SentinelConfig.metricLogFlushIntervalSec();
        if (flushInterval <= 0) {
            RecordLog.info("[FlowRuleManager] The MetricTimerListener isn't started. If you want to start it, "
                    + "please change the value(current: {}) of config({}) more than 0 to start it.", flushInterval,
                SentinelConfig.METRIC_FLUSH_INTERVAL);
            return;
        }
        SCHEDULER.scheduleAtFixedRate(new MetricTimerListener(), 0, flushInterval, TimeUnit.SECONDS);
    }

    /**
     * 监听 {@link FlowRule} 的动态配置源 {@link SentinelProperty}。
     * 也可通过 {@link #loadRules(List)} 直接加载规则。
     *
     * @param property 待监听的配置属性
     */
    public static void register2Property(SentinelProperty<List<FlowRule>> property) {
        AssertUtil.notNull(property, "property cannot be null");
        synchronized (LISTENER) {
            RecordLog.info("[FlowRuleManager] Registering new property to flow rule manager");
            currentProperty.removeListener(LISTENER);
            property.addListener(LISTENER);
            currentProperty = property;
        }
    }

    /**
     * 获取全部流控规则的副本。
     *
     * @return 规则列表副本
     */
    public static List<FlowRule> getRules() {
        return flowRules.getRules();
    }

    /**
     * 加载 {@link FlowRule} 列表，替换原有规则。
     *
     * @param rules 新规则列表
     */
    public static void loadRules(List<FlowRule> rules) {
        currentProperty.updateValue(rules);
    }

    /** 获取指定资源名下的流控规则（包内可见）。 */
    static List<FlowRule> getFlowRules(String resource) {
        return flowRules.getRules(resource);
    }

    /** 判断资源是否已配置流控规则。 */
    public static boolean hasConfig(String resource) {
        return flowRules.hasConfig(resource);
    }

    /** 判断来源是否为该资源的“其他”来源（未单独配置的 caller）。 */
    public static boolean isOtherOrigin(String origin, String resourceName) {
        if (StringUtil.isEmpty(origin)) {
            return false;
        }

        List<FlowRule> rules = flowRules.getRules(resourceName);

        if (rules != null) {
            for (FlowRule rule : rules) {
                if (origin.equals(rule.getLimitApp())) {
                    return false;
                }
            }
        }

        return true;
    }

    private static final class FlowPropertyListener implements PropertyListener<List<FlowRule>> {

        @Override
        public synchronized void configUpdate(List<FlowRule> value) {
            Map<String, List<FlowRule>> rules = FlowRuleUtil.buildFlowRuleMap(value);
            flowRules.updateRules(rules);
            RecordLog.info("[FlowRuleManager] Flow rules received: {}", rules);
        }

        @Override
        public synchronized void configLoad(List<FlowRule> conf) {
            Map<String, List<FlowRule>> rules = FlowRuleUtil.buildFlowRuleMap(conf);
            flowRules.updateRules(rules);
            RecordLog.info("[FlowRuleManager] Flow rules loaded: {}", rules);
        }
    }

}
