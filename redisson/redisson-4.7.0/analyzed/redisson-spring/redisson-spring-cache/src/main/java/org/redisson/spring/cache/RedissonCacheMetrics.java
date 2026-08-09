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
package org.redisson.spring.cache;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;

/**
 * {@link RedissonCache} 的 Micrometer {@link io.micrometer.core.instrument.binder.cache.CacheMeterBinder}。
 * <p>暴露 size、hit、miss、put、eviction 等标准缓存指标。
 *
 * @author Nikita Koksharov
 */
public class RedissonCacheMetrics extends CacheMeterBinder {

    private final RedissonCache cache;
    
    public RedissonCacheMetrics(RedissonCache cache, Iterable<Tag> tags) {
        super(cache, cache.getName(), tags);
        this.cache = cache;
    }
    
    /**
     * 便捷方法：创建绑定器并注册到 {@link MeterRegistry}。
     * @param registry 指标注册表
     * @param cache 待监控的 {@link RedissonCache}
     * @param tags 附加标签
     * @return 同一 cache 实例（便于链式使用）
     */
    public static RedissonCache monitor(MeterRegistry registry, RedissonCache cache, Iterable<Tag> tags) {
        new RedissonCacheMetrics(cache, tags).bindTo(registry);
        return cache;
    }

    /** 当前缓存条目数（底层 {@link RMap#size()}）。 */
    @Override
    protected Long size() {
        return (long) cache.getNativeCache().size();
    }

    /** 命中次数，来自 {@link RedissonCache#getCacheHits()}。 */
    @Override
    protected long hitCount() {
        return cache.getCacheHits();
    }

    @Override
    protected Long missCount() {
        return cache.getCacheMisses();
    }

    @Override
    protected Long evictionCount() {
        return cache.getCacheEvictions();
    }

    @Override
    protected long putCount() {
        return cache.getCachePuts();
    }

    @Override
    protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
    }

}
