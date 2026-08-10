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

package com.alibaba.nacos.plugin.control.tps.barrier;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.plugin.control.tps.MonitorType;
import com.alibaba.nacos.plugin.control.tps.TpsMetrics;
import com.alibaba.nacos.plugin.control.tps.request.BarrierCheckRequest;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;
import com.alibaba.nacos.plugin.control.tps.rule.RuleDetail;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 规则屏障抽象基类，封装限流规则元数据与 TPS 检查/指标采集接口。
 *
 * <p>持有统计周期、最大计数、监控模式等规则属性，
 * 子类实现具体的计数算法与 {@link #applyTps} 逻辑。</p>
 *
 * @author shiyiyue
 */
public abstract class RuleBarrier {
    
    /** 统计周期。 */
    private TimeUnit period;
    
    /** 限流点名称。 */
    private String pointName;
    
    /** 周期内最大允许请求数，{@code -1} 表示无限制。 */
    private long maxCount;
    
    /** 规则名称。 */
    private String ruleName;
    
    /** 监控模式：{@link MonitorType#MONITOR} 或 {@link MonitorType#INTERCEPT}。 */
    private String monitorType = MonitorType.MONITOR.getType();
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public String getPointName() {
        return pointName;
    }
    
    public void setPointName(String pointName) {
        this.pointName = pointName;
    }
    
    public TimeUnit getPeriod() {
        return period;
    }
    
    public void setPeriod(TimeUnit period) {
        this.period = period;
    }
    
    /**
     * 获取屏障算法名称标识。
     *
     * @return 屏障名称
     */
    public abstract String getBarrierName();
    
    public long getMaxCount() {
        return maxCount;
    }
    
    public void setMaxCount(long maxCount) {
        this.maxCount = maxCount;
    }
    
    public String getMonitorType() {
        return monitorType;
    }
    
    public void setMonitorType(String monitorType) {
        this.monitorType = monitorType;
    }
    
    /** 判断当前是否为监控模式（超限不拒绝）。 */
    public boolean isMonitorType() {
        return MonitorType.MONITOR.getType().equalsIgnoreCase(this.monitorType);
    }
    
    /** 生成 JSON 格式的限流拒绝详情消息。 */
    public String getLimitMsg() {
        Map<String, String> limitMsg = new HashMap<>(3);
        limitMsg.put("deniedType", "point");
        limitMsg.put("period", period.toString());
        limitMsg.put("limitCount", String.valueOf(maxCount));
        return JacksonUtils.toJson(limitMsg);
    }
    
    /**
     * 执行 TPS 限流检查。
     *
     * @param barrierCheckRequest 屏障检查请求
     * @return 检查结果
     */
    public abstract TpsCheckResponse applyTps(BarrierCheckRequest barrierCheckRequest);
    
    /**
     * 应用规则详情（最大 TPS、周期、监控模式等）。
     *
     * @param ruleDetail 规则详情
     */
    public abstract void applyRuleDetail(RuleDetail ruleDetail);
    
    /**
     * 获取指定时间戳的 TPS 指标快照。
     *
     * @param timeStamp 时间戳（毫秒）
     * @return 指标快照，无数据时可能为 {@code null}
     */
    public abstract TpsMetrics getMetrics(long timeStamp);
    
    /** 清除限流规则，恢复为无限制监控模式。 */
    public void clearLimitRule() {
        this.maxCount = -1;
        this.monitorType = MonitorType.MONITOR.getType();
    }
}
