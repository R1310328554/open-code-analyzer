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

import org.redisson.api.bucket.CompareAndDeleteArgs;
import org.redisson.api.bucket.CompareAndSetArgs;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;


/**
 * {@link RBucket} 的 Reactor 风格 API 接口。
 * <p>单个对象最大 512MB；各方法返回 {@link Mono}，用于非阻塞响应式编程。
 *
 * @author Nikita Koksharov
 * @param <V> 存储对象类型
 */
public interface RBucketReactive<V> extends RExpirableReactive {

    /**
     * 返回对象序列化后的字节大小。
     * 
     * @return 对象字节大小
     */
    Mono<Long> size();

    /**
     * 仅当 Redis 键不存在时设置值（NX）。
     *
     * @param value 值
     * @return 见方法说明
     *         element was already set
     */
    Mono<Boolean> setIfAbsent(V value);

    /**
     * 仅当键不存在时设置值并指定过期时长。
     *
     * @param value 值
     * @param duration 过期时长
     * @return 见方法说明
     *         element was already set
     */
    Mono<Boolean> setIfAbsent(V value, Duration duration);

    /**
     * 已废弃，请改用 {@link #setIfAbsent(Object)}。
     * 
     * @param value 值
     * @return 见方法说明
     *         element was already set
     */
    @Deprecated
    Mono<Boolean> trySet(V value);

    /**
     * 已废弃，请改用 {@link #setIfAbsent(Object, Duration)}。
     * 
     * @param value 值
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 见方法说明
     *         element was already set
     */
    @Deprecated
    Mono<Boolean> trySet(V value, long timeToLive, TimeUnit timeUnit);

    /**
     * 仅当键已存在时更新值（XX）。
     *
     * @param value 值
     * @return 见方法说明
     *         element wasn't set
     */
    Mono<Boolean> setIfExists(V value);

    /**
     * 已废弃，请改用 {@link #setIfExists(Object, Duration)}。
     *
     * @param value 值
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 见方法说明
     *         element wasn't set
     */
    @Deprecated
    Mono<Boolean> setIfExists(V value, long timeToLive, TimeUnit timeUnit);

    /**
     * 仅当键已存在时设置 {@code value} 并指定过期时长 {@code duration}。
     *
     * @param value 值
     * @param duration 过期时长
     * @return 见方法说明
     *         element wasn't set
     */
    Mono<Boolean> setIfExists(V value, Duration duration);

    /**
     * Atomically sets the value to the given updated value
     * only if serialized state of the current value equals 
     * to serialized state of the expected value.
     *
     * @param expect the expected value
     * @param update the new value
     * @return {@code true} if successful; or {@code false} if the actual value
     *         was not equal to the expected value.
     */
    Mono<Boolean> compareAndSet(V expect, V update);

    /**
     * Atomically sets the value if the condition specified in args is met.
     * <p>
     * Supports multiple comparison modes:
     * <ul>
     *   <li>{@link CompareAndSetArgs#expected(Object)} - compatible with any Redis/Valkey version</li>
     *   <li>{@link CompareAndSetArgs#unexpected(Object)} - compatible with any Redis/Valkey version</li>
     *   <li>{@link CompareAndSetArgs#expectedDigest(String)} - requires Redis 8.4+, uses SET IFDEQ</li>
     *   <li>{@link CompareAndSetArgs#unexpectedDigest(String)} - requires Redis 8.4+, uses SET IFDNE</li>
     * </ul>
     *
     * @param args compare-and-set arguments containing condition and new value
     * @return {@code true} if successful, {@code false} if condition was not met
     */
    Mono<Boolean> compareAndSet(CompareAndSetArgs<V> args);

