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

import com.alibaba.nacos.plugin.control.Loggers;
import com.alibaba.nacos.plugin.control.tps.request.BarrierCheckRequest;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;
import com.alibaba.nacos.plugin.control.tps.rule.RuleDetail;
import com.alibaba.nacos.plugin.control.tps.rule.TpsControlRule;

/**
 * 默认 Nacos TPS 屏障，将限流检查委托给底层 {@link RuleBarrier}。
 *
 * <p>负责将 {@link TpsCheckRequest} 转换为 {@link BarrierCheckRequest}，
 * 并将 {@link TpsControlRule} 中的点级规则应用到屏障。</p>
 *
 * @author shiyiyue
 */
public class DefaultNacosTpsBarrier extends TpsBarrier {
    
    public DefaultNacosTpsBarrier(String pointName) {
        super(pointName);
    }
    
    /**
     * 执行 TPS 限流检查，转发至点级规则屏障。
     *
     * @param tpsCheckRequest TPS 检查请求
     * @return 是否允许通过及结果码
     */
    public TpsCheckResponse applyTps(TpsCheckRequest tpsCheckRequest) {
        
        BarrierCheckRequest pointCheckRequest = new BarrierCheckRequest();
        pointCheckRequest.setCount(tpsCheckRequest.getCount());
        pointCheckRequest.setPointName(super.getPointName());
        pointCheckRequest.setTimestamp(tpsCheckRequest.getTimestamp());
        return super.getPointBarrier().applyTps(pointCheckRequest);
    }
    
    /**
     * 应用或清除 TPS 管控规则到点级屏障。
     *
     * @param newControlRule 新规则，{@code null} 或空规则表示清除限流
     */
    public synchronized void applyRule(TpsControlRule newControlRule) {
        Loggers.CONTROL.info("Apply tps control rule start,pointName=[{}]  ", this.getPointName());
        
        // 1. 规则为空时清除所有限流配置
        if (newControlRule == null || newControlRule.getPointRule() == null) {
            Loggers.CONTROL.info("Clear all tps control rule ,pointName=[{}]  ",
                this.getPointName());
            super.getPointBarrier().clearLimitRule();
            return;
        }
        
        // 2. 更新点级规则详情（最大 TPS、监控模式等）
        RuleDetail newPointRule = newControlRule.getPointRule();
        
        Loggers.CONTROL.info(
            "Update  point  control rule ,pointName=[{}],original maxTps={}, new maxTps={}"
                + ",original monitorType={}, original monitorType={}, ",
            this.getPointName(),
            this.pointBarrier.getMaxCount(), newPointRule.getMaxCount(),
            this.pointBarrier.getMonitorType(),
            newPointRule.getMonitorType());
        this.pointBarrier.applyRuleDetail(newPointRule);
        
        Loggers.CONTROL.info("Apply tps control rule end,pointName=[{}]  ", this.getPointName());
        
    }
}
