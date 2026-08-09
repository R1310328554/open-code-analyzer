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
 * 全抖动（Full Jitter）延迟策略：对指数退避结果施加完全随机化。
 * <p>
 * 在 [0, exponentialBackoff] 区间均匀随机，最大化去同步效果，
 * 是 AWS 推荐的退避策略之一。
 *
 * @author Nikita Koksharov
 *
 */
public class FullJitterDelay implements DelayStrategy {

    /** 首次重试的基础延迟。 */
    private final Duration baseDelay;
    /** 指数退避的上限。 */
    private final Duration maxDelay;

    /**
     * 创建全抖动延迟策略。
     *
     * @param baseDelay 第一次重试的基础延迟
     * @param maxDelay 延迟上限
     */
    public FullJitterDelay(Duration baseDelay, Duration maxDelay) {
        Objects.requireNonNull(baseDelay);
        Objects.requireNonNull(maxDelay);

        this.baseDelay = baseDelay;
        this.maxDelay = maxDelay;
    }

    /** 计算指数退避后在 [0, backoff] 内均匀随机。 */
    @Override
    public Duration calcDelay(int attempt) {
        long baseMs = baseDelay.toMillis();
        long maxMs = maxDelay.toMillis();

        long exponentialDelayMs;
        if (attempt >= 63 || baseMs <= 0) {
            exponentialDelayMs = maxMs;
        } else {
            long shifted = 1L << attempt;

            if (baseMs > maxMs / shifted) {
                exponentialDelayMs = maxMs;
            } else {
                exponentialDelayMs = Math.min(baseMs * shifted, maxMs);
            }
        }

        exponentialDelayMs = Math.max(exponentialDelayMs, 1);

        long jitteredDelayMs = ThreadLocalRandom.current().nextLong(0, exponentialDelayMs + 1);
        return Duration.ofMillis(jitteredDelayMs);
    }
}
