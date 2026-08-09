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

import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * {@link RMultimapCache} 的 Reactor API。
 * <p>各方法返回 {@link Mono}；支持为单个键设置过期时间。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RMultimapCacheReactive<K, V> {

    /**
     * 为指定键设置过期时间；到期后键及其全部值自动删除。
     *
     * @param key 映射键
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 发出 {@code true}（键存在且设置成功）或 {@code false}（键不存在）的 Mono/Single
     */
    Mono<Boolean> expireKey(K key, long timeToLive, TimeUnit timeUnit);
}
