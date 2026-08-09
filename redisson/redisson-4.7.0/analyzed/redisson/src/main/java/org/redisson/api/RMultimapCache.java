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

import java.util.concurrent.TimeUnit;

/**
 * 带 per-key TTL 的 Multimap 基础接口，一个键可映射多个值。
 * <p>继承 {@link RMultimap} 全部能力，并支持为单个键设置过期时间。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RMultimapCache<K, V> extends RMultimap<K, V>, RMultimapCacheAsync<K, V> {

    /**
     * 为指定键设置过期时间；到期后键及其全部值自动删除。
     * 
     * @param key 映射键
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 键存在且设置成功时为 {@code true}；键不存在时为 {@code false}
     */
    boolean expireKey(K key, long timeToLive, TimeUnit timeUnit);
    
}
