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
package org.redisson.api.map;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * {@link PutArgs} 的默认实现，保存待写入条目及 TTL 相关选项。
 * <p>
 * 由 {@link PutArgs#entries(java.util.Map)} 工厂方法创建。
 */
public final class PutParams<K, V> implements PutArgs<K, V> {

    private boolean keepTTL;
    private Duration timeToLive;
    private Instant expireAt;

    private final Map<K, V> entries;

    public PutParams(Map<K, V> values) {
        this.entries = values;
    }

    /** 启用保留原 TTL 选项。 */
    @Override
    public PutArgs<K, V> keepTTL() {
        this.keepTTL = true;
        return this;
    }

    /** 设置生存时间。 */
    @Override
    public PutArgs<K, V> timeToLive(Duration ttl) {
        this.timeToLive = ttl;
        return this;
    }

    /** 设置绝对过期时间。 */
    @Override
    public PutArgs<K, V> expireAt(Instant time) {
        this.expireAt = time;
        return this;
    }

    /** 是否保留各键原有 TTL。 */
    public boolean isKeepTTL() {
        return keepTTL;
    }

    /** 返回设置的生存时长。 */
    public Duration getTimeToLive() {
        return timeToLive;
    }

    /** 返回设置的绝对过期时刻。 */
    public Instant getExpireAt() {
        return expireAt;
    }

    /** 返回待写入的键值映射。 */
    public Map<K, V> getEntries() {
        return entries;
    }
}
