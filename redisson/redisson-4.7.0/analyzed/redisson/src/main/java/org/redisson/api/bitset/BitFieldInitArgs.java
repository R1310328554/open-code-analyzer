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
package org.redisson.api.bitset;

/**
 * BITFIELD 命令的初始化参数接口，用于链式构建子命令序列。
 *
 * @author Su Ko
 *
 */
public interface BitFieldInitArgs {

    /**
     * 添加 OVERFLOW 子命令；
     * 为后续 SET/INCRBY 操作设置溢出行为，直至下一次 OVERFLOW。
     *
     * @param overflow 溢出处理方式
     * @return 参数对象
     */
    BitFieldInitArgs overflow(BitFieldOverflow overflow);

    /**
     * 添加 GET 子命令，读取有符号值。
     * 返回指定编码/偏移处存储的值。
     *
     * @param size 有符号数位数，最多 64 位
     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移
     * @return 参数对象
     */
    BitFieldArgs getSigned(int size, BitOffset offset);

    /**
     * 添加 GET 子命令，读取无符号值。
     * 返回指定编码/偏移处存储的值。
     *
     * @param size 无符号数位数，最多 63 位
     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移
     * @return 参数对象
     */
    BitFieldArgs getUnsigned(int size, BitOffset offset);

    /**
     * 添加 SET 子命令，写入有符号值并返回旧值；
     * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。
     *
     * @param size 有符号数位数，最多 64 位
     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移
     * @param value 要写入的值
     * @return 参数对象
     */
    BitFieldArgs setSigned(int size, BitOffset offset, long value);

    /**
     * 添加 SET 子命令，写入无符号值并返回旧值；
     * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。
     *
     * @param size 无符号数位数，最多 63 位
     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移
     * @param value 要写入的值
     * @return 参数对象
     */
    BitFieldArgs setUnsigned(int size, BitOffset offset, long value);

    /**
     * 添加 INCRBY 子命令，对有符号值按给定量递增并返回新值；
     * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。
     *
     * @param size 有符号数位数，最多 64 位
     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移
     * @param increment 递增量
     * @return 参数对象
     */
    BitFieldArgs incrementSignedBy(int size, BitOffset offset, long increment);

    /**
     * 添加 INCRBY 子命令，对无符号值按给定量递增并返回新值；
     * 若 OVERFLOW 为 FAIL，溢出时可能返回 null。
     *
     * @param size 无符号数位数，最多 63 位
     * @param offset 由 {@link BitOffset#bit(long)} 或 {@link BitOffset#index(long)} 创建的偏移
     * @param increment 递增量
     * @return 参数对象
     */
    BitFieldArgs incrementUnsignedBy(int size, BitOffset offset, long increment);
}
