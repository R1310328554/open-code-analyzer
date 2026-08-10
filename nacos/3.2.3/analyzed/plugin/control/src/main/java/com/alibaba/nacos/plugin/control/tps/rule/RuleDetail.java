/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.control.tps.rule;

import java.util.concurrent.TimeUnit;

/**
 * TPS 限流规则明细。
 *
 * <p>描述单条规则的名称、周期内最大请求数、统计周期及监控/拦截模式。</p>
 *
 * @author shiyiyue
 */
public class RuleDetail {
    
    /** 规则名称。 */
    String ruleName;
    
    /** 统计周期内允许的最大请求数，{@code -1} 表示不限制。 */
    long maxCount = -1;
    
    /** 计数统计周期，默认秒级。 */
    TimeUnit period = TimeUnit.SECONDS;
    
    /**
     * 监控或拦截模式标识。
     *
     * <p>monitor 模式下超限仅记录不拒绝；intercept 模式下超限直接拒绝。</p>
     */
    String monitorType = "";
    
    public RuleDetail() {
        
    }
    
    /**
     * 获取规则名称。
     *
     * @return 规则名称
     */
    public String getRuleName() {
        return ruleName;
    }
    
    /**
     * 设置规则名称。
     *
     * @param ruleName 规则名称
     */
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    /**
     * 获取计数统计周期。
     *
     * @return 时间单位
     */
    public TimeUnit getPeriod() {
        return period;
    }
    
    /**
     * 设置计数统计周期。
     *
     * @param period 时间单位
     */
    public void setPeriod(TimeUnit period) {
        this.period = period;
    }
    
    /**
     * 获取周期内最大请求数。
     *
     * @return 最大 TPS，{@code -1} 表示不限制
     */
    public long getMaxCount() {
        return maxCount;
    }
    
    /**
     * 设置周期内最大请求数。
     *
     * @param maxCount 最大 TPS
     */
    public void setMaxCount(long maxCount) {
        this.maxCount = maxCount;
    }
    
    /**
     * 获取监控/拦截模式。
     *
     * @return 模式标识
     */
    public String getMonitorType() {
        return monitorType;
    }
    
    /**
     * 设置监控/拦截模式。
     *
     * @param monitorType 模式标识
     */
    public void setMonitorType(String monitorType) {
        this.monitorType = monitorType;
    }
    
    @Override
    public String toString() {
        return "Rule{" + "maxTps=" + maxCount + ", monitorType='" + monitorType + '\'' + '}';
    }
}
