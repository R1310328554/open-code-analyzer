/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.util.internal;

/**
 * Math utility methods.
 *
 * <p>数学工具方法：2 的幂、边界检查及已弃用的比较封装。</p>
 */
public final class MathUtil {

    private MathUtil() {
    }

    /**
     * Fast method of finding the next power of 2 greater than or equal to the supplied value.
     *
     * <p>If the value is {@code <= 0} then 1 will be returned.
     * This method is not suitable for {@link Integer#MIN_VALUE} or numbers greater than 2^30.
     *
     * @param value from which to search for next power of 2
     * @return The next power of 2 or the value itself if it is a power of 2
     *
     * <p>快速求不小于 value 的最小 2 的幂；value≤0 时行为由调用方断言约束。</p>
     */
    public static int findNextPositivePowerOfTwo(final int value) {
        assert value > Integer.MIN_VALUE && value < 0x40000000;
        return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }

    /**
     * Fast method of finding the next power of 2 greater than or equal to the supplied value.
     * <p>This method will do runtime bounds checking and call {@link #findNextPositivePowerOfTwo(int)} if within a
     * valid range.
     * @param value from which to search for next power of 2
     * @return The next power of 2 or the value itself if it is a power of 2.
     * <p>Special cases for return values are as follows:
     * <ul>
     *     <li>{@code <= 0} -> 1</li>
     *     <li>{@code >= 2^30} -> 2^30</li>
     * </ul>
     *
     * <p>带运行时边界的 2 的幂查找，避免非法输入。</p>
     */
    public static int safeFindNextPositivePowerOfTwo(final int value) {
        return value <= 0 ? 1 : value >= 0x40000000 ? 0x40000000 : findNextPositivePowerOfTwo(value);
    }

    /**
     * Determine if the requested {@code index} and {@code length} will fit within {@code capacity}.
     * @param index The starting index.
     * @param length The length which will be utilized (starting from {@code index}).
     * @param capacity The capacity that {@code index + length} is allowed to be within.
     * @return {@code false} if the requested {@code index} and {@code length} will fit within {@code capacity}.
     * {@code true} if this would result in an index out of bounds exception.
     *
     * <p>单次位运算检测 index/length/capacity 是否越界（含溢出）。</p>
     */
    public static boolean isOutOfBounds(int index, int length, int capacity) {
        return (index | length | capacity | index + length) < 0 || index + length > capacity;
    }

    /**
     * @deprecated not used anymore. User Integer.compare() instead. For removal.
     * Compares two {@code int} values.
     *
     * @param  x the first {@code int} to compare
     * @param  y the second {@code int} to compare
     * @return the value {@code 0} if {@code x == y};
     *         {@code -1} if {@code x < y}; and
     *         {@code 1} if {@code x > y}
     *
     * <p>已弃用：请直接使用 {@link Integer#compare(int, int)}。</p>
     */
    @Deprecated
    public static int compare(final int x, final int y) {
        // do not subtract for comparison, it could overflow
        return Integer.compare(x, y);
    }

    /**
     * @deprecated not used anymore. User Long.compare() instead. For removal.
     * Compare two {@code long} values.
     * @param x the first {@code long} to compare.
     * @param y the second {@code long} to compare.
     * @return
     * <ul>
     * <li>0 if {@code x == y}</li>
     * <li>{@code > 0} if {@code x > y}</li>
     * <li>{@code < 0} if {@code x < y}</li>
     * </ul>
     *
     * <p>已弃用：请直接使用 {@link Long#compare(long, long)}。</p>
     */
    @Deprecated
    public static int compare(long x, long y) {
        return Long.compare(x, y);
    }

}
