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
 * 按需增长的 Redis 位图 {@link RBitSet} 同步 API；封装 GETBIT/SETBIT、BITCOUNT、BITOP 及位域读写。
 *
 * @author Nikita Koksharov
 *
 */
public interface RBitSet extends RExpirable, RBitSetAsync {

    /**
     * 读取指定位域的有符号整数；
     * 参数为 {@code offset} 与 {@code size}。
     *
     * @param size 有符号数位宽（最多 64 位）
     * @param offset 有符号数偏移
     * @return 有符号整数
     */
    long getSigned(int size, long offset);

    /**
     * 写入指定位域的有符号整数并返回旧值；
     * 在指定 {@code offset} 处写入 {@code value}。
     *
     * @param size 有符号数位宽（最多 64 位）
     * @param offset 有符号数偏移
     * @param value 有符号数值
     * @return 旧有符号整数
     */
    long setSigned(int size, long offset, long value);

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
    long incrementAndGetSigned(int size, long offset, long increment);

    /**
     * 读取指定位域的无符号整数；
     * 参数为 {@code offset} 与 {@code size}。
     *
     * @param size 无符号数位宽（最多 64 位）
     * @param offset 无符号数偏移
     * @return 无符号整数
     */
    long getUnsigned(int size, long offset);

    /**
     * 写入指定位域的无符号整数并返回旧值；
     * 在指定 {@code offset} 处写入 {@code value}。
     *
     * @param size 无符号数位宽（最多 64 位）
     * @param offset 无符号数偏移
     * @param value 无符号数值
     * @return 旧无符号整数
     */
    long setUnsigned(int size, long offset, long value);

    /**
     * 将指定位域无符号整数加上
     * 增量 {@code increment}（位宽 {@code size}）
     * 位于 {@code offset}，并返回结果。
     * （见上条）
     *
     * @param size 无符号数位宽（最多 64 位）
     * @param offset 无符号数偏移
     * @param increment 增量
     * @return 结果值
     */
    long incrementAndGetUnsigned(int size, long offset, long increment);

    /**
     * 执行 BITFIELD 多子命令。
     * and returns result list in the same order.
     * if ReadMode is Slave And Args is only get commands,
     * then BITFIELD_RO command will be executed
     *
     * @param args BITFIELD 参数
     * @return 结果值s
     */
    List<Long> bitField(BitFieldArgs args);

    /**
     * 读取 {@code offset} 处的字节值。
     *
     * @param offset - offset of number
     * @return number
     */
    byte getByte(long offset);

    /**
     * 写入 {@code offset} 处字节值并返回旧值；
     * 在指定 {@code offset} 处写入 {@code value}。
     *
     * @param offset - offset of number
     * @param value - value of number
     * @return previous value of number
     */
    byte setByte(long offset, byte value);

    /**
     * 在 {@code offset} 处将字节值加上 {@code increment} 并返回结果。
     * （见上条）
     *
     * @param offset - offset of number
     * @param increment 增量
     * @return 结果值
     */
    byte incrementAndGetByte(long offset, byte increment);

    /**
     * 读取 {@code offset} 处的 short 值。
     *
     * @param offset - offset of number
     * @return number
     */
    short getShort(long offset);

    /**
     * 写入 {@code offset} 处 short 值并返回旧值；
     * 在指定 {@code offset} 处写入 {@code value}。
     *
     * @param offset - offset of number
     * @param value - value of number
     * @return previous value of number
     */
    short setShort(long offset, short value);

    /**
     * 在 {@code offset} 处将 short 值加上 {@code increment} 并返回结果。
     * （见上条）
     *
     * @param offset - offset of number
     * @param increment 增量
     * @return 结果值
     */
    short incrementAndGetShort(long offset, short increment);

    /**
     * 读取 {@code offset} 处的 int 值。
     *
     * @param offset - offset of number
     * @return number
     */
    int getInteger(long offset);

    /**
     * 写入 {@code offset} 处 int 值并返回旧值；
     * 在指定 {@code offset} 处写入 {@code value}。
     *
     * @param offset - offset of number
     * @param value - value of number
     * @return previous value of number
     */
    int setInteger(long offset, int value);

    /**
     * 在 {@code offset} 处将 int 值加上 {@code increment} 并返回结果。
     * （见上条）
     *
     * @param offset - offset of number
     * @param increment 增量
     * @return 结果值
     */
    int incrementAndGetInteger(long offset, int increment);

    /**
     * 读取 {@code offset} 处的 long 值。
     *
     * @param offset - offset of number
     * @return number
     */
    long getLong(long offset);

    /**
     * 写入 {@code offset} 处 long 值并返回旧值；
     * 在指定 {@code offset} 处写入 {@code value}。
     *
     * @param offset - offset of number
     * @param value - value of number
     * @return previous value of number
     */
    long setLong(long offset, long value);

    /**
     * 在 {@code offset} 处将 long 值加上 {@code increment} 并返回结果。
     * （见上条）
     *
     * @param offset - offset of number
     * @param increment 增量
     * @return 结果值
     */
    long incrementAndGetLong(long offset, long increment);

