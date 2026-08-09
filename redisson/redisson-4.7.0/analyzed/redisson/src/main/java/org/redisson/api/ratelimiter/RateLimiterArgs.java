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
 * {@code RRateLimiter.updateRate(...)} 方法的参数对象。
 */
public interface RateLimiterArgs {

    /**
     * 使用必填参数创建限流配置。
     *
     * @param mode 限流模式
     * @param rate 速率（许可数）
     * @param rateInterval 速率时间窗口
     * @return 参数实例
     */
    static RateLimiterArgs of(RateType mode, long rate, Duration rateInterval) {
        return new RateLimiterParams(mode, rate, rateInterval);
    }

    /**
     * 设置限流器键的存活时间（TTL）。
     *
     * @param keepAliveTime 无新获取请求后限流器被删除前的最长等待时间
     * @return 参数实例
     */
    RateLimiterArgs keepAliveTime(Duration keepAliveTime);

    /**
     * 设置是否保留当前状态（可用许可与已用许可历史），或重置状态。
     *
     * @param keepState {@code true} 保留状态，{@code false} 重置状态
     * @return 参数实例
     */
    RateLimiterArgs keepState(boolean keepState);

}
