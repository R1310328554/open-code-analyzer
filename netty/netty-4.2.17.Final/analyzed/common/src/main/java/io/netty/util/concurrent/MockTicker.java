/*
 * Copyright 2025 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * A fake {@link Ticker} that allows the caller control the flow of time.
 * This can be useful when you test time-sensitive logic without waiting for too long
 * or introducing flakiness due to non-deterministic nature of system clock.
 *
 * <p>可人工推进时间的假 {@link Ticker}，用于单元测试中模拟超时、调度等时间相关逻辑，
 * 避免依赖真实系统时钟导致的不稳定或长时间等待。</p>
 */
public interface MockTicker extends Ticker {

    @Override
    default long initialNanoTime() {
        // 测试时钟通常从 0 开始
        return 0;
    }

    /**
     * Advances the current {@link #nanoTime()} by the given amount of time.
     *
     * @param amount the amount of time to advance this ticker by.
     * @param unit the {@link TimeUnit} of {@code amount}.
     *
     * <p>将当前 {@link #nanoTime()} 向前推进指定时长。</p>
     */
    void advance(long amount, TimeUnit unit);

    /**
     * Advances the current {@link #nanoTime()} by the given amount of time.
     *
     * @param amountMillis the number of milliseconds to advance this ticker by.
     *
     * <p>以毫秒为单位推进时钟，等价于 {@code advance(amountMillis, MILLISECONDS)}。</p>
     */
    default void advanceMillis(long amountMillis) {
        advance(amountMillis, TimeUnit.MILLISECONDS);
    }
}
