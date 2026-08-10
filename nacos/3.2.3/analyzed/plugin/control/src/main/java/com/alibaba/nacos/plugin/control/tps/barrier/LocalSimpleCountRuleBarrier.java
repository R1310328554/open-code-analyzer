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

import java.util.concurrent.TimeUnit;

/**
 * 本地简单计数规则屏障，使用 {@link LocalSimpleCountRateCounter} 作为速率计数器。
 *
 * @author shiyiyue
 */
public class LocalSimpleCountRuleBarrier extends SimpleCountRuleBarrier {
    
    public LocalSimpleCountRuleBarrier(String pointName, String ruleName, TimeUnit period) {
        super(pointName, ruleName, period);
    }
    
    /**
     * 创建本地简单计数速率计数器实例。
     *
     * @param name   计数器名称
     * @param period 统计周期
     * @return 本地计数器实例
     */
    public RateCounter createSimpleCounter(String name, TimeUnit period) {
        return new LocalSimpleCountRateCounter(name, period);
    }
    
    /** 返回屏障算法标识 {@code localsimplecount}。 */
    @Override
    public String getBarrierName() {
        return "localsimplecount";
    }
}
