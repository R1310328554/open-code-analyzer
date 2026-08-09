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

import java.time.Duration;

/**
 * 基于租约（Lease）的缓存 Map Reactor API。
 * <p>
 * 缓存未命中时生成不透明租约令牌；各方法返回 {@link Mono}。
 *
 * @author nhancdt2602
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RLeasedMapReactive<K, V> {

    /**
     * 返回 {@code key} 对应的缓存值；未命中时返回 {@code null} 并尝试获取租约。
     * <p>
     * 未命中时尝试获取租约并与 {@code null} 一并返回。
     * 租约在 {@code leaseTimeToLive} 超时后自动释放。
     *
     * @param key 键名
     * @param leaseTimeToLive 租约存活时间
     * @return 缓存值或未命中时的租约信息
     */
    Mono<LeaseGetResult<V>> getWithLease(K key, Duration leaseTimeToLive);

    /**
     * 使 {@code key} 对应条目失效并删除当前租约令牌（如有）。
     *
     * @param key 键名
     * @return {@code true} if the map entry was removed ({@code HDEL} removed a field), {@code false} otherwise.
     *         The lease key is deleted in the same operation but does not affect this return value (the script result
     *         list records {@code HDEL} counts only).
     */
    Mono<Boolean> removeWithLease(K key);

    /**
     * 仅当 {@code leaseToken} 仍有效时，将 {@code value} 写入 {@code key}。
     *
     * @param key 键名
     * @param value 起始值
     * @param leaseToken 租约令牌
     * @return {@code true} if value has been stored, otherwise {@code false}
     */
    Mono<Boolean> putWithLease(K key, V value, String leaseToken);

    /**
     * 仅当 {@code leaseToken} 仍有效时，将 {@code value} 写入 {@code key}。
     *
     * @param key 键名
     * @param value 起始值
     * @param ttl 条目 TTL
     * @param leaseToken 租约令牌
     * @return {@code true} if value has been stored, otherwise {@code false}
     */
    Mono<Boolean> putWithLease(K key, V value, Duration ttl, String leaseToken);

    /**
     * 仅当 {@code leaseToken} 仍有效时，将 {@code value} 写入 {@code key}。
     *
     * @param key 键名
     * @param value 起始值
     * @param ttl 条目 TTL
     * @param maxIdleTime 最大空闲时间
     * @param leaseToken 租约令牌
     * @return {@code true} if value has been stored, otherwise {@code false}
     */
    Mono<Boolean> putWithLease(K key, V value, Duration ttl, Duration maxIdleTime, String leaseToken);
}

