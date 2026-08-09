/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

/**
 * <p>基础<a href="https://martinfowler.com/bliki/CircuitBreaker.html">熔断器</a>接口。</p>
 *
 * @author Eric Zhao
 */
public interface CircuitBreaker {

    /**
     * 获取关联的熔断规则。
     *
     * @return 关联的熔断规则
     */
    DegradeRule getRule();

    /**
     * 仅在调用时刻可用时获取一次调用的许可。
     *
     * @param context 当前调用上下文
     * @return 获取成功返回 {@code true}，否则返回 {@code false}
     */
    boolean tryPass(Context context);

    /**
     * 获取熔断器当前状态。
     *
     * @return 熔断器当前状态
     */
    State currentState();

    /**
     * <p>记录一次已完成请求并处理熔断器状态转换。</p>
     * <p>在<strong>已通过</strong>的调用结束时调用。</p>
     *
     * @param context 当前调用上下文
     */
    void onRequestComplete(Context context);

    /**
     * 熔断器状态枚举。
     */
    enum State {
        /** {@code OPEN} 状态下，所有请求将被拒绝，直至到达下次恢复时间点。 */
        OPEN,
        /**
         * {@code HALF_OPEN} 状态下允许一次“探测”调用。
         * 若调用按策略判定为异常（如慢调用），则重新转为 {@code OPEN} 并等待下次恢复；
         * 否则视为资源已恢复，停止熔断并转为 {@code CLOSED}。
         */
        HALF_OPEN,
        /** {@code CLOSED} 状态下允许所有请求；当指标超过阈值时转为 {@code OPEN}。 */
        CLOSED
    }
}
