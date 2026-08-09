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

import java.util.BitSet;
import java.util.List;
import org.redisson.api.bitset.BitFieldArgs;

/**
 * 按需增长的 Redis 位图 {@link RBitSet} 同步 API；封装 GETBIT/SETBIT、BITCOUNT、BITOP 及位域读写。 Asynchronous interface.
 *
 * @author Nikita Koksharov
 *
 */
public interface RBitSetAsync extends RExpirableAsync {

    /**
     * 读取指定位域的有符号整数；
     * 参数为 {@code offset} 与 {@code size}。
     *
     * @param size 有符号数位宽（最多 64 位）
     * @param offset 有符号数偏移
     * @return 有符号整数
     */
    RFuture<Long> getSignedAsync(int size, long offset);

    /**
     * 写入指定位域的有符号整数并返回旧值；
     * 在指定 {@code offset} 处写入 {@code value}。
     *
     * @param size 有符号数位宽（最多 64 位）
     * @param offset 有符号数偏移
     * @param value 有符号数值
     * @return 旧有符号整数
     */
    RFuture<Long> setSignedAsync(int size, long offset, long value);

    /**
     * 将指定位域有符号整数加上
     * 增量 {@code increment}（位宽 {@code size}）
     * 位于 {@code offset}，并返回结果。
     * （见上条）
     *
     * @param size 有符号数位宽（最多 64 位）
     * @param offset 有符号数偏移
     * @param increment 增量
     * @return 结果值
     */
    RFuture<Long> incrementAndGetSignedAsync(int size, long offset, long increment);

    /**
     * 读取指定位域的无符号整数；
     * 参数为 {@code offset} 与 {@code size}。
     *
     * @param size - size of unsigned number up to 63 bits
     * @param offset 无符号数偏移
     * @return 无符号整数
     */
    RFuture<Long> getUnsignedAsync(int size, long offset);

    /**
     * 写入指定位域的无符号整数并返回旧值；
     * 在指定 {@code offset} 处写入 {@code value}。
     *
     * @param size - size of unsigned number up to 63 bits
     * @param offset 无符号数偏移
     * @param value 无符号数值
     * @return 旧无符号整数
     */
    RFuture<Long> setUnsignedAsync(int size, long offset, long value);

    /**
     * 将指定位域无符号整数加上
     * 增量 {@code increment}（位宽 {@code size}）
     * 位于 {@code offset}，并返回结果。
     * （见上条）
     *
     * @param size - size of unsigned number up to 63 bits
     * @param offset 无符号数偏移
     * @param increment 增量
     * @return 结果值
     */
    RFuture<Long> incrementAndGetUnsignedAsync(int size, long offset, long increment);

    /**
     * 执行 BITFIELD 多子命令。
     * and returns result list in the same order.
     * if ReadMode is Slave And Args is only get commands,
     * then BITFIELD_RO command will be executed
     *
     * @param args BITFIELD 参数
     * @return 结果值s
     */
    RFuture<List<Long>> bitFieldAsync(BitFieldArgs args);

    /**
     * 读取 {@code offset} 处的字节值。
     *
     * @param offset - offset of number
     * @return number
     */
    RFuture<Byte> getByteAsync(long offset);

    /**
     * 写入 {@code offset} 处字节值并返回旧值；
     * 在指定 {@code offset} 处写入 {@code value}。
     *
     * @param offset - offset of number
     * @param value - value of number
     * @return previous value of number
     */
    RFuture<Byte> setByteAsync(long offset, byte value);

    /**
     * 在 {@code offset} 处将字节值加上 {@code increment} 并返回结果。
     * （见上条）
     *
     * @param offset - offset of number
     * @param increment 增量
     * @return 结果值
     */
    RFuture<Byte> incrementAndGetByteAsync(long offset, byte increment);

    /**
     * 读取 {@code offset} 处的 short 值。
     *
     * @param offset - offset of number
     * @return number
     */
    RFuture<Short> getShortAsync(long offset);

