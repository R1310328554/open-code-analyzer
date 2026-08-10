/*
 * Copyright 2013 The Netty Project
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

import static io.netty.util.internal.ObjectUtil.checkPositive;
import static io.netty.util.internal.ObjectUtil.checkNonEmpty;

import java.util.Arrays;

/**
 * 可追加的字符序列，实现 {@link CharSequence} 与 {@link Appendable}。
 * <p>内部以 {@code char[]} 存储，{@code pos} 表示当前有效长度；支持 {@link #reset()} 复用缓冲区而无需重新分配。</p>
 */
public final class AppendableCharSequence implements CharSequence, Appendable {
    /** 底层字符数组。 */
    private char[] chars;
    /** 当前有效字符数（逻辑长度）。 */
    private int pos;

    /**
     * 按指定初始容量分配字符数组。
     *
     * @param length 初始容量，必须为正数
     */
    public AppendableCharSequence(int length) {
        chars = new char[checkPositive(length, "length")];
    }

    /** 由已有字符数组构造，逻辑长度等于数组长度。 */
    private AppendableCharSequence(char[] chars) {
        this.chars = checkNonEmpty(chars, "chars");
        pos = chars.length;
    }

    /**
     * 将逻辑长度截断为 {@code length}，不缩小底层数组。
     *
     * @param length 新长度，须满足 {@code 0 <= length <= pos}
     */
    public void setLength(int length) {
        if (length < 0 || length > pos) {
            throw new IllegalArgumentException("length: " + length + " (length: >= 0, <= " + pos + ')');
        }
        this.pos = length;
    }

    @Override
    public int length() {
        return pos;
    }

    @Override
    public char charAt(int index) {
        if (index > pos) {
            throw new IndexOutOfBoundsException();
        }
        return chars[index];
    }

    /**
     * Access a value in this {@link CharSequence}.
     * This method is considered unsafe as index values are assumed to be legitimate.
     * Only underlying array bounds checking is done.
     * <p>不校验 {@code index} 与 {@code pos} 的关系，仅依赖数组边界；调用方须保证索引合法。</p>
     * @param index The index to access the underlying array at.
     * @return The value at {@code index}.
     */
    public char charAtUnsafe(int index) {
        return chars[index];
    }

    @Override
    public AppendableCharSequence subSequence(int start, int end) {
        if (start == end) {
            // 空子序列须返回 length>0 的内部数组以满足扩容逻辑
            // If start and end index is the same we need to return an empty sequence to conform to the interface.
            // As our expanding logic depends on the fact that we have a char[] with length > 0 we need to construct
            // an instance for which this is true.
            return new AppendableCharSequence(Math.min(16, chars.length));
        }
        return new AppendableCharSequence(Arrays.copyOfRange(chars, start, end));
    }

    @Override
    public AppendableCharSequence append(char c) {
        if (pos == chars.length) {
            // 容量不足时翻倍扩容
            char[] old = chars;
            chars = new char[old.length << 1];
            System.arraycopy(old, 0, chars, 0, old.length);
        }
        chars[pos++] = c;
        return this;
    }

    @Override
    public AppendableCharSequence append(CharSequence csq) {
        return append(csq, 0, csq.length());
    }

    @Override
    public AppendableCharSequence append(CharSequence csq, int start, int end) {
        if (csq.length() < end) {
            throw new IndexOutOfBoundsException("expected: csq.length() >= ("
                    + end + "),but actual is (" + csq.length() + ")");
        }
        int length = end - start;
        if (length > chars.length - pos) {
            chars = expand(chars, pos + length, pos);
        }
        if (csq instanceof AppendableCharSequence) {
            // 同类型序列通过数组拷贝优化
            // Optimize append operations via array copy
            AppendableCharSequence seq = (AppendableCharSequence) csq;
            char[] src = seq.chars;
            System.arraycopy(src, start, chars, pos, length);
            pos += length;
            return this;
        }
        for (int i = start; i < end; i++) {
            chars[pos++] = csq.charAt(i);
        }

        return this;
    }

    /**
     * Reset the {@link AppendableCharSequence}. Be aware this will only reset the current internal position and not
     * shrink the internal char array.
     * <p>将 {@code pos} 置 0 以复用缓冲区；底层 {@code char[]} 容量不变。</p>
     */
    public void reset() {
        pos = 0;
    }

    @Override
    public String toString() {
        return new String(chars, 0, pos);
    }

    /**
     * Create a new {@link String} from the given start to end.
     * <p>在 {@code [start, end)} 区间构造新字符串，并校验索引不超过 {@code pos}。</p>
     */
    public String substring(int start, int end) {
        int length = end - start;
        if (start > pos || length > pos) {
            throw new IndexOutOfBoundsException("expected: start and length <= ("
                    + pos + ")");
        }
        return new String(chars, start, length);
    }

    /**
     * Create a new {@link String} from the given start to end.
     * This method is considered unsafe as index values are assumed to be legitimate.
     * Only underlying array bounds checking is done.
     * <p>不校验与 {@code pos} 的关系，仅依赖 {@code String} 构造时的数组边界检查。</p>
     */
    public String subStringUnsafe(int start, int end) {
        return new String(chars, start, end - start);
    }

    /** 按需翻倍扩容直至满足 {@code neededSpace}，并拷贝前 {@code size} 个元素。 */
    private static char[] expand(char[] array, int neededSpace, int size) {
        int newCapacity = array.length;
        do {
            // double capacity until it is big enough
            newCapacity <<= 1;

            if (newCapacity < 0) {
                throw new IllegalStateException();
            }

        } while (neededSpace > newCapacity);

        char[] newArray = new char[newCapacity];
        System.arraycopy(array, 0, newArray, 0, size);

        return newArray;
    }
}
