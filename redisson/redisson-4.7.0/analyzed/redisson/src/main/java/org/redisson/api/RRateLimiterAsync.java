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

import org.redisson.api.ratelimiter.RateLimiterArgs;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的分布式限流器异步 API。
 * <p>各方法返回 {@link RFuture}；底层同样通过 GCRA 算法限流。
 *
 * @author Nikita Koksharov
 */
public interface RRateLimiterAsync extends RExpirableAsync {

    /**
     * 请改用 {@link #trySetRateAsync(RateType, long, Duration)}
     *
     * @param mode 限流模式（{@link RateType}）
     * @param rate 时间窗口内允许的请求数
     * @param rateInterval 速率统计时间窗口
     * @param rateIntervalUnit 时间窗口单位
     * @return 设置成功则为 {@code true}，否则 {@code false}
     */
    @Deprecated
    RFuture<Boolean> trySetRateAsync(RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit);

    /**
     * 仅在限流器尚未配置时设置速率（首次设置）。
     *
     * @param mode 限流模式（{@link RateType}）
     * @param rate 时间窗口内允许的请求数
     * @param rateInterval 速率统计时间窗口
     * @return 设置成功则为 {@code true}，否则 {@code false}
     */
    RFuture<Boolean> trySetRateAsync(RateType mode, long rate, Duration rateInterval);

    /**
     * 仅在限流器尚未配置时设置速率（首次设置）。
     * Time to live is applied only if rate limit has been set successfully.
     *
     * @param mode 限流模式（{@link RateType}）
     * @param rate 时间窗口内允许的请求数
     * @param rateInterval 速率统计时间窗口
     * @param keepAliveTime 无新获取时限流器键的最大存活时间
     * @return 设置成功则为 {@code true}，否则 {@code false}
     */
    RFuture<Boolean> trySetRateAsync(RateType mode, long rate, Duration rateInterval, Duration keepAliveTime);

    /**
     * 若调用时恰好有可用令牌则立即获取一个。
     *
     * <p>若有可用令牌则立即获取并返回 {@code true}，可用令牌数减一。
     *
     * <p>若无可用令牌则立即返回 {@code false}。
     *
     * @return 获取成功则为 {@code true}，否则 {@code false}
     */
    RFuture<Boolean> tryAcquireAsync();
    
    /**
     * 若调用时可用令牌足够则立即获取指定数量。
     *
     * <p>若令牌足够则立即获取并返回 {@code true}，可用令牌数相应减少。
     *
     * <p>若令牌不足则立即返回 {@code false}。
     *
     * @param permits 待获取令牌数
     * @return 获取成功则为 {@code true}，否则 {@code false}
     */
    RFuture<Boolean> tryAcquireAsync(long permits);
    
    /**
     * 阻塞获取一个令牌，直到有可用令牌为止。
     *
     * <p>Acquires a permit, if one is available and returns immediately,
     * reducing the number of available permits by one.
     * 
     * @return 无返回值
     */
    RFuture<Void> acquireAsync();
    
    /**
     * 阻塞获取指定数量的令牌，直到全部可用为止。
     *
     * <p>若令牌足够则立即获取指定数量，可用令牌数相应减少。
     * 
     * @param permits 待获取令牌数
     * @return 无返回值
     */
    RFuture<Void> acquireAsync(long permits);
    
    /**
     * 请改用 {@link #tryAcquireAsync(Duration)}
     *
     * @param timeout 最大等待时间
     * @param unit 超时时间单位
     * @return 获取成功则为 {@code true}，超时则为 {@code false}
     */
    @Deprecated
    RFuture<Boolean> tryAcquireAsync(long timeout, TimeUnit unit);

    /**
     * 在指定等待时间内尝试获取一个令牌。
     *
     * <p>若有可用令牌则立即获取并返回 {@code true}，可用令牌数减一。
     *
     * <p>若无可用令牌则当前线程进入等待，直到超时或获取成功。
     *
     * <p>成功获取时返回 {@code true}。
     *
     * <p>超时则返回 {@code false}；等待时间小于等于零时不等待。
     *
     * @param timeout 最大等待时间
     * @return 获取成功则为 {@code true}，超时则为 {@code false}
     */
    RFuture<Boolean> tryAcquireAsync(Duration timeout);

