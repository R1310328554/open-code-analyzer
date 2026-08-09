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
package org.redisson.api.bucket;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * RBucket 比较并设置（compare-and-set）操作的参数接口。
 * <p>
 * 先通过静态工厂方法创建比较条件，再调用 {@code set()} 指定新值，
 * 并可选择配置 TTL 或绝对过期时间。
 * <p>
 * 支持的比较模式：
 * <ul>
 *   <li>{@link #expected(Object)} — 当前值等于期望值时设置（任意 Redis/Valkey 版本）</li>
 *   <li>{@link #unexpected(Object)} — 当前值不等于指定值时设置（任意 Redis/Valkey 版本）</li>
 *   <li>{@link #expectedDigest(String)} — 摘要相等时设置（需 Redis 8.4+，SET IFDEQ）</li>
 *   <li>{@link #unexpectedDigest(String)} — 摘要不同时设置（需 Redis 8.4+，SET IFDNE）</li>
 * </ul>
 *
 * @author Nikita Koksharov
 *
 * @param <V> 值类型
 */
public interface CompareAndSetArgs<V> {

    /**
     * 创建「当前值等于期望值」时成功的比较条件。
     * <p>
     * 兼容任意 Valkey 或 Redis 版本。
     *
     * @param <V> 值类型
     * @param object 期望的当前值（可为 null 表示键不存在）
     * @return 需继续调用 {@code set()} 的条件构建器
     */
    static <V> CompareAndSetStep<V> expected(V object) {
        return new CompareAndSetParams<>(ConditionType.EXPECTED, object);
    }

    /**
     * 创建「当前值不等于指定值」时成功的比较条件。
     * <p>
     * 兼容任意 Valkey 或 Redis 版本。
     *
     * @param <V> 值类型
     * @param object 不期望的当前值
     * @return 需继续调用 {@code set()} 的条件构建器
     */
    static <V> CompareAndSetStep<V> unexpected(V object) {
        return new CompareAndSetParams<>(ConditionType.UNEXPECTED, object);
    }

    /**
     * 创建「当前值摘要等于期望摘要」时成功的比较条件。
     * <p>
     * 使用 SET IFDEQ 命令，需 Redis 8.4+ 或兼容的 Valkey 版本；
     * 摘要可通过 DIGEST 命令获取。
     *
     * @param <V> 值类型
     * @param value 期望的哈希摘要（DIGEST 命令返回的十六进制字符串）
     * @return 需继续调用 {@code set()} 的条件构建器
     */
    static <V> CompareAndSetStep<V> expectedDigest(String value) {
        Objects.requireNonNull(value, "Digest value can't be null");
        return new CompareAndSetParams<V>(ConditionType.EXPECTED_DIGEST, value);
    }

    /**
     * 创建「当前值摘要不等于指定摘要」时成功的比较条件。
     * <p>
     * 使用 SET IFDNE 命令，需 Redis 8.4+ 或兼容的 Valkey 版本；
     * 摘要可通过 DIGEST 命令获取。
     *
     * @param <V> 值类型
     * @param value 不期望的哈希摘要（DIGEST 命令返回的十六进制字符串）
     * @return 需继续调用 {@code set()} 的条件构建器
     */
    static <V> CompareAndSetStep<V> unexpectedDigest(String value) {
        Objects.requireNonNull(value, "Digest value can't be null");
        return new CompareAndSetParams<V>(ConditionType.UNEXPECTED_DIGEST, value);
    }

    /**
     * 设置键的生存时间（TTL）。
     * 可选配置，可与 set 操作组合使用。
     *
     * @param duration 生存时长
     * @return 当前实例，支持链式调用
     */
    CompareAndSetArgs<V> timeToLive(Duration duration);

    /**
     * 设置键的绝对过期时间点。
     * 可选配置，可与 set 操作组合使用。
     *
     * @param time 过期时刻
     * @return 当前实例，支持链式调用
     */
    CompareAndSetArgs<V> expireAt(Instant time);

}