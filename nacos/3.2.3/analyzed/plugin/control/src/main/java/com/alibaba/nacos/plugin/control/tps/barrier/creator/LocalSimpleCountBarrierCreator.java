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

package com.alibaba.nacos.plugin.control.tps.barrier.creator;

import com.alibaba.nacos.plugin.control.tps.barrier.RuleBarrier;
import com.alibaba.nacos.plugin.control.tps.barrier.LocalSimpleCountRuleBarrier;

import java.util.concurrent.TimeUnit;

/**
 * 本地简单计数规则屏障创建器。
 *
 * <p>单例实现，为限流点或子规则创建基于本地内存计数的 {@link LocalSimpleCountRuleBarrier}。</p>
 *
 * @author shiyiyue
 */
public class LocalSimpleCountBarrierCreator implements RuleBarrierCreator {
    
    /** 单例实例。 */
    private static final LocalSimpleCountBarrierCreator INSTANCE =
        new LocalSimpleCountBarrierCreator();
    
    public LocalSimpleCountBarrierCreator() {
    }
    
    /**
     * 获取创建器单例。
     *
     * @return 本地简单计数屏障创建器实例
     */
    public static final LocalSimpleCountBarrierCreator getInstance() {
        return INSTANCE;
    }
    
    /**
     * 创建指定限流点与规则名的本地计数屏障。
     *
     * @param pointName 限流点名称
     * @param ruleName  规则名称
     * @param period    计数统计周期
     * @return 本地简单计数规则屏障
     */
    @Override
    public RuleBarrier createRuleBarrier(String pointName, String ruleName, TimeUnit period) {
        return new LocalSimpleCountRuleBarrier(pointName, ruleName, period);
    }
    
    /**
     * 返回创建器标识名 {@code localsimplecountor}。
     *
     * @return 创建器名称
     */
    @Override
    public String name() {
        return "localsimplecountor";
    }
}
