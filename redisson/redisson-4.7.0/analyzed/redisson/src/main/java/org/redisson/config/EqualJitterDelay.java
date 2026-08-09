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
 * 均等抖动（Equal Jitter）延迟策略：在指数退避基础上引入适度随机性。
 * <p>
 * 保留退避值的一半作为稳定部分，另一半随机取值；
 * 最终延迟介于 backoff/2 与 backoff 之间，兼顾可预测性与去同步效果。
 *
 * @author Nikita Koksharov
 *
 */
public class EqualJitterDelay implements DelayStrategy {

    /** 首次重试的基础延迟。 */
    final Duration baseDelay;
    /** 指数退避的上限。 */
    final Duration maxDelay;

    /**
     * 创建均等抖动延迟策略。
     *
     * @param baseDelay 第一次重试的基础延迟
     * @param maxDelay 延迟上限，限制指数增长
     */
    public EqualJitterDelay(Duration baseDelay, Duration maxDelay) {
        Objects.requireNonNull(baseDelay);
        Objects.requireNonNull(maxDelay);

        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
    }

    /** 按 attempt 计算指数退避，再应用均等抖动。 */
    @Override
    public Duration calcDelay(int attempt) {
        long baseMs = baseDelay.toMillis();
        long maxMs = maxDelay.toMillis();

        long exponentialDelayMs;
        if (attempt >= 63 || baseMs >= maxMs) {
            exponentialDelayMs = maxMs;
        } else {
            long shifted = 1L << attempt;

            if (shifted > maxMs / baseMs) {
                exponentialDelayMs = maxMs;
            } else {
                exponentialDelayMs = Math.min(baseMs * shifted, maxMs);
            }
        }

        long halfDelay = exponentialDelayMs / 2;
        long randomComponent = 0;
        if (halfDelay != 0) {
            randomComponent = ThreadLocalRandom.current().nextLong(0, halfDelay + 1);
        }

        return Duration.ofMillis(halfDelay + randomComponent);
    }



    /** 返回基础延迟配置。 */
    public Duration getBaseDelay() {
        return baseDelay;
    }

    /** 返回最大延迟配置。 */
    public Duration getMaxDelay() {
        return maxDelay;
    }
}