    /**
     * 返回"逻辑长度"= 最高置 1 位索引加一。
     * 若无任何置 1 位则返回 0。
     * 
     * @return "logical size" = index of highest set bit plus one
     */
    long length();

    /**
     * 将 [{@code fromIndex}, {@code toIndex}) 范围内的位设为 {@code value}。
     * 
     * @param fromIndex inclusive
     * @param toIndex exclusive
     * @param value true = 1, false = 0
     * 
     */
    void set(long fromIndex, long toIndex, boolean value);

    /**
     * 将全部位清零。 from <code>fromIndex</code> (inclusive) to <code>toIndex</code> (exclusive)
     * 
     * @param fromIndex inclusive
     * @param toIndex exclusive
     * 
     */
    void clear(long fromIndex, long toIndex);

    /**
     * 将源 BitSet 的位状态复制到本对象。
     * 
     * @param bs 源 BitSet
     */
    void set(BitSet bs);

    /**
     * 将 indexArray 中索引对应的位设为 {@code value}。
     *
     * @param indexArray The index array of bits that needs to be set to <code>value</code>
     * @param value true = 1, false = 0
     */
    void set(long[] indexArray, boolean value);

    /**
     * 对全部位执行 NOT。
     *
     * @return length in bytes of the destination key
     */
    long not();

    /**
     * 将 [{@code fromIndex}, {@code toIndex}) 范围内的位全部置 1。
     * 
     * @param fromIndex inclusive
     * @param toIndex exclusive
     * 
     */
    void set(long fromIndex, long toIndex);

    /**
     * 返回置 1 的位数。
     * 
     * @return number of set bits.
     */
    long size();

    /**
     * 位为 1 返回 {@code true}，否则 {@code false}。
     * 
     * @param bitIndex - index of bit
     * @return 位为 1 则 {@code true}，否则 {@code false}
     */
    boolean get(long bitIndex);
    
    /**
     * 返回布尔数组，每个元素对应输入参数的位查询结果。
     *
     * @param bitIndexes indexes of bit
     * @return Returns a boolean array where each element of the array corresponds to the query result of the input parameters.
     */
    boolean[] get(long... bitIndexes);

    /**
     * Set bit to one at specified bitIndex
     * 
     * @param bitIndex - index of bit
     * @return <code>true</code> - if previous value was true, 
     * {@code false} — 若旧值为 false
     */
    boolean set(long bitIndex);

    /**
     * Set bit to <code>value</code> at specified <code>bitIndex</code>
     * 
     * @param bitIndex - index of bit
     * @param value true = 1, false = 0
     * @return <code>true</code> - if previous value was true,
     * {@code false} — 若旧值为 false
     *
    */
    boolean set(long bitIndex, boolean value);

    byte[] toByteArray();

    /**
     * 返回置 1 的位数。
     * 
     * @return number of bits
     */
    long cardinality();

    /**
     * Set bit to zero at specified <code>bitIndex</code>
     * 
     * @param bitIndex - index of bit
     * @return <code>true</code> - if previous value was true, 
     * {@code false} — 若旧值为 false
     */
    boolean clear(long bitIndex);

    /**
     * 将全部位清零。
     */
    void clear();

    BitSet asBitSet();

    /**
     * 对本对象与指定位集执行 OR。
     * Stores result into this object.
     * 
     * @param bitSetNames - name of stored bitsets
     * @return length in bytes of the destination key
     */
    long or(String... bitSetNames);

    /**
     * 对本对象与指定位集执行 AND。
     * Stores result into this object.
     * 
     * @param bitSetNames - name of stored bitsets
     * @return length in bytes of the destination key
     */
    long and(String... bitSetNames);

    /**
     * 对本对象与指定位集执行 XOR。
     * Stores result into this object.
     * 
     * @param bitSetNames - name of stored bitsets
     * @return length in bytes of the destination key
     */
    long xor(String... bitSetNames);

    /**
     * 对本对象与指定位集执行 DIFF。
     * Sets bits that are set in this object but not in any of the other bitsets.
     * Stores result into this object.
     *
     * @param bitSetNames name of stored bitsets
     * @return length in bytes of the destination key
     */
    long diff(String... bitSetNames);

    /**
     * 对本对象与指定位集执行 DIFF1。
     * Sets bits that are set in one or more of the other bitsets but not in this object.
     * Stores result into this object.
     *
     * @param bitSetNames name of stored bitsets
     * @return length in bytes of the destination key
     */
    long diffInverse(String... bitSetNames);

    /**
     * 对本对象与指定位集执行 ANDOR。
     * Sets bits that are set in this object AND also in one or more of the other bitsets.
     * Stores result into this object.
     *
     * @param bitSetNames name of stored bitsets
     * @return length in bytes of the destination key
     */
    long andOr(String... bitSetNames);

    /**
     * 对本对象与指定位集执行 ONE。
     * Sets bits that are set in exactly one of the provided bitsets.
     * Stores result into this object.
     *
     * @param bitSetNames name of stored bitsets
     * @return length in bytes of the destination key
     */
    long setExclusive(String... bitSetNames);

}
