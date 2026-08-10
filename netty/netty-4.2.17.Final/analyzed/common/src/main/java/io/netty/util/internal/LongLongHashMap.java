/*
 * Copyright 2025 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.internal;

import java.util.Arrays;

/**
 * 开放寻址的 long→long 哈希表，键 0 单独存放；用于内部高性能映射场景。
 */
public final class LongLongHashMap {
    /** 掩码模板：保证索引为偶数（键值成对存储）。 */
    private static final int MASK_TEMPLATE = ~1;
    /** 数组长度减 1 且清除最低位，用作环形索引掩码。 */
    private int mask;
    /** 扁平数组：[k0,v0,k1,v1,...]，0 表示空槽。 */
    private long[] array;
    /** 单次查找最大探测步数（与 log(容量) 相关）。 */
    private int maxProbe;
    /** 键 0 对应的值（0 不进入主表）。 */
    private long zeroVal;
    /** 缺失键或空槽时的默认返回值。 */
    private final long emptyVal;

    public LongLongHashMap(long emptyVal) {
        this.emptyVal = emptyVal;
        zeroVal = emptyVal;
        int initialSize = 32;
        array = new long[initialSize];
        mask = initialSize - 1;
        computeMaskAndProbe();
    }

    /** 拷贝构造：复制掩码、数组与零键值。 */
    public LongLongHashMap(LongLongHashMap other) {
        this.mask = other.mask;
        this.array = Arrays.copyOf(other.array, other.array.length);
        this.maxProbe = other.maxProbe;
        this.zeroVal = other.zeroVal;
        this.emptyVal = other.emptyVal;
    }

    /**
     * 插入或更新键值，返回旧 value；新键返回 {@link #emptyVal}。
     */
    public long put(long key, long value) {
        if (key == 0) {
            long prev = zeroVal;
            zeroVal = value;
            return prev;
        }

        for (;;) {
            int index = index(key);
            for (int i = 0; i < maxProbe; i++) {
                long existing = array[index];
                if (existing == key || existing == 0) {
                    long prev = existing == 0? emptyVal : array[index + 1];
                    array[index] = key;
                    array[index + 1] = value;
                    for (; i < maxProbe; i++) { // Nerf any existing misplaced entries.
                        // 清除因冲突而误放在其他槽位的同键条目
                        index = index + 2 & mask;
                        if (array[index] == key) {
                            array[index] = 0;
                            prev = array[index + 1];
                            break;
                        }
                    }
                    return prev;
                }
                index = index + 2 & mask;
            }
            expand(); // Grow array and re-hash.
        }
    }

    /** 删除键；键 0 重置为 {@link #emptyVal}。 */
    public void remove(long key) {
        if (key == 0) {
            zeroVal = emptyVal;
            return;
        }
        int index = index(key);
        for (int i = 0; i < maxProbe; i++) {
            long existing = array[index];
            if (existing == key) {
                array[index] = 0;
                break;
            }
            index = index + 2 & mask;
        }
    }

    /** 查询键对应值；不存在返回 {@link #emptyVal}。 */
    public long get(long key) {
        if (key == 0) {
            return zeroVal;
        }
        int index = index(key);
        for (int i = 0; i < maxProbe; i++) {
            long existing = array[index];
            if (existing == key) {
                return array[index + 1];
            }
            index = index + 2 & mask;
        }
        return emptyVal;
    }

    /** MurmurHash64 混合后取掩码，得到键槽起始偶数索引。 */
    private int index(long key) {
        // Hash with murmur64, and mask.
        key ^= key >>> 33;
        key *= 0xff51afd7ed558ccdL;
        key ^= key >>> 33;
        key *= 0xc4ceb9fe1a85ec53L;
        key ^= key >>> 33;
        return (int) key & mask;
    }

    /** 容量翻倍并 rehash 所有非空条目。 */
    private void expand() {
        long[] prev = array;
        array = new long[prev.length * 2];
        computeMaskAndProbe();
        for (int i = 0; i < prev.length; i += 2) {
            long key = prev[i];
            if (key != 0) {
                long val = prev[i + 1];
                put(key, val);
            }
        }
    }

    /** 根据数组长度更新 mask 与 maxProbe。 */
    private void computeMaskAndProbe() {
        int length = array.length;
        mask = length - 1 & MASK_TEMPLATE;
        maxProbe = (int) Math.log(length);
    }
}
