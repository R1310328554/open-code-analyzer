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
 * 带条目过期与本地缓存的 Map Reactor API。
 * <p>
 * 组合 {@link RMapCacheReactive} 与 {@link RLocalCachedMapReactive} 能力。
 *
 * @author Nikita Koksharov
 * @param <K> Map 键类型
 * @param <V> Map 值类型
 */
public interface RLocalCachedMapCacheReactive<K, V> extends RMapCacheReactive<K, V>, RLocalCachedMapReactive<K, V> {

}
