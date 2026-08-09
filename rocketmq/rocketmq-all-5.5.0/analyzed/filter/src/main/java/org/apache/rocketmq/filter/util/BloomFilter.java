/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.filter.util;

import com.google.common.hash.Hashing;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 布隆过滤器简化实现：基于 MurmurHash3 计算 k 个位位置，支持生成 {@link BloomFilterData}。
 * <p>参数 f 为百分误差率，n 为期望映射元素数量。</p>
 */
public class BloomFilter {

    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    // f 表示百分误差率，例如 10 即 10%
    private int f = 10;
    private int n = 128;

    // k：哈希函数个数，由公式计算
    private int k;
    // bit count, by calculation.
    private int m;

    /**
     * 按误差率与期望元素数创建过滤器。
     *
     * @param f 百分误差率
     * @param n 期望映射元素数量
     */
    public static BloomFilter createByFn(int f, int n) {
        return new BloomFilter(f, n);
    }

    /**
     * 私有构造：校验参数并计算 k、m。
     *
     * @param f error rate
     * @param n num will mapping to bit
     */
    private BloomFilter(int f, int n) {
        if (f < 1 || f >= 100) {
            throw new IllegalArgumentException("f must be greater or equal than 1 and less than 100");
        }
        if (n < 1) {
            throw new IllegalArgumentException("n must be greater than 0");
        }

        this.f = f;
        this.n = n;

        // 依据误差率推导 k、m，详见类注释中的数学关系
        // f = (1 - p)^k = e^(kln(1-p))
        // when p = 0.5, k = ln2 * (m/n), f = (1/2)^k = (0.618)^(m/n)
        double errorRate = f / 100.0;
        this.k = (int) Math.ceil(logMN(0.5, errorRate));

        if (this.k < 1) {
            throw new IllegalArgumentException("Hash function num is less than 1, maybe you should change the value of error rate or bit num!");
        }

        // m >= n*log2(1/f)*log2(e)
        this.m = (int) Math.ceil(this.n * logMN(2, 1 / errorRate) * logMN(2, Math.E));
        // m%8 = 0
        this.m = (int) (Byte.SIZE * Math.ceil(this.m / (Byte.SIZE * 1.0)));
    }

    /**
     * 计算字符串对应的 k 个位下标（双哈希法，参见 Kirsch & Mitzenmacher 论文）。
     */
    public int[] calcBitPositions(String str) {
        int[] bitPositions = new int[this.k];

        long hash64 = Hashing.murmur3_128().hashString(str, UTF_8).asLong();

        int hash1 = (int) hash64;
        int hash2 = (int) (hash64 >>> 32);

        for (int i = 1; i <= this.k; i++) {
            int combinedHash = hash1 + (i * hash2);
            // 合并哈希为负时按位取反，保证模运算为正
            if (combinedHash < 0) {
                combinedHash = ~combinedHash;
            }
            bitPositions[i - 1] = combinedHash % this.m;
        }

        return bitPositions;
    }

    /** 生成包含位下标与总位数的 {@link BloomFilterData}。 */
    public BloomFilterData generate(String str) {
        int[] bitPositions = calcBitPositions(str);

        return new BloomFilterData(bitPositions, this.m);
    }

    /**
     * Calculate bit positions of {@code str}, then set the related {@code bits} positions to 1.
     */
    public void hashTo(String str, BitsArray bits) {
        hashTo(calcBitPositions(str), bits);
    }

    /**
     * Set the related {@code bits} positions to 1.
     */
    public void hashTo(int[] bitPositions, BitsArray bits) {
        check(bits);

        for (int i : bitPositions) {
            bits.setBit(i, true);
        }
    }

