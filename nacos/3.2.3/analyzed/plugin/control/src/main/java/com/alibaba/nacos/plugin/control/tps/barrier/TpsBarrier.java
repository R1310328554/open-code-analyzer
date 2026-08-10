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

import com.alibaba.nacos.plugin.control.tps.barrier.creator.RuleBarrierCreator;
import com.alibaba.nacos.plugin.control.tps.barrier.creator.LocalSimpleCountBarrierCreator;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;
import com.alibaba.nacos.plugin.control.tps.rule.TpsControlRule;

import java.util.concurrent.TimeUnit;

/**
 * TPS 屏障抽象基类，为每个限流点持有底层 {@link RuleBarrier} 并转发检查与规则应用。
 *
 * <p>默认使用 {@link LocalSimpleCountBarrierCreator} 创建秒级本地简单计数屏障。</p>
 *
 * @author shiyiyue
 */
public abstract class TpsBarrier {
    
    /** 规则屏障创建器。 */
    protected RuleBarrierCreator ruleBarrierCreator;
    
    /** 限流点名称。 */
    protected String pointName;
    
    /** 点级规则屏障实例。 */
    protected RuleBarrier pointBarrier;
    
    public TpsBarrier(String pointName) {
        this.pointName = pointName;
        this.ruleBarrierCreator = new LocalSimpleCountBarrierCreator();
        this.pointBarrier =
            ruleBarrierCreator.createRuleBarrier(pointName, pointName, TimeUnit.SECONDS);
    }
    
    /**
     * 执行 TPS 限流检查。
     *
     * @param tpsCheckRequest TPS 检查请求
     * @return 是否允许通过及结果码
     */
    public abstract TpsCheckResponse applyTps(TpsCheckRequest tpsCheckRequest);
    
    /**
     * 获取点级规则屏障。
     *
     * @return 规则屏障实例
     */
    public RuleBarrier getPointBarrier() {
        return pointBarrier;
    }
    
    /**
     * 获取限流点名称。
     *
     * @return 限流点名称
     */
    public String getPointName() {
        return pointName;
    }
    
    /**
     * 应用或清除 TPS 管控规则。
     *
     * @param newControlRule 新规则
     */
    public abstract void applyRule(TpsControlRule newControlRule);
}
