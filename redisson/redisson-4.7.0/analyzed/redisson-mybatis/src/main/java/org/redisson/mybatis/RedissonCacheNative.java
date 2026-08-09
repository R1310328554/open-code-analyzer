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
package org.redisson.mybatis;

import org.redisson.MapCacheNativeWrapper;
import org.redisson.api.RMapCache;
import org.redisson.api.RMapCacheNative;
import org.redisson.api.RedissonClient;

/**
 * 基于 Redis 7+ {@link RMapCacheNative} 的 MyBatis 缓存实现。
 * <p>通过 {@link MapCacheNativeWrapper} 适配标准 {@link RMapCache} 接口；
 * 不支持 maxIdleTime 与 maxSize 配置。
 *
 * @author Nikita Koksharov
 */
public class RedissonCacheNative extends RedissonCache {

    /** @param id MyBatis 缓存命名空间 ID */
    public RedissonCacheNative(String id) {
        super(id);
    }

    /** Native Map 不支持 max-idle，调用时抛出 {@link IllegalArgumentException}。 */
    @Override
    public void setMaxIdleTime(long maxIdleTime) {
        throw new IllegalArgumentException("maxIdleTime setting isn't supported");
    }

    /** Native Map 不支持 LRU 容量限制，调用时抛出 {@link IllegalArgumentException}。 */
    @Override
    public void setMaxSize(int maxSize) {
        throw new IllegalArgumentException("maxSize setting isn't supported");
    }

    /** 使用 {@link RedissonClient#getMapCacheNative} 并包装为 {@link RMapCache}。 */
    @Override
    protected RMapCache<Object, Object> getMapCache(String id, RedissonClient redisson) {
        RMapCacheNative<Object, Object> cache = redisson.getMapCacheNative(id);
        return new MapCacheNativeWrapper<>(cache);
    }

}
