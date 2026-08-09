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
 * Arguments object for {@code RRateLimiter.updateRate(...)} methods.
 */
public interface RateLimiterArgs {

    /**
     * Creates arguments with required parameters.
     *
     * @param mode rate mode
     * @param rate rate
     * @param rateInterval rate time interval
     * @return arguments instance
     */
    static RateLimiterArgs of(RateType mode, long rate, Duration rateInterval) {
        return new RateLimiterParams(mode, rate, rateInterval);
    }

    /**
     * Defines time to live of rate limiter keys.
     *
     * @param keepAliveTime maximum time that limiter will wait for a new acquisition before deletion
     * @return arguments instance
     */
    RateLimiterArgs keepAliveTime(Duration keepAliveTime);

    /**
     * Defines whether to keep current state (available permits and used permits history) or reset it.
     *
     * @param keepState {@code true} to keep state, {@code false} to reset state
     * @return arguments instance
     */
    RateLimiterArgs keepState(boolean keepState);

}

