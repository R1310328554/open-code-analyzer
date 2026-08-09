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
package com.alibaba.csp.sentinel.slots.system;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.property.SimplePropertyListener;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * <p>
 * Sentinel 系统规则使入站流量与系统容量相匹配，综合考虑入站请求的平均 RT、QPS 与线程数。
 * 还提供系统负载度量（仅 Linux 可用）。
 * </p>
 * <p>
 * RT、QPS、线程数较易理解：若入站请求的 RT、QPS 或线程数超过阈值，请求将被拒绝。
 * 负载则采用不同的计算方法。
 * </p>
 * <p>
 * 将系统视为管道，约束之间的转换形成三个不同区域（流量受限、容量受限与危险区），
 * 行为定性不同。在途请求不足以填满管道时，由 RTprop 决定行为；否则由系统容量主导。
 * 约束线相交于 inflight = Capacity × RTprop。超过该点后管道已满，
 * 超出容量的在途请求形成队列，导致 RTT 与在途流量线性相关并推高系统负载。
 * 危险区内系统将停止响应。<br/>
 * 可参考 BBR 算法了解更多。
 * </p>
 * <p>
 * 注意：{@link SystemRule} 仅对入站请求生效，出站流量不受其限制。
 * </p>
 *
 * @author jialiang.linjl
 * @author leyou
 */
public final class SystemRuleManager {

    private static volatile double highestSystemLoad = Double.MAX_VALUE;
    /** CPU 使用率，取值范围 [0, 1]。 */
    private static volatile double highestCpuUsage = Double.MAX_VALUE;
    private static volatile double qps = Double.MAX_VALUE;
    private static volatile long maxRt = Long.MAX_VALUE;
    private static volatile long maxThread = Long.MAX_VALUE;
    /** 标记该阈值是否已由用户设置。 */
    private static volatile boolean highestSystemLoadIsSet = false;
    private static volatile boolean highestCpuUsageIsSet = false;
    private static volatile boolean qpsIsSet = false;
    private static volatile boolean maxRtIsSet = false;
    private static volatile boolean maxThreadIsSet = false;

    private static AtomicBoolean checkSystemStatus = new AtomicBoolean(false);

