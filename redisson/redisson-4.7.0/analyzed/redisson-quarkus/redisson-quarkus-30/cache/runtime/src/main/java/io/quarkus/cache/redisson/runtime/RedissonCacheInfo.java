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

import java.time.Duration;
import java.util.Optional;

/**
 * 单个 Quarkus Redisson 缓存的运行时元数据（由构建期名称 + 配置合并而成）。
 *
 * @author Nikita Koksharov
 */
public class RedissonCacheInfo {

    /** 缓存最大条目数；超出时按 LRU 淘汰（仅 STANDARD MapCache 支持）。 */
    /**
     * The maximum size of this cache.
     * Superfluous elements are evicted using LRU algorithm.
     */
    public Optional<Integer> maxSize = Optional.empty();

    /** Quarkus 缓存逻辑名，同时作为 Redis Map 名称。 */
    /**
     * The cache name
     */
    public String name;

    /** 条目访问后过期时间（max-idle，对应 expireAfterAccess）。 */
    /**
     * The default time to live of the item stored in the cache
     */
    public Optional<Duration> expireAfterAccess = Optional.empty();

    /** 条目写入后过期时间（TTL，对应 expireAfterWrite）。 */
    /**
     * The default time to live to add to the item once read
     */
    public Optional<Duration> expireAfterWrite = Optional.empty();

    public Optional<CacheImplementation> implementation = Optional.empty();

}
