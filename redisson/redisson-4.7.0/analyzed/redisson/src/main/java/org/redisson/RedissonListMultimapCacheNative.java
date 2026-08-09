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
package org.redisson;

import org.redisson.api.RFuture;
import org.redisson.api.RListMultimapCacheNative;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;

import java.util.concurrent.TimeUnit;

/**
 * 使用 Redis 原生键 TTL 的 {@link org.redisson.api.RListMultimapCacheNative} 实现。
 * <p>与 {@link RedissonListMultimapCache} 不同，过期由 Redis {@code EXPIRE} 直接作用于 List 键。
 *
 * @author Nikita Koksharov
 * @param <K> 映射键类型
 * @param <V> 列表元素类型
 */
public class RedissonListMultimapCacheNative<K, V> extends RedissonListMultimap<K, V> implements RListMultimapCacheNative<K, V> {

    private final RedissonMultimapCacheNative<K> baseCache;

    /** 使用默认 codec 构造。 */
    public RedissonListMultimapCacheNative(CommandAsyncExecutor connectionManager, String name) {
        super(connectionManager, name);
        baseCache = new RedissonMultimapCacheNative<>(connectionManager, this, prefix);
    }

    /** @param codec 键与值的编解码器 */
    public RedissonListMultimapCacheNative(Codec codec, CommandAsyncExecutor connectionManager, String name) {
        super(codec, connectionManager, name);
        baseCache = new RedissonMultimapCacheNative<>(connectionManager, this, prefix);
    }

    @Override
    public boolean expireKey(K key, long timeToLive, TimeUnit timeUnit) {
        return get(expireKeyAsync(key, timeToLive, timeUnit));
    }
    
    @Override
    public RFuture<Boolean> expireKeyAsync(K key, long timeToLive, TimeUnit timeUnit) {
        return baseCache.expireKeyAsync(key, timeToLive, timeUnit);
    }
    
    @Override
    public RFuture<Boolean> expireAsync(long timeToLive, TimeUnit timeUnit, String param, String... keys) {
        return baseCache.expireAsync(timeToLive, timeUnit, param);
    }

    @Override
    protected RFuture<Boolean> expireAtAsync(long timestamp, String param, String... keys) {
        return baseCache.expireAtAsync(timestamp, param);
    }

    @Override
    public RFuture<Boolean> clearExpireAsync() {
        return baseCache.clearExpireAsync();
    }

}
