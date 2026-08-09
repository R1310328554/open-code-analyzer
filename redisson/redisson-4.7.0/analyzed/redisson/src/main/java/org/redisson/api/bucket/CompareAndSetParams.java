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
package org.redisson.api.bucket;

import java.time.Duration;
import java.time.Instant;

/**
 * {@link CompareAndSetStep} 与 {@link CompareAndSetArgs} 的默认实现。
 * <p>
 * 保存比较条件、新值以及 TTL/过期时间等可选参数，供 RBucket 内部执行 CAS 操作。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 值类型
 */
public final class CompareAndSetParams<V> implements CompareAndSetStep<V>, CompareAndSetArgs<V> {

    private final ConditionType conditionType;
    private V expectedValue;
    private V unexpectedValue;
    private String expectedDigest;
    private String unexpectedDigest;
    private V newValue;
    private Duration timeToLive;
    private Instant expireAt;

    CompareAndSetParams(ConditionType conditionType, V object) {
        this.conditionType = conditionType;
        if (conditionType == ConditionType.EXPECTED) {
            this.expectedValue = object;
        }
        if (conditionType == ConditionType.UNEXPECTED) {
            this.unexpectedValue = object;
        }
    }

    CompareAndSetParams(ConditionType conditionType, String digest) {
        this.conditionType = conditionType;
        if (conditionType == ConditionType.EXPECTED_DIGEST) {
            this.expectedDigest = digest;
        }
        if (conditionType == ConditionType.UNEXPECTED_DIGEST) {
            this.unexpectedDigest = digest;
        }
    }

    /** 设置条件满足时要写入的新值。 */
    @Override
    public CompareAndSetArgs<V> set(V value) {
        this.newValue = value;
        return this;
    }

    /** 设置写入后的生存时间。 */
    @Override
    public CompareAndSetArgs<V> timeToLive(Duration duration) {
        this.timeToLive = duration;
        return this;
    }

    /** 设置写入后的绝对过期时间。 */
    @Override
    public CompareAndSetArgs<V> expireAt(Instant time) {
        this.expireAt = time;
        return this;
    }

    /** 返回比较条件类型。 */
    public ConditionType getConditionType() {
        return conditionType;
    }

    public V getExpectedValue() {
        return expectedValue;
    }

    public V getUnexpectedValue() {
        return unexpectedValue;
    }

    public String getExpectedDigest() {
        return expectedDigest;
    }

    public String getUnexpectedDigest() {
        return unexpectedDigest;
    }

    /** 返回待写入的新值。 */
    public V getNewValue() {
        return newValue;
    }

    public Duration getTimeToLive() {
        return timeToLive;
    }

    public Instant getExpireAt() {
        return expireAt;
    }

}