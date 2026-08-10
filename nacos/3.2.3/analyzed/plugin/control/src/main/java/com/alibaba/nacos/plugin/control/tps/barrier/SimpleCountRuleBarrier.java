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

import com.alibaba.nacos.plugin.control.tps.MonitorType;
import com.alibaba.nacos.plugin.control.tps.TpsMetrics;
import com.alibaba.nacos.plugin.control.tps.request.BarrierCheckRequest;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;
import com.alibaba.nacos.plugin.control.tps.response.TpsResultCode;
import com.alibaba.nacos.plugin.control.tps.rule.RuleDetail;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 简单计数规则屏障，基于 {@link RateCounter} 实现固定窗口计数限流。
 *
 * <p>拦截模式下超限拒绝请求，监控模式下仅累加计数不拒绝；
 * 周期变更时会重建底层计数器。</p>
 *
 * @author shiyiyue
 */
public abstract class SimpleCountRuleBarrier extends RuleBarrier {
    
    /** 底层速率计数器。 */
    RateCounter rateCounter;
    
    public SimpleCountRuleBarrier(String pointName, String ruleName, TimeUnit period) {
        super.setPointName(pointName);
        super.setPeriod(period);
        super.setRuleName(ruleName);
        this.rateCounter = createSimpleCounter(ruleName, period);
    }
    
    /**
     * 创建速率计数器实例，由子类指定具体实现。
     *
     * @param name   计数器名称
     * @param period 统计周期
     * @return 速率计数器
     */
    public abstract RateCounter createSimpleCounter(String name, TimeUnit period);
    
    /** 周期变更时重建底层计数器。 */
    public void reCreateRaterCounter(String name, TimeUnit period) {
        this.rateCounter = createSimpleCounter(name, period);
    }
    
    @Override
    public TpsCheckResponse applyTps(BarrierCheckRequest barrierCheckRequest) {
        if (MonitorType.INTERCEPT.getType().equals(getMonitorType())) {
            long maxCount = getMaxCount();
            boolean accepted = rateCounter.tryAdd(barrierCheckRequest.getTimestamp(),
                barrierCheckRequest.getCount(), maxCount);
            return accepted ? new TpsCheckResponse(true, TpsResultCode.PASS_BY_POINT, "success")
                : new TpsCheckResponse(false, TpsResultCode.DENY_BY_POINT,
                    "tps over limit :" + maxCount);
        } else {
            rateCounter.add(barrierCheckRequest.getTimestamp(), barrierCheckRequest.getCount());
            return new TpsCheckResponse(true, TpsResultCode.PASS_BY_POINT, "success");
        }
    }
    
    /** 将时间戳对齐到当前统计周期的窗口起始。 */
    long trimTimeStamp(long timeStamp) {
        if (this.getPeriod() == TimeUnit.SECONDS) {
            timeStamp = RateCounter.getTrimMillsOfSecond(timeStamp);
        } else if (this.getPeriod() == TimeUnit.MINUTES) {
            timeStamp = RateCounter.getTrimMillsOfMinute(timeStamp);
        } else if (this.getPeriod() == TimeUnit.HOURS) {
            timeStamp = RateCounter.getTrimMillsOfHour(timeStamp);
        } else {
            // 默认按秒对齐
            timeStamp = RateCounter.getTrimMillsOfSecond(timeStamp);
        }
        return timeStamp;
    }
    
    @Override
    public TpsMetrics getMetrics(long timeStamp) {
        timeStamp = trimTimeStamp(timeStamp);
        
        TpsMetrics tpsMetrics = new TpsMetrics("", "", timeStamp, super.getPeriod());
        long totalPass = rateCounter.getCount(timeStamp);
        if (totalPass <= 0) {
            return null;
        }
        tpsMetrics.setCounter(new TpsMetrics.Counter(totalPass, 0));
        return tpsMetrics;
        
    }
    
    /**
     * 应用规则详情；统计周期变更时重建计数器。
     *
     * @param ruleDetail 规则详情
     */
    public void applyRuleDetail(RuleDetail ruleDetail) {
        
        if (!Objects.equals(this.getPeriod(), ruleDetail.getPeriod())) {
            this.setMaxCount(ruleDetail.getMaxCount());
            this.setMonitorType(ruleDetail.getMonitorType());
            this.setPeriod(ruleDetail.getPeriod());
            reCreateRaterCounter(ruleDetail.getRuleName(), this.getPeriod());
        } else {
            this.setMaxCount(ruleDetail.getMaxCount());
            this.setMonitorType(ruleDetail.getMonitorType());
        }
    }
}
