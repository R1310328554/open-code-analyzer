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

import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * Spring Boot Actuator 缓存指标绑定提供者（Boot 2.x/3.x {@code actuate.metrics.cache} 包）。
 * <p>为 {@link RedissonCache} 实例创建 {@link RedissonCacheMetrics}。
 *
 * @author Nikita Koksharov
 */
public class RedissonCacheMeterBinderProvider implements CacheMeterBinderProvider<RedissonCache> {

    /** 返回绑定 hit/miss/put/eviction 指标的 {@link RedissonCacheMetrics}。 */
    @Override
    public MeterBinder getMeterBinder(RedissonCache cache, Iterable<Tag> tags) {
        return new RedissonCacheMetrics(cache, tags);
    }
    
}
