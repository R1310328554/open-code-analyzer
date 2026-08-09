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

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 支持过期时间（TTL）的 Redisson 对象基类接口。
 * <p>提供 {@code expire}、{@code clearExpire} 及条件过期等能力。
 *
 * @author Nikita Koksharov
 */
public interface RExpirable extends RObject, RExpirableAsync {

    /**
     * 已废弃，请改用 {@link #expire(Duration)}。
     *
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 见方法说明
     */
    @Deprecated
    boolean expire(long timeToLive, TimeUnit timeUnit);

    /**
     * 已废弃，请改用 {@link #expire(Instant)}。
     *
     * @param timestamp 过期时间戳
     * @return 见方法说明
     */
    @Deprecated
    boolean expireAt(long timestamp);

    /**
     * 已废弃，请改用 {@link #expire(Instant)}。
     *
     * @param timestamp 过期时间戳
     * @return 见方法说明
     */
    @Deprecated
    boolean expireAt(Date timestamp);

    /**
     * 为对象设置绝对过期时刻；到期后 Redis 键将自动删除。
     * the key will automatically be deleted.
     *
     * @param time 过期时刻
     * @return 见方法说明
     */
    boolean expire(Instant time);

    /**
     * 仅当对象已设置过期时间时，才更新为新的绝对过期时刻（Redis 7.0+）。
     * When expire date comes the object will automatically be deleted.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上.</b>
     *
     * @param time 过期时刻
     * @return 见方法说明
     */
    boolean expireIfSet(Instant time);

    /**
     * 仅当对象尚未设置过期时间时，才设置绝对过期时刻（Redis 7.0+）。
     * When expire date comes the object will automatically be deleted.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上.</b>
     *
     * @param time 过期时刻
     * @return 见方法说明
     */
    boolean expireIfNotSet(Instant time);

    /**
     * 仅当新绝对过期时刻晚于当前值时才更新（Redis 7.0+）。
     * When expire date comes the object will automatically be deleted.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上.</b>
     *
     * @param time 过期时刻
     * @return 见方法说明
     */
    boolean expireIfGreater(Instant time);

    /**
     * 仅当新绝对过期时刻早于当前值时才更新（Redis 7.0+）。
     * When expire date comes the object will automatically be deleted.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上.</b>
     *
     * @param time 过期时刻
     * @return 见方法说明
     */
    boolean expireIfLess(Instant time);

    /**
     * 为对象设置相对过期时长；到期后 Redis 键将自动删除。
     * the key will automatically be deleted.
     *
     * @param duration 过期时长
     * @return 见方法说明
     */
    boolean expire(Duration duration);

    /**
     * 仅当对象已设置 TTL 时，才更新为新的相对过期时长（Redis 7.0+）。
     * After the timeout has expired, the key will automatically be deleted.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上.</b>
     *
     * @param duration 过期时长
     * @return 见方法说明
     */
    boolean expireIfSet(Duration duration);

    /**
     * 仅当对象尚未设置 TTL 时，才设置相对过期时长（Redis 7.0+）。
     * After the timeout has expired, the key will automatically be deleted.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上.</b>
     *
     * @param duration 过期时长
     * @return 见方法说明
     */
    boolean expireIfNotSet(Duration duration);

    /**
     * 仅当新 TTL 长于当前值时才更新（Redis 7.0+）。
     * After the timeout has expired, the key will automatically be deleted.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上.</b>
     *
     * @param duration 过期时长
     * @return 见方法说明
     */
    boolean expireIfGreater(Duration duration);

    /**
     * 仅当新 TTL 短于当前值时才更新（Redis 7.0+）。
     * After the timeout has expired, the key will automatically be deleted.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上.</b>
     *
     * @param duration 过期时长
     * @return 见方法说明
     */
    boolean expireIfLess(Duration duration);

    /**
     * 清除对象的过期时间或绝对过期时刻。
     *
     * @return 见方法说明
     *         <code>false</code> if object does not exist or does not have an associated timeout
     */
    boolean clearExpire();

    /**
     * 返回对象剩余存活时间（毫秒）。
     *
     * @return 剩余毫秒数
     *          -2 if the key does not exist.
     *          -1 if the key exists but has no associated expire.
     */
    long remainTimeToLive();

    /**
     * 返回对象绝对过期时刻的 Unix 毫秒时间戳（Redis 7.0+）。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上.</b>
     *
     * @return Unix 毫秒时间戳
     *          -2 if the key does not exist.
     *          -1 if the key exists but has no associated expiration time.
     */
    long getExpireTime();
}
