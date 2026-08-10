/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.control.impl;

import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.plugin.control.Loggers;
import com.alibaba.nacos.plugin.control.tps.TpsControlManager;
import com.alibaba.nacos.plugin.control.tps.TpsMetrics;
import com.alibaba.nacos.plugin.control.tps.barrier.TpsBarrier;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;
import com.alibaba.nacos.plugin.control.tps.response.TpsResultCode;
import com.alibaba.nacos.plugin.control.tps.rule.TpsControlRule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Nacos 默认 TPS 流控管理器。
 *
 * <p>维护 TPS 点位与 {@link TpsBarrier} 映射，支持规则注册/更新； 内置定时任务每 900ms 上报通过/拒绝计数。</p>
 *
 * @author shiyiyue
 */
public class NacosTpsControlManager extends TpsControlManager {
    
    /** TPS 点位名称到限流屏障的映射。 */
    protected final Map<String, TpsBarrier> points = new ConcurrentHashMap<>(16);
    
    /** TPS 点位名称到流控规则的映射。 */
    protected final Map<String, TpsControlRule> rules = new ConcurrentHashMap<>(16);
    
    /** 定时上报 TPS 指标的调度线程池。 */
    protected ScheduledExecutorService executorService;
    
    /** 初始化 TPS 管理器并启动指标上报任务。 */
    public NacosTpsControlManager() {
        super();
        executorService = ExecutorFactory.newSingleScheduledExecutorService(r -> {
            Thread thread = new Thread(r, "nacos.plugin.tps.control.reporter");
            thread.setDaemon(true);
            return thread;
        });
        startTpsReport();
    }
    
    /** 启动固定延迟的 TPS 指标上报调度。 */
    protected void startTpsReport() {
        executorService
            .scheduleWithFixedDelay(new NacosTpsControlManager.TpsMetricsReporter(), 0, 900,
                TimeUnit.MILLISECONDS);
    }
    
    /**
     * 注册 TPS 限流点位并初始化或应用已有规则。
     *
     * @param pointName pointName.
     */
    public synchronized void registerTpsPoint(String pointName) {
        if (!points.containsKey(pointName)) {
            points.put(pointName, tpsBarrierCreator.createTpsBarrier(pointName));
            if (rules.containsKey(pointName)) {
                points.get(pointName).applyRule(rules.get(pointName));
            } else {
                initTpsRule(pointName);
            }
        }
    }
    
    /**
     * 更新指定点位的 TPS 流控规则。
     *
     * @param pointName pointName.
     * @param rule      rule.
     */
    public synchronized void applyTpsRule(String pointName, TpsControlRule rule) {
        if (rule == null || rule.getPointRule() == null) {
            rules.remove(pointName);
        } else {
            rules.put(pointName, rule);
        }
        if (points.containsKey(pointName)) {
            points.get(pointName).applyRule(rule);
        }
    }
    
    /** 返回全部 TPS 点位屏障映射。 */
    public Map<String, TpsBarrier> getPoints() {
        return points;
    }
    
    /** 返回全部 TPS 流控规则映射。 */
    public Map<String, TpsControlRule> getRules() {
        return rules;
    }
    
    /**
     * 对请求执行 TPS 校验，未注册点位则跳过。
     *
     * @param tpsRequest TpsRequest.
     * @return check current tps is allowed.
     */
    public TpsCheckResponse check(TpsCheckRequest tpsRequest) {
        
        if (points.containsKey(tpsRequest.getPointName())) {
            try {
                return points.get(tpsRequest.getPointName()).applyTps(tpsRequest);
            } catch (Throwable throwable) {
                Loggers.TPS.warn("[{}]apply tps error,error={}", tpsRequest.getPointName(),
                    throwable);
            }
        }
        return new TpsCheckResponse(true, TpsResultCode.CHECK_SKIP, "skip");
        
    }
    
    /** 定时采集各点位 TPS 通过/拒绝计数并写入日志。 */
    class TpsMetricsReporter implements Runnable {
        
        long lastReportSecond = 0L;
        
        /**
         * 将毫秒时间戳格式化为 {@code yyyy-MM-dd HH:mm:ss} 字符串。
         *
         * @param timeStamp timestamp milliseconds.
         * @return
         */
        public String getTimeFormatOfSecond(long timeStamp) {
            String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timeStamp));
            return format;
        }
        
        @Override
        public void run() {
            try {
                long now = System.currentTimeMillis();
                StringBuilder stringBuilder = new StringBuilder();
                Set<Map.Entry<String, TpsBarrier>> entries = points.entrySet();
                
                long tempSecond = 0L;
                
                long metricsTime = now - 1000L;
                String formatString = getTimeFormatOfSecond(metricsTime);
                for (Map.Entry<String, TpsBarrier> entry : entries) {
                    TpsBarrier tpsBarrier = entry.getValue();
                    String pointName = entry.getKey();
                    TpsMetrics metrics = tpsBarrier.getPointBarrier().getMetrics(metricsTime);
                    if (metrics != null) {
                        // 该秒指标已上报则跳过
                        if (lastReportSecond != 0L && lastReportSecond == metrics.getTimeStamp()) {
                            continue;
                        }
                        tempSecond = metrics.getTimeStamp();
                        
                        stringBuilder.append(pointName).append("|").append("point").append("|")
                            .append(metrics.getPeriod()).append("|").append(formatString)
                            .append("|")
                            .append(metrics.getCounter().getPassCount()).append("|")
                            .append(metrics.getCounter().getDeniedCount()).append("|").append("\n");
                    }
                }
                
                if (tempSecond > 0) {
                    lastReportSecond = tempSecond;
                }
                
                if (stringBuilder.length() > 0) {
                    Loggers.TPS.info("Tps reporting...\n" + stringBuilder.toString());
                }
            } catch (Throwable throwable) {
                Loggers.TPS.error("Tps reporting error", throwable);
            }
            
        }
    }
    
    /** 返回插件标识 {@code nacos}。 */
    @Override
    public String getName() {
        return "nacos";
    }
}