    /**
     * Conditionally deletes the bucket based on value comparison.
     * <p>
     * <ul>
     *    <li> {@link CompareAndDeleteArgs#expected(Object)} - compatible with any Redis/Valkey version</li>
     *    <li> {@link CompareAndDeleteArgs#unexpected(Object)} - compatible with any Redis/Valkey version</li>
     *    <li> {@link CompareAndDeleteArgs#expectedDigest(String)} - requires Redis 8.4+</li>
     *    <li> {@link CompareAndDeleteArgs#unexpectedDigest(String)} - requires Redis 8.4+</li>
     * </ul>
     *
     * @param args comparison arguments
     * @return {@code true} if bucket was deleted, {@code false} otherwise
     */
    Mono<Boolean> compareAndDelete(CompareAndDeleteArgs<V> args);

    /**
     * 读取当前值并以 {@code newValue} 替换，返回旧值。
     * 
     * @param newValue 新值
     * @return 替换前的旧值
     */
    Mono<V> getAndSet(V newValue);

    /**
     * 已废弃，请改用 {@link #getAndSet(Object, Duration)}。
     * 
     * @param value 值
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 替换前的旧值
     */
    @Deprecated
    Mono<V> getAndSet(V value, long timeToLive, TimeUnit timeUnit);

    /**
     * Retrieves current element in the holder and replaces it
     * with <code>value</code> with defined expiration <code>duration</code>.
     *
     * @param value value to set
     * @param duration expiration duration
     * @return previous value
     */
    Mono<V> getAndSet(V value, Duration duration);

    /**
     * 读取当前值并为其设置过期时长。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上.</b>
     *
     * @param duration 过期时长
     * @return 当前值
     */
    Mono<V> getAndExpire(Duration duration);

    /**
     * 读取当前值并设置绝对过期时刻。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上.</b>
     *
     * @param time of exact object expiration moment
     * @return 当前值
     */
    Mono<V> getAndExpire(Instant time);

    /**
     * 读取当前值并清除已设置的过期时间。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上.</b>
     *
     * @return 当前值
     */
    Mono<V> getAndClearExpire();

    /**
     * 返回容器中存储的值。
     * 
     * @return 当前值
     */
    Mono<V> get();
    
    /**
     * 读取当前值并删除该 Redis 键。
     * 
     * @return 当前值
     */
    Mono<V> getAndDelete();

    /**
     * 将值写入容器。
     * 
     * @param value 值
     * @return void
     */
    Mono<Void> set(V value);

    /**
     * 已废弃，请改用 {@link #set(Object, Duration)}。
     * 
     * @param value 值
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return void
     */
    @Deprecated
    Mono<Void> set(V value, long timeToLive, TimeUnit timeUnit);

    /**
     * 写入 {@code value} 并设置过期时长 {@code duration}。
     *
     * @param value 值
     * @param duration 过期时长
     */
    Mono<Void> set(V value, Duration duration);

    /**
     * 设置新值并保留原有 TTL。
     * <p>
     * 需要 <b>Redis 6.0.0 及以上.</b>
     *
     * @param value 值
     * @return void
     */
    Mono<Void> setAndKeepTTL(V value);

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     * @see org.redisson.api.listener.SetObjectListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    Mono<Integer> addListener(ObjectListener listener);

    /**
     * Returns the common part of the data stored in this bucket
     * and a bucket defined by the <code>name</code>
     *
     * @param name second bucket
     * @return common part of the data
     */
    Mono<V> findCommon(String name);

    /**
     * Returns the length of the common part of the data stored in this bucket
     * and a bucket defined by the <code>name</code>
     *
     * @param name second bucket
     * @return common part of the data
     */
    Mono<Long> findCommonLength(String name);

    /**
     * Returns the hash digest of the value stored in this bucket as a hexadecimal string.
     * The digest is computed using the XXH3 hash algorithm.
     * <p>
     * Requires <b>Redis 8.4.0 or higher</b>.
     *
     * @return hash digest as hexadecimal string, or empty Mono if the bucket doesn't exist
     */
    Mono<String> getDigest();

}
