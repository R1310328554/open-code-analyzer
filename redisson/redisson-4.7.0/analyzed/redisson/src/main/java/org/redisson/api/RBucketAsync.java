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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * 单对象持有者（Bucket）的异步 API；单个对象最大 512MB。
 *
 * @author Nikita Koksharov
 * @param <V> 对象类型
 */
public interface RBucketAsync<V> extends RExpirableAsync {

    /**
     * 返回对象序列化后的字节大小。
     *
     * @return 字节数
     */
    RFuture<Long> sizeAsync();
    
    /**
     * 读取持有者中存储的对象。
     *
     * @return 对象值
     */
    RFuture<V> getAsync();
    
    /**
     * 读取并删除持有者中的对象。
     *
     * @return 对象值
     */
    RFuture<V> getAndDeleteAsync();

    /**
     * 已废弃，请改用 {@link #setIfAbsentAsync(Object)}。
     *
     * @param value 待设置的值
     * @return 设置成功为 {@code true}，键已存在为 {@code false}
     */
    @Deprecated
    RFuture<Boolean> trySetAsync(V value);

    /**
     * 已废弃，请改用 {@link #setIfAbsentAsync(Object, Duration)}。
     *
     * @param value 待设置的值
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 设置成功为 {@code true}，键已存在为 {@code false}
     */
    @Deprecated
    RFuture<Boolean> trySetAsync(V value, long timeToLive, TimeUnit timeUnit);

    /**
     * 仅当持有者不存在时设置值（SET NX）。
     *
     * @param value 待设置的值
     * @return 设置成功为 {@code true}，键已存在为 {@code false}
     */
    RFuture<Boolean> setIfAbsentAsync(V value);

    /**
     * 仅当持有者不存在时设置值并指定过期时间。
     *
     * @param value 待设置的值
     * @param duration 过期时长
     * @return 设置成功为 {@code true}，键已存在为 {@code false}
     */
    RFuture<Boolean> setIfAbsentAsync(V value, Duration duration);

    /**
     * 仅当持有者已存在时设置值（SET XX）。
     *
     * @param value 待设置的值
     * @return 设置成功为 {@code true}，键不存在为 {@code false}
     */
    RFuture<Boolean> setIfExistsAsync(V value);

    /**
     * 已废弃，请改用 {@link #setIfExistsAsync(Object, Duration)}。
     *
     * @param value 待设置的值
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 设置成功为 {@code true}，键不存在为 {@code false}
     */
    RFuture<Boolean> setIfExistsAsync(V value, long timeToLive, TimeUnit timeUnit);

    /**
     * 仅当持有者已存在时设置值并指定过期时间。
     *
     * @param value 待设置的值
     * @param duration 过期时长
     * @return 设置成功为 {@code true}，键不存在为 {@code false}
     */
    RFuture<Boolean> setIfExistsAsync(V value, Duration duration);

    /**
     * 原子比较并设置：当前值序列化结果与期望值相等时写入新值。
     *
     * @param expect 期望值
     * @param update 新值
     * @return 成功为 {@code true}，期望值不匹配为 {@code false}
     */
    RFuture<Boolean> compareAndSetAsync(V expect, V update);

    /**
     * 按 {@code args} 指定条件原子设置值。
     * <ul>
     *   <li>{@link CompareAndSetArgs#expected(Object)} — 任意 Redis/Valkey 版本</li>
     *   <li>{@link CompareAndSetArgs#unexpected(Object)} — 任意 Redis/Valkey 版本</li>
     *   <li>{@link CompareAndSetArgs#expectedDigest(String)} — 需 Redis 8.4+，使用 SET IFDEQ</li>
     *   <li>{@link CompareAndSetArgs#unexpectedDigest(String)} — 需 Redis 8.4+，使用 SET IFDNE</li>
     * </ul>
     *
     * @param args 比较并设置参数
     * @return 条件满足并成功为 {@code true}，否则 {@code false}
     */
    RFuture<Boolean> compareAndSetAsync(CompareAndSetArgs<V> args);

    /**
     * 按值比较条件删除 Bucket。
     * <ul>
     *    <li>{@link CompareAndDeleteArgs#expected(Object)} — 任意 Redis/Valkey 版本</li>
     *    <li>{@link CompareAndDeleteArgs#unexpected(Object)} — 任意 Redis/Valkey 版本</li>
     *    <li>{@link CompareAndDeleteArgs#expectedDigest(String)} — 需 Redis 8.4+</li>
     *    <li>{@link CompareAndDeleteArgs#unexpectedDigest(String)} — 需 Redis 8.4+</li>
     * </ul>
     *
     * @param args 比较参数
     * @return 删除成功为 {@code true}，否则 {@code false}
     */
    RFuture<Boolean> compareAndDeleteAsync(CompareAndDeleteArgs<V> args);

    /**
     * 读取当前值并用 {@code newValue} 替换。
     *
     * @param newValue 新值
     * @return 替换前的值
     */
    RFuture<V> getAndSetAsync(V newValue);

    /**
     * 已废弃，请改用 {@link #getAndSetAsync(Object, Duration)}。
     *
     * @param value 新值
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 替换前的值
     */
    RFuture<V> getAndSetAsync(V value, long timeToLive, TimeUnit timeUnit);

    /**
     * 读取当前值并替换为新值，同时设置过期时间。
     *
     * @param value 新值
     * @param duration 过期时长
     * @return 替换前的值
     */
    RFuture<V> getAndSetAsync(V value, Duration duration);

    /**
     * 读取当前值并为其设置过期时长。
     * <p>需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param duration 存活时长
     * @return 当前值
     */
    RFuture<V> getAndExpireAsync(Duration duration);

    /**
     * 读取当前值并设置绝对过期时刻。
     * <p>需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param time 过期时刻
     * @return 当前值
     */
    RFuture<V> getAndExpireAsync(Instant time);

    /**
     * 读取当前值并清除已设置的过期时间。
     * <p>需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @return 当前值
     */
    RFuture<V> getAndClearExpireAsync();

    /**
     * 将对象写入持有者。
     *
     * @param value 待设置的值
     */
    RFuture<Void> setAsync(V value);

    /**
     * 已废弃，请改用 {@link #setAsync(Object, Duration)}。
     *
     * @param value 待设置的值
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     */
    RFuture<Void> setAsync(V value, long timeToLive, TimeUnit timeUnit);

    /**
     * 写入对象并设置过期时长。
     *
     * @param value 待设置的值
     * @param duration 过期时长
     */
    RFuture<Void> setAsync(V value, Duration duration);

    /**
     * 设置新值并保留原有 TTL。
     * <p>需要 <b>Redis 6.0.0 及以上</b>。
     *
     * @param value 待设置的值
     */
    RFuture<Void> setAndKeepTTLAsync(V value);

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
    RFuture<Integer> addListenerAsync(ObjectListener listener);

    /**
     * 返回本 Bucket 与指定名称 Bucket 存储数据的公共前缀部分。
     *
     * @param name 另一 Bucket 名称
     * @return 公共数据部分
     */
    RFuture<V> findCommonAsync(String name);

    /**
     * 返回本 Bucket 与指定名称 Bucket 公共前缀部分的字节长度。
     *
     * @param name 另一 Bucket 名称
     * @return 公共部分长度
     */
    RFuture<Long> findCommonLengthAsync(String name);

    /**
     * 返回 Bucket 值的 XXH3 哈希摘要（十六进制字符串）。
     * <p>需要 <b>Redis 8.4.0 及以上</b>。
     *
     * @return 哈希摘要；Bucket 不存在时为 {@code null}
     */
    RFuture<String> getDigestAsync();

}
