/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api;

/**
 * 限流器当前配置快照；包含 {@link RateType}、时间窗口与速率上限。
 *
 * @author Nikita Koksharov
 *
 */
public class RateLimiterConfig {

    private RateType rateType;
    private Long rateInterval;
    private Long rate;

    public RateLimiterConfig(RateType rateType, Long rateInterval, Long rate) {
        super();
        this.rateType = rateType;
        this.rateInterval = rateInterval;
        this.rate = rate;
    }

    /**
     * 返回通过 {@link RRateLimiter#trySetRate(RateType, long, long, RateIntervalUnit)} 或 {@link RRateLimiter#trySetRateAsync(RateType, long, long, RateIntervalUnit)} 设置的限流模式。
     * 
     * @return 限流模式
     */
    public RateType getRateType() {
        return rateType;
    }
    
    /**
     * 返回通过 {@link RRateLimiter#trySetRate(RateType, long, long, RateIntervalUnit)} 或 {@link RRateLimiter#trySetRateAsync(RateType, long, long, RateIntervalUnit)} 设置的速率时间窗口（毫秒）。
     * 
     * @return 速率时间窗口（毫秒）
     */
    public Long getRateInterval() {
        return rateInterval;
    }

    /**
     * 返回通过 {@link RRateLimiter#trySetRate(RateType, long, long, RateIntervalUnit)} 或 {@link RRateLimiter#trySetRateAsync(RateType, long, long, RateIntervalUnit)} 设置的速率上限。
     * 
     * @return 速率上限
     */
    public Long getRate() {
        return rate;
    }

    
}