    /**
     * Extra check:
     * <li>1. check {@code filterData} belong to this bloom filter.</li>
     * <p>
     * Then set the related {@code bits} positions to 1.
     * </p>
     */
    public void hashTo(BloomFilterData filterData, BitsArray bits) {
        if (!isValid(filterData)) {
            throw new IllegalArgumentException(
                String.format("Bloom filter data may not belong to this filter! %s, %s",
                    filterData, this)
            );
        }
        hashTo(filterData.getBitPos(), bits);
    }

    /**
     * Calculate bit positions of {@code str}, then check all the related {@code bits} positions is 1.
     *
     * @return true: all the related {@code bits} positions is 1
     */
    public boolean isHit(String str, BitsArray bits) {
        return isHit(calcBitPositions(str), bits);
    }

    /**
     * Check all the related {@code bits} positions is 1.
     *
     * @return true: all the related {@code bits} positions is 1
     */
    public boolean isHit(int[] bitPositions, BitsArray bits) {
        check(bits);
        boolean ret = bits.getBit(bitPositions[0]);
        for (int i = 1; i < bitPositions.length; i++) {
            ret &= bits.getBit(bitPositions[i]);
        }
        return ret;
    }

    /**
     * Check all the related {@code bits} positions is 1.
     *
     * @return true: all the related {@code bits} positions is 1
     */
    public boolean isHit(BloomFilterData filterData, BitsArray bits) {
        if (!isValid(filterData)) {
            throw new IllegalArgumentException(
                String.format("Bloom filter data may not belong to this filter! %s, %s",
                    filterData, this)
            );
        }
        return isHit(filterData.getBitPos(), bits);
    }

    /**
     * 检查是否所有位均已置 1（用于判断是否可能已被占用）。
     *
     * @return 全部位为 1 时返回 true
     */
    public boolean checkFalseHit(int[] bitPositions, BitsArray bits) {
        for (int j = 0; j < bitPositions.length; j++) {
            int pos = bitPositions[j];

            // check position of bits has been set.
            // that mean no one occupy the position.
            if (!bits.getBit(pos)) {
                return false;
            }
        }

        return true;
    }

    protected void check(BitsArray bits) {
        if (bits.bitLength() != this.m) {
            throw new IllegalArgumentException(
                String.format("Length(%d) of bits in BitsArray is not equal to %d!", bits.bitLength(), this.m)
            );
        }
    }

    /**
     * Check {@code BloomFilterData} is valid, and belong to this bloom filter.
     * <li>1. not null</li>
     * <li>2. {@link org.apache.rocketmq.filter.util.BloomFilterData#getBitNum} must be equal to {@code m} </li>
     * <li>3. {@link org.apache.rocketmq.filter.util.BloomFilterData#getBitPos} is not null</li>
     * <li>4. {@link org.apache.rocketmq.filter.util.BloomFilterData#getBitPos}'s length is equal to {@code k}</li>
     */
    public boolean isValid(BloomFilterData filterData) {
        if (filterData == null
            || filterData.getBitNum() != this.m
            || filterData.getBitPos() == null
            || filterData.getBitPos().length != this.k) {
            return false;
        }

        return true;
    }

    /**
     * @return 误判率百分比 f
     */
    public int getF() {
        return f;
    }

    /**
     * @return 期望元素数 n
     */
    public int getN() {
        return n;
    }

    /**
     * @return 哈希函数个数 k
     */
    public int getK() {
        return k;
    }

    /**
     * @return 位数组总长度 m
     */
    public int getM() {
        return m;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BloomFilter))
            return false;

        BloomFilter that = (BloomFilter) o;

        if (f != that.f)
            return false;
        if (k != that.k)
            return false;
        if (m != that.m)
            return false;
        if (n != that.n)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = f;
        result = 31 * result + n;
        result = 31 * result + k;
        result = 31 * result + m;
        return result;
    }

    @Override
    public String toString() {
        return String.format("f: %d, n: %d, k: %d, m: %d", f, n, k, m);
    }

    protected double logMN(double m, double n) {
        return Math.log(n) / Math.log(m);
    }
}