    /**
     * 请改用 {@link #tryAcquireAsync(long, Duration)}
     *
     * @param permits 令牌数量
     * @param timeout 最大等待时间
     * @param unit 超时时间单位
     * @return 获取成功则为 {@code true}，超时则为 {@code false}
     */
    @Deprecated
    RFuture<Boolean> tryAcquireAsync(long permits, long timeout, TimeUnit unit);

    /**
     * 在指定等待时间内尝试获取指定数量的令牌。
     *
     * <p>若令牌足够则立即获取并返回 {@code true}，可用令牌数相应减少。
     *
     * <p>若无足够令牌则当前线程进入等待，直到超时或获取成功。
     *
     * <p>成功获取时返回 {@code true}。
     *
     * <p>超时则返回 {@code false}；等待时间小于等于零时不等待。
     *
     * @param permits 令牌数量
     * @param timeout 最大等待时间
     * @return 获取成功则为 {@code true}，超时则为 {@code false}
     */
    RFuture<Boolean> tryAcquireAsync(long permits, Duration timeout);

    /**
     * 释放指定数量的令牌，增加可用配额。
     *
     * <p>按指定数量增加可用令牌，唤醒可继续获取的等待者。
     *
     * <p>释放操作完成后 Future/Mono 结束。
     *
     * @param permits 令牌数量 to release; must be greater than or equal to zero
     */
    RFuture<Void> releaseAsync(long permits);

    /**
     * 请改用 {@link #setRateAsync(RateLimiterArgs)}
     *
     * @param mode 限流模式（{@link RateType}）
     * @param rate 时间窗口内允许的请求数
     * @param rateInterval 速率统计时间窗口
     * @param rateIntervalUnit 时间窗口单位
     * @return 设置成功则为 {@code true}，否则 {@code false}
     */
    @Deprecated
    RFuture<Void> setRateAsync(RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit);

    /**
     * 更新速率配置，可选择重置或保留当前令牌状态。
     * <p>
     * Use {@link RateLimiterArgs#of(RateType, long, Duration)} to construct arguments.
     *
     * @param args 限流参数对象
     * @return 限流器未配置或已过期则为 {@code false}，否则 {@code true}
     */
    RFuture<Boolean> updateRateAsync(RateLimiterArgs args);

    /**
     * 请改用 {@link #setRateAsync(RateLimiterArgs)}
     * 
     * 设置速率并清空令牌状态；若此前未配置则覆盖限流与状态。
     *
     * @param mode 限流模式（{@link RateType}）
     * @param rate 时间窗口内允许的请求数
     * @param rateInterval 速率统计时间窗口
     */
    @Deprecated
    RFuture<Void> setRateAsync(RateType mode, long rate, Duration rateInterval);

    /**
     * 设置速率，可选择重置或保留当前令牌状态。
     * <p>
     * Use {@link RateLimiterArgs#of(RateType, long, Duration)} to construct arguments.
     *
     * @param args 限流参数对象
     */
    RFuture<Void> setRateAsync(RateLimiterArgs args);

    /**
     * 设置 TTL、速率并清空令牌状态；若此前未配置则覆盖限流与状态。
     *
     * @param mode 限流模式（{@link RateType}）
     * @param rate 时间窗口内允许的请求数
     * @param rateInterval 速率统计时间窗口
     * @param keepAliveTime 无新获取时限流器键的最大存活时间
     */
    RFuture<Void> setRateAsync(RateType mode, long rate, Duration rateInterval, Duration keepAliveTime);

    /**
     * 返回当前限流器的配置快照。
     * 
     * @return 限流配置对象
     */
    RFuture<RateLimiterConfig> getConfigAsync();

    /**
     * 返回当前可用令牌数量。
     *
     * @return 可用令牌数
     */
    RFuture<Long> availablePermitsAsync();

}
