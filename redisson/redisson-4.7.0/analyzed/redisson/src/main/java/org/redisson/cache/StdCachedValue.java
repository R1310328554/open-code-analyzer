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
package org.redisson.cache;

import org.redisson.misc.WrappedLock;

/**
 * 标准缓存值实现，持有强引用键值并跟踪 TTL 与空闲时间。
 * <p>
 * 实现 {@link CachedValue} 接口，供 LRU/LFU 等本地缓存策略使用。
 */

public class StdCachedValue<K, V> implements CachedValue<K, V> {

    /** 缓存键。 */
    private final K key;
    /** 缓存值（强引用）。 */
    private final V value;

    /** 存活时间（毫秒），0 表示不限。 */
    private final long ttl;
    /** 最大空闲时间（毫秒），0 表示不限。 */
    private final long maxIdleTime;

    /** 条目创建时间戳。 */
    private long creationTime;
    /** 最后一次访问时间戳。 */
    private long lastAccess;

    /** 条目级并发锁。 */
    private final WrappedLock lock = new WrappedLock();

    /**
     * 创建标准缓存值。
     *
     * @param key 键
     * @param value 值
     * @param ttl 存活时间（毫秒）
     * @param maxIdleTime 最大空闲时间（毫秒）
     */
    public StdCachedValue(K key, V value, long ttl, long maxIdleTime) {
        this.value = value;
        this.ttl = ttl;
        this.key = key;
        this.maxIdleTime = maxIdleTime;
        
        if (ttl != 0 || maxIdleTime != 0) {
            creationTime = System.currentTimeMillis();
            lastAccess = creationTime;
        }
    }

    /** 根据 TTL 与最大空闲时间判断条目是否已过期。 */
    @Override
    public boolean isExpired() {
        if (maxIdleTime == 0 && ttl == 0) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if (ttl != 0 && creationTime + ttl < currentTime) {
            return true;
        }
        if (maxIdleTime != 0 && lastAccess + maxIdleTime < currentTime) {
            return true;
        }
        return false;
    }

    /** 返回条目最早过期时间戳。 */
    @Override
    public long getExpireTime() {
        if (maxIdleTime == 0 && ttl == 0) {
            return 0;
        }
        long expireTime = Long.MAX_VALUE;
        if (maxIdleTime != 0) {
            expireTime = Math.min(expireTime, lastAccess + maxIdleTime);
        }
        if (ttl != 0) {
            expireTime = Math.min(expireTime, creationTime + ttl);
        }
        return expireTime;
    }

    @Override
    public K getKey() {
        return key;
    }

    /** 获取值并更新最后访问时间。 */
    @Override
    public V getValue() {
        lastAccess = System.currentTimeMillis();
        return value;
    }

    @Override
    public String toString() {
        return "CachedValue [key=" + key + ", value=" + value + "]";
    }

    /** 返回条目级并发锁。 */
    @Override
    public WrappedLock getLock() {
        return lock;
    }
}
