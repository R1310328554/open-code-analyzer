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

import io.quarkus.cache.Cache;
import io.smallrye.mutiny.Uni;

/**
 * 扩展 Quarkus {@link Cache} 的 Redisson 专用异步 API。
 * <p>所有方法返回 {@link Uni}，底层映射 Redisson {@code *Async} 操作。
 *
 * @author Nikita Koksharov
 */
public interface RedissonCache extends Cache {

    /** 写入键值；若配置了 TTL/max-idle 则附带过期策略。 */
    <K, V> Uni<V> put(K key, V value);

    /** 仅当键不存在时写入并返回旧值（或 null）。 */
    <K, V> Uni<V> putIfAbsent(K key, V value);

    <K, V> Uni<V> putIfExists(K key, V value);

    <K, V> Uni<Boolean> fastPut(K key, V value);

    <K, V> Uni<Boolean> fastPutIfAbsent(K key, V value);

    <K, V> Uni<Boolean> fastPutIfExists(K key, V value);

    /** 异步读取；键缺失时返回 {@code defaultValue}。 */
    <K, V> Uni<V> getOrDefault(K key, V defaultValue);

}
