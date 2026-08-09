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

import java.time.Duration;
import java.util.Objects;

/**
 * {@link RGcra} 分布式 GCRA 限流器的速率配置快照。
 * <p>
 * 由 {@link RGcra#trySetRate} 或 {@link RGcra#setRate} 设置。
 *
 * @author Nikita Koksharov
 */
public final class GcraConfig {

    private final long maxBurst;
    private final long tokensPerPeriod;
    private final Duration period;

    /** @param maxBurst 最大突发令牌数
     *  @param tokensPerPeriod 每周期补充令牌数
     *  @param period 补充周期 */
    public GcraConfig(long maxBurst, long tokensPerPeriod, Duration period) {
        this.maxBurst = maxBurst;
        this.tokensPerPeriod = tokensPerPeriod;
        this.period = period;
    }

    /** @return 最大突发令牌容量 */

    public long getMaxBurst() {
        return maxBurst;
    }

    /** @return 每个补充周期恢复的令牌数 */

    public long getTokensPerPeriod() {
        return tokensPerPeriod;
    }

    /** @return 令牌补充周期 */

    public Duration getPeriod() {
        return period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GcraConfig that = (GcraConfig) o;
        return maxBurst == that.maxBurst
                && tokensPerPeriod == that.tokensPerPeriod
                && Objects.equals(period, that.period);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxBurst, tokensPerPeriod, period);
    }

    @Override
    public String toString() {
        return "GcraConfig{"
                + "maxBurst=" + maxBurst
                + ", tokensPerPeriod=" + tokensPerPeriod
                + ", period=" + period
                + '}';
    }
}
