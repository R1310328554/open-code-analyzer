/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;

/**
 * 熔断器状态变更观察者接口。
 *
 * @author Eric Zhao
 * @since 1.8.0
 */
public interface CircuitBreakerStateChangeObserver {

    /**
     * <p>熔断器状态变更时触发的回调。可能的转换包括：</p>
     * <ul>
     * <li>{@code CLOSED} → {@code OPEN}（携带触发指标）</li>
     * <li>{@code OPEN} → {@code HALF_OPEN}</li>
     * <li>{@code OPEN} → {@code CLOSED}</li>
     * <li>{@code HALF_OPEN} → {@code OPEN}（携带触发指标）</li>
     * </ul>
     *
     * @param prevState     熔断器先前状态
     * @param newState      熔断器新状态
     * @param rule          关联规则
     * @param snapshotValue 熔断打开时的触发值（新状态为 CLOSED 或 HALF_OPEN 时为 null）
     */
    void onStateChange(CircuitBreaker.State prevState, CircuitBreaker.State newState, DegradeRule rule,
                       Double snapshotValue);
}
