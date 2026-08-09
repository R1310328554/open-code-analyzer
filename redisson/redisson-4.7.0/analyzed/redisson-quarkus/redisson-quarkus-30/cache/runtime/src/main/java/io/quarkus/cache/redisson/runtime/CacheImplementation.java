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

/**
 * Quarkus Redisson 缓存底层 Redis 结构实现类型。
 * <p>STANDARD/NATIVE 为开源版可用；V2、LOCALCACHE、CLUSTERED 等需 PRO 版本。
 */
public enum CacheImplementation {

    /** 标准 {@link RMap}/{@link RMapCache} 实现。 */
    STANDARD,

    /** Native Map 实现（{@link RMapCacheNative}），不支持 max-idle 与 LRU。 */
    NATIVE,

    /** PRO 版 V2 结构（需联系 sales@redisson.pro）。 */
    V2,

    /** PRO 版本地缓存层。 */
    LOCALCACHE,

    LOCALCACHE_V2,

    CLUSTERED,

    CLUSTERED_LOCALCACHE

}
