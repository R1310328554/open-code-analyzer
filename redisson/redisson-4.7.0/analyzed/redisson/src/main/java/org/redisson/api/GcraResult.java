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

import java.util.Objects;

/**
 * Redis {@code GCRA} 命令的限流判定结果。
 * <p>
 * 表示请求令牌是否被限流，以及当前可用令牌与重试等待时间。
 *
 * @author Su Ko
 */
public final class GcraResult {

    private final boolean limited;
    private final long maxTokens;
    private final long availableTokens;
    private final long retryAfterSeconds;
    private final long fullBurstAfterSeconds;

    /** @param limited 是否被限流
     *  @param maxTokens 最大令牌容量
     *  @param availableTokens 当前可用令牌数
     *  @param retryAfterSeconds 获取所需令牌需等待的秒数
     *  @param fullBurstAfterSeconds 恢复满突发容量需等待的秒数 */
    public GcraResult(boolean limited, long maxTokens, long availableTokens,
                      long retryAfterSeconds, long fullBurstAfterSeconds) {
        this.limited = limited;
        this.maxTokens = maxTokens;
        this.availableTokens = availableTokens;
        this.retryAfterSeconds = retryAfterSeconds;
        this.fullBurstAfterSeconds = fullBurstAfterSeconds;
    }

    /** @return 若请求令牌无法获取则为 {@code true}（已触发限流） */

    public boolean isLimited() {
        return limited;
    }

    /** @return 最大突发令牌容量 */

    public long getMaxTokens() {
        return maxTokens;
    }

    /** @return 当前可用令牌数 */

    public long getAvailableTokens() {
        return availableTokens;
    }

    /** @return 获取所需令牌前需等待的秒数 */

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    /** @return 恢复满突发容量前需等待的秒数 */

    public long getFullBurstAfterSeconds() {
        return fullBurstAfterSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GcraResult that = (GcraResult) o;
        return limited == that.limited
                && maxTokens == that.maxTokens
                && availableTokens == that.availableTokens
                && retryAfterSeconds == that.retryAfterSeconds
                && fullBurstAfterSeconds == that.fullBurstAfterSeconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(limited, maxTokens, availableTokens, retryAfterSeconds, fullBurstAfterSeconds);
    }

    @Override
    public String toString() {
        return "GcraResult{"
                + "limited=" + limited
                + ", maxTokens=" + maxTokens
                + ", availableTokens=" + availableTokens
                + ", retryAfterSeconds=" + retryAfterSeconds
                + ", fullBurstAfterSeconds=" + fullBurstAfterSeconds
                + '}';
    }
}