    /**
     * 写入 {@code offset} 处 short 值并返回旧值；
     * 在指定 {@code offset} 处写入 {@code value}。
     *
     * @param offset - offset of number
     * @param value - value of number
     * @return previous value of number
     */
    RFuture<Short> setShortAsync(long offset, short value);

    /**
     * 在 {@code offset} 处将 short 值加上 {@code increment} 并返回结果。
     * （见上条）
     *
     * @param offset - offset of number
     * @param increment 增量
     * @return 结果值
     */
    RFuture<Short> incrementAndGetShortAsync(long offset, short increment);

    /**
     * 读取 {@code offset} 处的 int 值。
     *
     * @param offset - offset of number
     * @return number
     */
    RFuture<Integer> getIntegerAsync(long offset);

    /**
     * 写入 {@code offset} 处 int 值并返回旧值；
     * 在指定 {@code offset} 处写入 {@code value}。
     *
     * @param offset - offset of number
     * @param value - value of number
     * @return previous value of number
     */
    RFuture<Integer> setIntegerAsync(long offset, int value);

    /**
     * 在 {@code offset} 处将 int 值加上 {@code increment} 并返回结果。
     * （见上条）
     *
     * @param offset - offset of number
     * @param increment 增量
     * @return 结果值
     */
    RFuture<Integer> incrementAndGetIntegerAsync(long offset, int increment);

    /**
     * 读取 {@code offset} 处的 long 值。
     *
     * @param offset - offset of number
     * @return number
     */
    RFuture<Long> getLongAsync(long offset);

    /**
     * 写入 {@code offset} 处 long 值并返回旧值；
     * 在指定 {@code offset} 处写入 {@code value}。
     *
     * @param offset - offset of number
     * @param value - value of number
     * @return previous value of number
     */
    RFuture<Long> setLongAsync(long offset, long value);

    /**
     * 在 {@code offset} 处将 long 值加上 {@code increment} 并返回结果。
     * （见上条）
     *
     * @param offset - offset of number
     * @param increment 增量
     * @return 结果值
     */
    RFuture<Long> incrementAndGetLongAsync(long offset, long increment);

    RFuture<byte[]> toByteArrayAsync();

    /**
     * 返回"逻辑长度"= 最高置 1 位索引加一。
     * 若无任何置 1 位则返回 0。
     *
     * @return "logical size" = index of highest set bit plus one
     */
    RFuture<Long> lengthAsync();

    /**
     * 将 [{@code fromIndex}, {@code toIndex}) 范围内的位设为 {@code value}。
     *
     * @param fromIndex inclusive
     * @param toIndex exclusive
     * @param value true = 1, false = 0
     * @return 无返回值
     *
     */
    RFuture<Void> setAsync(long fromIndex, long toIndex, boolean value);

    /**
     * 将全部位清零。 from <code>fromIndex</code> (inclusive) to <code>toIndex</code> (exclusive)
     *
     * @param fromIndex inclusive
     * @param toIndex exclusive
     * @return 无返回值
     *
     */
    RFuture<Void> clearAsync(long fromIndex, long toIndex);

    /**
     * 将源 BitSet 的位状态复制到本对象。
     *
     * @param bs 源 BitSet
     * @return 无返回值
     */
    RFuture<Void> setAsync(BitSet bs);

    /**
     * 对全部位执行 NOT。
     *
     * @return length in bytes of the destination key
     */
    RFuture<Long> notAsync();

    /**
     * 将 [{@code fromIndex}, {@code toIndex}) 范围内的位全部置 1。
     *
     * @param fromIndex inclusive
     * @param toIndex exclusive
     * @return 无返回值
     */
    RFuture<Void> setAsync(long fromIndex, long toIndex);

    /**
     * 返回置 1 的位数。
     *
     * @return number of set bits.
     */
    RFuture<Long> sizeAsync();

