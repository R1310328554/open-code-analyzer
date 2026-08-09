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
 * 支持 per-entry TTL 的 List Multimap（Redis 原生过期）。
 * <p>保持插入顺序，允许值重复；使用 Redis 原生键 TTL，而非定时驱逐任务。
 * <p>
 * Requires <b>Redis 7.4.0 and higher.</b>
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RListMultimapCacheNativeRx<K, V> extends RListMultimapRx<K, V>, RMultimapCacheRx<K, V> {

}
