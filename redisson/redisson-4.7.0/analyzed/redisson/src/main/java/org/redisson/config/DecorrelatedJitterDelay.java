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
package org.redisson.config;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 去相关抖动（Decorrelated Jitter）延迟策略。
 * <p>
 * 以上一次退避时长为基准引入随机性，避免指数退避过快增长，
 * 同时使相邻两次等待时长相互独立，降低重试风暴同步风险。
 *
 * @author Nikita Koksharov
 *
 */
public class DecorrelatedJitterDelay implements DelayStrategy {

    /** 最小延迟（基准值）。 */
    private final Duration minDelay;
    /** 最大延迟上限。 */
    private final Duration maxDelay;
    /** 上一次计算出的延迟，用于去相关随机。 */
    private Duration previousDelay;

    /**
     * 创建去相关抖动延迟策略。
     *
     * @param minDelay 最小延迟（基准延迟）
     * @param maxDelay 允许的最大延迟
     */
    public DecorrelatedJitterDelay(Duration minDelay, Duration maxDelay) {
        Objects.requireNonNull(minDelay);
        Objects.requireNonNull(maxDelay);

        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.previousDelay = Duration.ZERO;
    }

    /** 基于 minDelay 与 previousDelay 计算本次等待时长，并更新 previousDelay。 */
    @Override
    public Duration calcDelay(int attempt) {
        long previousMs;
        if (previousDelay.isZero()) {
            previousMs = minDelay.toMillis();
        } else {
            previousMs = previousDelay.toMillis();
        }

        long randomRange = previousMs * 3;
        long randomComponent = 0;
        if (randomRange != 0) {
            randomComponent = ThreadLocalRandom.current().nextLong(0, randomRange);
        }

        long newDelayMs = Math.min(
                minDelay.toMillis() + randomComponent,
                maxDelay.toMillis()
        );

        previousDelay = Duration.ofMillis(newDelayMs);
        return previousDelay;
    }
}
