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

import java.util.concurrent.TimeUnit;

/**
 * 规则级限流屏障创建器 SPI 接口。
 *
 * <p>定义按限流点、规则名与统计周期创建 {@link RuleBarrier} 的契约，
 * 供不同计数策略（如本地简单计数）扩展实现。</p>
 *
 * @author shiyiyue
 */
public interface RuleBarrierCreator {
    
    /**
     * 在指定时间周期内创建规则计数屏障。
     *
     * @param pointName 限流点名称
     * @param ruleName  规则名称
     * @param period    计数统计周期
     * @return 规则限流屏障实例
     */
    RuleBarrier createRuleBarrier(String pointName, String ruleName, TimeUnit period);
    
    /**
     * 返回速率计数创建器的唯一标识名。
     *
     * @return 创建器名称
     */
    String name();
}
