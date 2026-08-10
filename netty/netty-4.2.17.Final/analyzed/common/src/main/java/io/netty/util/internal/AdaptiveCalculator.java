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

import java.util.ArrayList;
import java.util.List;

import static io.netty.util.internal.ObjectUtil.checkPositive;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Calculate sizes in a adaptive way.
 *
 * <p>根据实际使用量自适应调整下次分配尺寸（如 ByteBuf 容量），采用离散 SIZE_TABLE 与迟滞增减。</p>
 */
public final class AdaptiveCalculator {
    /** 记录到更大用量时索引一次跳增步长。 */
    private static final int INDEX_INCREMENT = 4;
    /** 用量回落时索引递减步长（需连续两次确认才减）。 */
    private static final int INDEX_DECREMENT = 1;

    private static final int[] SIZE_TABLE;

    static {
        List<Integer> sizeTable = new ArrayList<Integer>();
        for (int i = 16; i < 512; i += 16) {
            sizeTable.add(i);
        }

        // 512 起按 2 幂扩展 SIZE_TABLE，直至 int 溢出终止
        for (int i = 512; i > 0; i <<= 1) {
            sizeTable.add(i);
        }

        SIZE_TABLE = new int[sizeTable.size()];
        for (int i = 0; i < SIZE_TABLE.length; i ++) {
            SIZE_TABLE[i] = sizeTable.get(i);
        }
    }

    /** 在 SIZE_TABLE 中二分查找不小于 size 的档位索引。 */
    private static int getSizeTableIndex(final int size) {
        for (int low = 0, high = SIZE_TABLE.length - 1;;) {
            if (high < low) {
                return low;
            }
            if (high == low) {
                return high;
            }

            int mid = low + high >>> 1;
            int a = SIZE_TABLE[mid];
            int b = SIZE_TABLE[mid + 1];
            if (size > b) {
                low = mid + 1;
            } else if (size < a) {
                high = mid - 1;
            } else if (size == a) {
                return mid;
            } else {
                return mid + 1;
            }
        }
    }

    private final int minIndex;
    private final int maxIndex;
    private final int minCapacity;
    private final int maxCapacity;
    private int index;
    private int nextSize;
    private boolean decreaseNow;

    public AdaptiveCalculator(int minimum, int initial, int maximum) {
        checkPositive(minimum, "minimum");
        if (initial < minimum) {
            throw new IllegalArgumentException("initial: " + initial);
        }
        if (maximum < initial) {
            throw new IllegalArgumentException("maximum: " + maximum);
        }

        int minIndex = getSizeTableIndex(minimum);
        if (SIZE_TABLE[minIndex] < minimum) {
            this.minIndex = minIndex + 1;
        } else {
            this.minIndex = minIndex;
        }

        int maxIndex = getSizeTableIndex(maximum);
        if (SIZE_TABLE[maxIndex] > maximum) {
            this.maxIndex = maxIndex - 1;
        } else {
            this.maxIndex = maxIndex;
        }

        int initialIndex = getSizeTableIndex(initial);
        if (SIZE_TABLE[initialIndex] > initial) {
            this.index = initialIndex - 1;
        } else {
            this.index = initialIndex;
        }
        this.minCapacity = minimum;
        this.maxCapacity = maximum;
        nextSize = max(SIZE_TABLE[index], minCapacity);
    }

    /** 根据本次实际使用 size 调整 index/nextSize：过大则升档，过小则迟滞降档。 */
    public void record(int size) {
        if (size <= SIZE_TABLE[max(0, index - INDEX_DECREMENT)]) {
            if (decreaseNow) {
                index = max(index - INDEX_DECREMENT, minIndex);
                nextSize = max(SIZE_TABLE[index], minCapacity);
                decreaseNow = false;
            } else {
                decreaseNow = true;
            }
        } else if (size >= nextSize) {
            index = min(index + INDEX_INCREMENT, maxIndex);
            nextSize = min(SIZE_TABLE[index], maxCapacity);
            decreaseNow = false;
        }
    }

    /** 返回建议的下次分配容量（受 min/max 约束）。 */
    public int nextSize() {
        return nextSize;
    }
}
