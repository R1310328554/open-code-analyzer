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
package io.quarkus.cache.redisson.runtime;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithParentName;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static io.quarkus.runtime.annotations.ConfigPhase.RUN_TIME;

/**
 * Quarkus {@code cache.redisson.*} 运行时配置根映射。
 * <p>{@link #defaultConfig()} 为全局默认；{@link #cachesConfig()} 按缓存名覆盖。
 *
 * @author Nikita Koksharov
 */
@ConfigRoot(phase = RUN_TIME)
@ConfigMapping(prefix = "cache.redisson")
public interface RedissonCachesConfig {

    /** 应用于所有 Redisson 缓存的默认配置（优先级最低）。 */
    /**
     * Default configuration applied to all Redis caches (lowest precedence)
     */
    @WithParentName
    RedissonCacheRuntimeConfig defaultConfig();

    /** 按缓存名附加的配置（优先级最高，覆盖 default）。 */
    /**
     * Additional configuration applied to a specific Redis cache (highest precedence)
     */
    @WithParentName
    Map<String, RedissonCacheRuntimeConfig> cachesConfig();

    interface RedissonCacheRuntimeConfig {

        /** 缓存最大条目数；超出时 LRU 淘汰；{@code 0} 表示无界（默认）。 */
        /**
         * Specifies maximum size of this cache.
         * Superfluous elements are evicted using LRU algorithm.
         * If <code>0</code> the cache is unbounded (default).
         */
        Optional<Integer> maxSize();

        /** 写入后过期时间（对应 {@link RedissonCacheInfo#expireAfterWrite}）。 */
        /**
         * Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after
         * the entry's creation, or the most recent replacement of its value.
         */
        Optional<Duration> expireAfterWrite();

        /** 访问后过期时间（max-idle，对应 {@link RedissonCacheInfo#expireAfterAccess}）。 */
        /**
         * Specifies that each entry should be automatically removed from the cache once a fixed duration has elapsed after
         * the last access of its value.
         */
        Optional<Duration> expireAfterAccess();

        /** 底层 Redis 结构实现（{@link CacheImplementation}）。 */
        /**
         * Specifies the cache implementation.
         */
        Optional<CacheImplementation> implementation();

    }


}
