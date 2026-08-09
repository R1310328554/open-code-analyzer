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

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.cache.metrics.CacheMeterBinderProvider;

/**
 * Spring Boot 4.0+ 缓存指标绑定提供者（{@code org.springframework.boot.cache.metrics} 包）。
 * <p>接口签名与 Actuator 版相同，适配 Boot 4 模块化包结构。
 *
 * @author Nikita Koksharov
 */
public class RedissonCacheMeterBinderProviderV4 implements CacheMeterBinderProvider<RedissonCache> {

    /** 创建 {@link RedissonCacheMetrics} 以暴露 Micrometer 缓存指标。 */
    @Override
    public MeterBinder getMeterBinder(RedissonCache cache, Iterable<Tag> tags) {
        return new RedissonCacheMetrics(cache, tags);
    }
    
}