    /**
     * 位为 1 返回 {@code true}，否则 {@code false}。
     *
     * @param bitIndex - index of bit
     * @return 位为 1 则 {@code true}，否则 {@code false}
     */
    RFuture<Boolean> getAsync(long bitIndex);
    
    /**
     * 返回布尔数组，每个元素对应输入参数的位查询结果。
     *
     * @param bitIndexes indexes of bit
     * @return Returns a boolean array where each element of the array corresponds to the query result of the input parameters.
     */
    RFuture<boolean[]> getAsync(long... bitIndexes);

    /**
     * Set bit to one at specified bitIndex
     *
     * @param bitIndex - index of bit
     * @return <code>true</code> - if previous value was true,
     * {@code false} — 若旧值为 false
     */
    RFuture<Boolean> setAsync(long bitIndex);

    /**
     * Set bit to <code>value</code> at specified <code>bitIndex</code>
     *
     * @param bitIndex - index of bit
     * @param value true = 1, false = 0
     * @return <code>true</code> - if previous value was true,
     * {@code false} — 若旧值为 false
     */
    RFuture<Boolean> setAsync(long bitIndex, boolean value);

    /**
     * 将 indexArray 中索引对应的位设为 {@code value}。
     *
     * @param indexArray The index array of bits that needs to be set to <code>value</code>
     * @param value true = 1, false = 0
     */
    RFuture<Void> setAsync(long[] indexArray, boolean value);

    /**
     * 返回置 1 的位数。
     *
     * @return number of bits
     */
    RFuture<Long> cardinalityAsync();

    /**
     * Set bit to zero at specified <code>bitIndex</code>
     *
     * @param bitIndex - index of bit
     * @return <code>true</code> - if previous value was true,
     * {@code false} — 若旧值为 false
     */
    RFuture<Boolean> clearAsync(long bitIndex);

    /**
     * 将全部位清零。
     *
     * @return 无返回值
     */
    RFuture<Void> clearAsync();

    /**
     * 对本对象与指定位集执行 OR。
     * Stores result into this object.
     *
     * @param bitSetNames - name of stored bitsets
     * @return length in bytes of the destination key
     */
    RFuture<Long> orAsync(String... bitSetNames);

    /**
     * 对本对象与指定位集执行 AND。
     * Stores result into this object.
     *
     * @param bitSetNames - name of stored bitsets
     * @return length in bytes of the destination key
     */
    RFuture<Long> andAsync(String... bitSetNames);

    /**
     * 对本对象与指定位集执行 XOR。
     * Stores result into this object.
     *
     * @param bitSetNames - name of stored bitsets
     * @return length in bytes of the destination key
     */
    RFuture<Long> xorAsync(String... bitSetNames);

    /**
     * 对本对象与指定位集执行 DIFF。
     * Sets bits that are set in this object but not in any of the other bitsets.
     * Stores result into this object.
     *
     * @param bitSetNames name of stored bitsets
     * @return length in bytes of the destination key
     */
    RFuture<Long> diffAsync(String... bitSetNames);

    /**
     * 对本对象与指定位集执行 DIFF1。
     * Sets bits that are set in one or more of the other bitsets but not in this object.
     * Stores result into this object.
     *
     * @param bitSetNames name of stored bitsets
     * @return length in bytes of the destination key
     */
    RFuture<Long> diffInverseAsync(String... bitSetNames);

    /**
     * 对本对象与指定位集执行 ANDOR。
     * Sets bits that are set in this object AND also in one or more of the other bitsets.
     * Stores result into this object.
     *
     * @param bitSetNames name of stored bitsets
     * @return length in bytes of the destination key
     */
    RFuture<Long> andOrAsync(String... bitSetNames);

    /**
     * 对本对象与指定位集执行 ONE。
     * Sets bits that are set in exactly one of the provided bitsets.
     * Stores result into this object.
     *
     * @param bitSetNames name of stored bitsets
     * @return length in bytes of the destination key
     */
    RFuture<Long> setExclusiveAsync(String... bitSetNames);

}