    private static SystemStatusListener statusListener = null;
    private final static SystemPropertyListener listener = new SystemPropertyListener();
    private static SentinelProperty<List<SystemRule>> currentProperty = new DynamicSentinelProperty<List<SystemRule>>();

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
        new NamedThreadFactory("sentinel-system-status-record-task", true));

    static {
        checkSystemStatus.set(false);
        statusListener = new SystemStatusListener();
        scheduler.scheduleAtFixedRate(statusListener, 0, 1, TimeUnit.SECONDS);
        currentProperty.addListener(listener);
    }

    /**
     * 监听 {@link SystemRule} 的动态配置源 {@link SentinelProperty}。
     * 也可通过 {@link #loadRules(List)} 直接加载规则。
     *
     * @param property 待监听的配置属性
     */
    public static void register2Property(SentinelProperty<List<SystemRule>> property) {
        synchronized (listener) {
            RecordLog.info("[SystemRuleManager] Registering new property to system rule manager");
            currentProperty.removeListener(listener);
            property.addListener(listener);
            currentProperty = property;
        }
    }

    /**
     * 加载 {@link SystemRule} 列表，替换原有规则。
     *
     * @param rules 新规则列表
     */
    public static void loadRules(List<SystemRule> rules) {
        currentProperty.updateValue(rules);
    }

    /**
     * 获取全部系统规则的副本。
     *
     * @return 规则列表副本
     */
    public static List<SystemRule> getRules() {

        List<SystemRule> result = new ArrayList<SystemRule>();
        if (!checkSystemStatus.get()) {
            return result;
        }

        if (highestSystemLoadIsSet) {
            SystemRule loadRule = new SystemRule();
            loadRule.setHighestSystemLoad(highestSystemLoad);
            result.add(loadRule);
        }

        if (highestCpuUsageIsSet) {
            SystemRule rule = new SystemRule();
            rule.setHighestCpuUsage(highestCpuUsage);
            result.add(rule);
        }

        if (maxRtIsSet) {
            SystemRule rtRule = new SystemRule();
            rtRule.setAvgRt(maxRt);
            result.add(rtRule);
        }

        if (maxThreadIsSet) {
            SystemRule threadRule = new SystemRule();
            threadRule.setMaxThread(maxThread);
            result.add(threadRule);
        }

        if (qpsIsSet) {
            SystemRule qpsRule = new SystemRule();
            qpsRule.setQps(qps);
            result.add(qpsRule);
        }

        return result;
    }

    public static double getInboundQpsThreshold() {
        return qps;
    }

    public static long getRtThreshold() {
        return maxRt;
    }

    public static long getMaxThreadThreshold() {
        return maxThread;
    }

    static class SystemPropertyListener extends SimplePropertyListener<List<SystemRule>> {

        @Override
        public synchronized void configUpdate(List<SystemRule> rules) {
            restoreSetting();
            // systemRules = rules;（历史遗留注释）
            if (rules != null && rules.size() >= 1) {
                for (SystemRule rule : rules) {
                    loadSystemConf(rule);
                }
            } else {
                checkSystemStatus.set(false);
            }

            RecordLog.info(String.format("[SystemRuleManager] Current system check status: %s, "
                    + "highestSystemLoad: %e, "
                    + "highestCpuUsage: %e, "
                    + "maxRt: %d, "
                    + "maxThread: %d, "
                    + "maxQps: %e",
                checkSystemStatus.get(),
                highestSystemLoad,
                highestCpuUsage,
                maxRt,
                maxThread,
                qps));
        }

        protected void restoreSetting() {
            checkSystemStatus.set(false);

            // 恢复默认阈值
            highestSystemLoad = Double.MAX_VALUE;
            highestCpuUsage = Double.MAX_VALUE;
            maxRt = Long.MAX_VALUE;
            maxThread = Long.MAX_VALUE;
            qps = Double.MAX_VALUE;

            highestSystemLoadIsSet = false;
            highestCpuUsageIsSet = false;
            maxRtIsSet = false;
            maxThreadIsSet = false;
            qpsIsSet = false;
        }

    }

    public static Boolean getCheckSystemStatus() {
        return checkSystemStatus.get();
    }

    public static double getSystemLoadThreshold() {
        return highestSystemLoad;
    }

    public static double getCpuUsageThreshold() {
        return highestCpuUsage;
    }

    public static void loadSystemConf(SystemRule rule) {
        boolean checkStatus = false;
        // 校验规则是否有效。

        if (rule.getHighestSystemLoad() >= 0) {
            highestSystemLoad = Math.min(highestSystemLoad, rule.getHighestSystemLoad());
            highestSystemLoadIsSet = true;
            checkStatus = true;
        }

        if (rule.getHighestCpuUsage() >= 0) {
            if (rule.getHighestCpuUsage() > 1) {
                RecordLog.warn(String.format("[SystemRuleManager] Ignoring invalid SystemRule: "
                    + "highestCpuUsage %.3f > 1", rule.getHighestCpuUsage()));
            } else {
                highestCpuUsage = Math.min(highestCpuUsage, rule.getHighestCpuUsage());
                highestCpuUsageIsSet = true;
                checkStatus = true;
            }
        }

        if (rule.getAvgRt() >= 0) {
            maxRt = Math.min(maxRt, rule.getAvgRt());
            maxRtIsSet = true;
            checkStatus = true;
        }
        if (rule.getMaxThread() >= 0) {
            maxThread = Math.min(maxThread, rule.getMaxThread());
            maxThreadIsSet = true;
            checkStatus = true;
        }

        if (rule.getQps() >= 0) {
            qps = Math.min(qps, rule.getQps());
            qpsIsSet = true;
            checkStatus = true;
        }

        checkSystemStatus.set(checkStatus);

    }

    /**
     * 对资源应用 {@link SystemRule}，仅检查入站流量。
     *
     * @param resourceWrapper 资源包装
     * @throws BlockException 任一系统规则阈值被超过时抛出
     */
    public static void checkSystem(ResourceWrapper resourceWrapper, int count) throws BlockException {
        if (resourceWrapper == null) {
            return;
        }
        // 确保系统检查开关已开启。
        if (!checkSystemStatus.get()) {
            return;
        }

        // 仅对入站流量生效
        if (resourceWrapper.getEntryType() != EntryType.IN) {
            return;
        }

        // 全局 QPS 检查
        double currentQps = Constants.ENTRY_NODE.passQps();
        if (currentQps + count > qps) {
            throw new SystemBlockException(resourceWrapper.getName(), "qps");
        }

        // 全局线程数检查
        int currentThread = Constants.ENTRY_NODE.curThreadNum();
        if (currentThread > maxThread) {
            throw new SystemBlockException(resourceWrapper.getName(), "thread");
        }

        double rt = Constants.ENTRY_NODE.avgRt();
        if (rt > maxRt) {
            throw new SystemBlockException(resourceWrapper.getName(), "rt");
        }

        // 负载检查（BBR 算法）
        if (highestSystemLoadIsSet && getCurrentSystemAvgLoad() > highestSystemLoad) {
            if (!checkBbr(currentThread)) {
                throw new SystemBlockException(resourceWrapper.getName(), "load");
            }
        }

        // CPU 使用率检查
        if (highestCpuUsageIsSet && getCurrentCpuUsage() > highestCpuUsage) {
            throw new SystemBlockException(resourceWrapper.getName(), "cpu");
        }
    }

    private static boolean checkBbr(int currentThread) {
        if (currentThread > 1 &&
            currentThread > Constants.ENTRY_NODE.maxSuccessQps() * Constants.ENTRY_NODE.minRt() / 1000) {
            return false;
        }
        return true;
    }

    public static double getCurrentSystemAvgLoad() {
        return statusListener.getSystemAverageLoad();
    }

    public static double getCurrentCpuUsage() {
        return statusListener.getCpuUsage();
    }
}
