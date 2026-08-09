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
package org.redisson.api;

/**
 * 带条目过期与本地缓存的 Map 同步 API。
 * <p>
 * 继承 {@link RMapCache} 的 TTL 能力，并在实例侧维护本地缓存以加速读操作。
 *
 * @author Nikita Koksharov
 * @param <K> Map 键类型
 * @param <V> Map 值类型
 */
public interface RLocalCachedMapCache<K, V> extends RMapCache<K, V>, RLocalCachedMap<K, V> {

    /**
     * 预热本地缓存值；不保证加载全部值，适合无逐出策略且整表本地缓存的场景。
     * will preload approximately all (all if no concurrent mutating activity)
     * 适用于无逐出策略、整表本地缓存的场景
     */
    void preloadCache();

}
