/*
 * Copyright 2016 The Netty Project
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

/**
 * 常数时间比较工具，避免通过执行时间泄露相等性信息。
 * <p>方法返回 {@code int}（0 或 1）而非 {@code boolean}，便于用位与串联多个常数时间比较。</p>
 */
public final class ConstantTimeUtils {
    private ConstantTimeUtils() { }

    /**
     * Compare two {@code int}s without leaking timing information.
     * <p>
     * The {@code int} return type is intentional and is designed to allow cascading of constant time operations:
     * <pre>
     *     int l1 = 1;
     *     int l2 = 1;
     *     int l3 = 1;
     *     int l4 = 500;
     *     boolean equals = (equalsConstantTime(l1, l2) & equalsConstantTime(l3, l4)) != 0;
     * </pre>
     * <p>通过位运算扩散异或结果，使比较耗时与数值差异无关。</p>
     * @param x the first value.
     * @param y the second value.
     * @return {@code 0} if not equal. {@code 1} if equal.
     */
    public static int equalsConstantTime(int x, int y) {
        int z = ~(x ^ y);
        z &= z >> 16;
        z &= z >> 8;
        z &= z >> 4;
        z &= z >> 2;
        z &= z >> 1;
        return z & 1;
    }

    /**
     * Compare two {@code longs}s without leaking timing information.
     * <p>
     * The {@code int} return type is intentional and is designed to allow cascading of constant time operations:
     * <pre>
     *     long l1 = 1;
     *     long l2 = 1;
     *     long l3 = 1;
     *     long l4 = 500;
     *     boolean equals = (equalsConstantTime(l1, l2) & equalsConstantTime(l3, l4)) != 0;
     * </pre>
     * <p>对 64 位值同样采用逐级掩码归约至最低位。</p>
     * @param x the first value.
     * @param y the second value.
     * @return {@code 0} if not equal. {@code 1} if equal.
     */
    public static int equalsConstantTime(long x, long y) {
        long z = ~(x ^ y);
        z &= z >> 32;
        z &= z >> 16;
        z &= z >> 8;
        z &= z >> 4;
        z &= z >> 2;
        z &= z >> 1;
        return (int) (z & 1);
    }

    /**
     * Compare two {@code byte} arrays for equality without leaking timing information.
     * For performance reasons no bounds checking on the parameters is performed.
     * <p>
     * The {@code int} return type is intentional and is designed to allow cascading of constant time operations:
     * <pre>
     *     byte[] s1 = new {1, 2, 3};
     *     byte[] s2 = new {1, 2, 3};
     *     byte[] s3 = new {1, 2, 3};
     *     byte[] s4 = new {4, 5, 6};
     *     boolean equals = (equalsConstantTime(s1, 0, s2, 0, s1.length) &
     *                       equalsConstantTime(s3, 0, s4, 0, s3.length)) != 0;
     * </pre>
     * <p>逐字节异或累加至 {@code b}，再与 0 做常数时间比较；调用方须保证索引合法。</p>
     * @param bytes1 the first byte array.
     * @param startPos1 the position (inclusive) to start comparing in {@code bytes1}.
     * @param bytes2 the second byte array.
     * @param startPos2 the position (inclusive) to start comparing in {@code bytes2}.
     * @param length the amount of bytes to compare. This is assumed to be validated as not going out of bounds
     * by the caller.
     * @return {@code 0} if not equal. {@code 1} if equal.
     */
    public static int equalsConstantTime(byte[] bytes1, int startPos1,
                                         byte[] bytes2, int startPos2, int length) {
        // 基准测试表明用 int 累加异或结果比 byte/long 更快
        // Benchmarking demonstrates that using an int to accumulate is faster than other data types.
        int b = 0;
        final int end = startPos1 + length;
        for (; startPos1 < end; ++startPos1, ++startPos2) {
            b |= bytes1[startPos1] ^ bytes2[startPos2];
        }
        return equalsConstantTime(b, 0);
    }

    /**
     * Compare two {@link CharSequence} objects without leaking timing information.
     * <p>
     * The {@code int} return type is intentional and is designed to allow cascading of constant time operations:
     * <pre>
     *     String s1 = "foo";
     *     String s2 = "foo";
     *     String s3 = "foo";
     *     String s4 = "goo";
     *     boolean equals = (equalsConstantTime(s1, s2) & equalsConstantTime(s3, s4)) != 0;
     * </pre>
     * <p>长度不等时立即返回 0；否则逐字符异或累加后常数时间判等。</p>
     * @param s1 the first value.
     * @param s2 the second value.
     * @return {@code 0} if not equal. {@code 1} if equal.
     */
    public static int equalsConstantTime(CharSequence s1, CharSequence s2) {
        if (s1.length() != s2.length()) {
            return 0;
        }

        // Benchmarking demonstrates that using an int to accumulate is faster than other data types.
        int c = 0;
        for (int i = 0; i < s1.length(); ++i) {
            c |= s1.charAt(i) ^ s2.charAt(i);
        }
        return equalsConstantTime(c, 0);
    }
}
