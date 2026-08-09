/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

/**
 * 熔断策略枚举，定义触发熔断的指标类型。
 *
 * @author Eric Zhao
 * @since 1.8.0
 */
public enum CircuitBreakerStrategy {

    /** 慢调用比例超过阈值时打开熔断（切断请求）。 */
    SLOW_REQUEST_RATIO(0),
    /** 异常比例超过阈值时打开熔断（切断请求）。 */
    ERROR_RATIO(1),
    /** 异常数超过阈值时打开熔断（切断请求）。 */
    ERROR_COUNT(2);

    private int type;

    CircuitBreakerStrategy(int type) {
        this.type = type;
    }

    /** 返回策略对应的数值类型标识。 */
    public int getType() {
        return type;
    }
}
