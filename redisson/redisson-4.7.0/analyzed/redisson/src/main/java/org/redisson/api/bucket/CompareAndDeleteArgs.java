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

import java.util.Objects;

/**
 * {@link org.redisson.api.RBucket#compareAndDelete(CompareAndDeleteArgs)} 的参数对象；
 * 定义按当前值比较条件删除 Bucket 的规则。
 * <p>
 * 支持值相等/不等及摘要（digest）比较等多种模式。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 值类型
 */
public interface CompareAndDeleteArgs<V> {

    /**
     * 当存储值与指定对象不相等时删除 Bucket。
     * 兼容任意 Valkey 或 Redis 版本。
     *
     * @param object 待比较的值
     * @param <V> 值类型
     * @return 参数对象
     */
    static <V> CompareAndDeleteArgs<V> unexpected(V object) {
        return new CompareAndDeleteParams<>(ConditionType.UNEXPECTED, object);
    }

    /**
     * 当存储值与指定对象相等时删除 Bucket。
     * 兼容任意 Valkey 或 Redis 版本。
     *
     * @param object 待比较的值
     * @param <V> 值类型
     * @return 参数对象
     */
    static <V> CompareAndDeleteArgs<V> expected(V object) {
        return new CompareAndDeleteParams<>(ConditionType.EXPECTED, object);
    }

    /**
     * 当存储值的摘要与指定摘要相等时删除 Bucket。
     * 使用 DELEX IFDEQ 命令，需 Valkey 8+ 或 Redis 8.4+。
     *
     * @param value 摘要值（DIGEST 命令返回的十六进制字符串）
     * @param <V> 值类型
     * @return 参数对象
     */
    static <V> CompareAndDeleteArgs<V> expectedDigest(String value) {
        Objects.requireNonNull(value, "Digest value can't be null");
        return new CompareAndDeleteParams<>(ConditionType.EXPECTED_DIGEST, value);
    }

    /**
     * 当存储值的摘要与指定摘要不同时删除 Bucket。
     * 使用 DELEX IFDNE 命令，需 Valkey 8+ 或 Redis 8.4+。
     *
     * @param value 摘要值（DIGEST 命令返回的十六进制字符串）
     * @param <V> 值类型
     * @return 参数对象
     */
    static <V> CompareAndDeleteArgs<V> unexpectedDigest(String value) {
        Objects.requireNonNull(value, "Digest value can't be null");
        return new CompareAndDeleteParams<>(ConditionType.UNEXPECTED_DIGEST, value);
    }

}