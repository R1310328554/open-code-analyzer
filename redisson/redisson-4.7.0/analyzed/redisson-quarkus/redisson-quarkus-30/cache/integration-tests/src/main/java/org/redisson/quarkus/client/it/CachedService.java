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
package org.redisson.quarkus.client.it;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 集成测试用缓存服务：演示 {@link io.quarkus.cache.CacheResult} 与 Redisson 缓存联动。
 * <p>{@link #cache1} 与 {@link #cache2} 分别映射不同缓存命名空间。
 */
@ApplicationScoped
public class CachedService {

    static final String CACHE1 = "cache1";
    static final String CACHE2 = "cache2";

    /** 按字符串键缓存随机 UUID；相同键应命中 Redis 缓存。 */
    @CacheResult(cacheName = CACHE1)
    public String cache1(String key) {
        return UUID.randomUUID().toString();
    }

    /** 按 Long 键缓存随机长整型；用于验证多缓存实例隔离。 */
    @CacheResult(cacheName = CACHE2)
    public Long cache2(Long val) {
        return ThreadLocalRandom.current().nextLong();
    }
}