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
package org.redisson.micronaut.cache;

import io.micronaut.cache.AbstractMapBasedSyncCache;
import io.micronaut.cache.AsyncCache;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.ArgumentUtils;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 基于 Redisson 的 Micronaut 同步缓存，继承 {@link AbstractMapBasedSyncCache}。
 * <p>{@link #mapCache} 非 null 时启用 TTL/max-idle；否则使用普通 {@link RMap}。
 *
 * @author Nikita Koksharov
 */
public class RedissonSyncCache extends AbstractMapBasedSyncCache<RMap<Object, Object>> {

    private final ConversionService conversionService;
    private final ExecutorService executorService;
    private final BaseCacheConfiguration configuration;
    private final RMapCache<Object, Object> mapCache;
    private final RMap<Object, Object> map;

    /** @param mapCache 带过期策略的 MapCache；纯 Map 模式为 null
     *  @param map 底层 Redis Map
     *  @param configuration 容量与过期配置
     */
    public RedissonSyncCache(ConversionService<?> conversionService,
                             RMapCache<Object, Object> mapCache,
                             RMap<Object, Object> map,
                             ExecutorService executorService,
                             BaseCacheConfiguration configuration) {
        super(conversionService, map);
        this.executorService = executorService;
        this.configuration = configuration;
        this.mapCache = mapCache;
        this.map = map;
        this.conversionService = conversionService;
        // 配置了 maxSize 时设置 MapCache LRU 上限。
        if (configuration.getMaxSize() != 0) {
            mapCache.setMaxSize(configuration.getMaxSize());
        }
    }

    @Override
    public String getName() {
        return getNativeCache().getName();
    }

    /** 键不存在时写入并返回先前值（Optional）。 */
    @NonNull
    @Override
    public <T> Optional<T> putIfAbsent(@NonNull Object key, @NonNull T value) {
        ArgumentUtils.requireNonNull("key", key);
        ArgumentUtils.requireNonNull("value", value);
        T res;
        if (mapCache != null) {
            res = (T) mapCache.putIfAbsent(key, value, configuration.getExpireAfterWrite().toMillis(), TimeUnit.MILLISECONDS,
                    configuration.getExpireAfterAccess().toMillis(), TimeUnit.MILLISECONDS);
        } else {
            res = (T) mapCache.putIfAbsent(key, value);
        }
        return Optional.ofNullable(res);
    }

    /** 键不存在时调用 supplier 获取值并写入；返回最终缓存值。 */
    @NonNull
    @Override
    public <T> T putIfAbsent(@NonNull Object key, @NonNull Supplier<T> value) {
        ArgumentUtils.requireNonNull("key", key);
        ArgumentUtils.requireNonNull("value", value);
        T val = value.get();
        T res;
        if (mapCache != null) {
            res = (T) mapCache.putIfAbsent(key, val, configuration.getExpireAfterWrite().toMillis(), TimeUnit.MILLISECONDS,
                    configuration.getExpireAfterAccess().toMillis(), TimeUnit.MILLISECONDS);
        } else {
            res = (T) mapCache.putIfAbsent(key, value);
        }
        return Optional.ofNullable(res).orElse(val);
    }

    /** 写入或覆盖条目；MapCache 模式下附带 TTL/max-idle。 */
    @Override
    public void put(@NonNull Object key, @NonNull Object value) {
        ArgumentUtils.requireNonNull("key", key);
        ArgumentUtils.requireNonNull("value", value);
        if (mapCache != null) {
            mapCache.fastPut(key, value, configuration.getExpireAfterWrite().toMillis(), TimeUnit.MILLISECONDS,
                    configuration.getExpireAfterAccess().toMillis(), TimeUnit.MILLISECONDS);
        } else {
            mapCache.fastPut(key, value);
        }
    }

    /** 返回共享同一 Redis 结构的 {@link RedissonAsyncCache} 视图。 */
    @NonNull
    @Override
    public AsyncCache<RMap<Object, Object>> async() {
        return new RedissonAsyncCache(mapCache, map, executorService, conversionService, configuration);
    }
}
