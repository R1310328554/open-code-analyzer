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

import io.reactivex.rxjava3.core.Single;

import java.time.Duration;

/**
 * {@link RGcra} RxJava3 风格 API。
 * <p>各方法返回 {@link Single}；需要 <b>Redis 8.8.0 及以上</b>。
 *
 * @author Su Ko
 */
public interface RGcraRx extends RExpirableRx {

    /**
     * 仅尚未配置时设置速率参数。
     *
     * @param maxBurst 最大突发容量
     * @param tokensPerPeriod 每周期补充令牌数
     * @param period 补充周期
     * @return {@code true} if the rate was set, or {@code false} if it was already set before
     */
    Single<Boolean> trySetRate(long maxBurst, long tokensPerPeriod, Duration period);

    /**
     * 覆盖设置速率参数并重置已消耗令牌。
     *
     * @param maxBurst 最大突发容量
     * @param tokensPerPeriod 每周期补充令牌数
     * @param period 补充周期
     * @return void
     */
    Single<Void> setRate(long maxBurst, long tokensPerPeriod, Duration period);

    /**
     * 返回通过 trySetRate/setRate 设置的速率配置。
     * {@link #trySetRate(long, long, Duration)} 或 {@link #setRate(long, long, Duration)}。
     *
     * @return 速率配置；未设置时返回 {@code null}
     */
    Single<GcraConfig> getConfig();

    /**
     * 以单令牌请求执行 GCRA 限流。
     * 使用已通过以下方法设置的速率配置：
     * {@link #trySetRate(long, long, Duration)} 或 {@link #setRate(long, long, Duration)}。
     *
     * @return GCRA 限流结果
     */
    Single<GcraResult> tryAcquire();

    /**
     * 以指定令牌数执行 GCRA 限流。
     * 使用已通过以下方法设置的速率配置：
     * {@link #trySetRate(long, long, Duration)} 或 {@link #setRate(long, long, Duration)}。
     *
     * @param tokens 请求令牌数
     * @return GCRA 限流结果
     */
    Single<GcraResult> tryAcquire(long tokens);

    /**
     * 以单令牌请求执行 GCRA 限流（一次性参数，已废弃）。
     *
     * @param maxBurst 最大突发容量
     * @param tokensPerPeriod 每周期补充令牌数
     * @param period 补充周期
     * @return GCRA 限流结果
     * @deprecated use {@link #trySetRate(long, long, Duration)} with {@link #tryAcquire()} instead
     */
    @Deprecated
    Single<GcraResult> tryAcquire(long maxBurst, long tokensPerPeriod, Duration period);

    /**
     * 以指定令牌数执行 GCRA 限流（一次性参数，已废弃）。
     *
     * @param maxBurst 最大突发容量
     * @param tokensPerPeriod 每周期补充令牌数
     * @param period 补充周期
     * @param tokens 请求令牌数
     * @return GCRA 限流结果
     * @deprecated use {@link #trySetRate(long, long, Duration)} with {@link #tryAcquire(long)} instead
     */
    @Deprecated
    Single<GcraResult> tryAcquire(long maxBurst, long tokensPerPeriod, Duration period, long tokens);

}
