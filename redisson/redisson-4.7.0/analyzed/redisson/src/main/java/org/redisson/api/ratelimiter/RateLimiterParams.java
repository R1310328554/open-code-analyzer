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
package org.redisson.api.ratelimiter;

import org.redisson.api.RateType;

import java.time.Duration;

/**
 * {@link RateLimiterArgs} 的参数实现，持有速率、时间窗口及状态保留配置。
 */
public final class RateLimiterParams implements RateLimiterArgs {

    final RateType mode;
    final long rate;
    final Duration rateInterval;
    Duration keepAliveTime = Duration.ZERO;
    boolean keepState;

    RateLimiterParams(RateType mode, long rate, Duration rateInterval) {
        this.mode = mode;
        this.rate = rate;
        this.rateInterval = rateInterval;
    }

    @Override
    public RateLimiterArgs keepAliveTime(Duration keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    @Override
    public RateLimiterArgs keepState(boolean keepState) {
        this.keepState = keepState;
        return this;
    }

    /** 返回限流模式。 */
    public RateType getMode() {
        return mode;
    }

    /** 返回速率（许可数）。 */
    public long getRate() {
        return rate;
    }

    /** 返回速率时间窗口。 */
    public Duration getRateInterval() {
        return rateInterval;
    }

    /** 返回限流器键的存活时间。 */
    public Duration getKeepAliveTime() {
        return keepAliveTime;
    }

    /** 返回是否保留当前限流状态。 */
    public boolean isKeepState() {
        return keepState;
    }
}
