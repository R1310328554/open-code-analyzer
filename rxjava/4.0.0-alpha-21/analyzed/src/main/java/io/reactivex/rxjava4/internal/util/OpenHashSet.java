/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

/*
 * Inspired by fastutils' OpenHashSet implementation at
 * https://github.com/vigna/fastutil/blob/master/drv/OpenHashSet.drv
 */

package io.reactivex.rxjava4.internal.util;

/**
 * 开放寻址哈希集合：仅支持 add/remove，不支持 null。
 * 借鉴 fastutil OpenHashSet，负载因子默认 0.75。
 *
 * @param <T> 元素类型
 */
public final class OpenHashSet<T> {
    private static final int INT_PHI = 0x9E3779B9;

    final float loadFactor;
    int mask;
    int size;
    int maxSize;
    T[] keys;

    /** 默认容量 16、负载因子 0.75。 */
    public OpenHashSet() {
        this(16, 0.75f);
    }

    /**
     * 指定初始容量，负载因子 0.75。
     * @param capacity 初始容量（会 round 到 2 的幂）
     */
    public OpenHashSet(int capacity) {
        this(capacity, 0.75f);
    }

    /**
     * @param capacity 初始容量
     * @param loadFactor 负载因子，决定 rehash 阈值
     */
    @SuppressWarnings("unchecked")
    public OpenHashSet(int capacity, float loadFactor) {
        this.loadFactor = loadFactor;
        int c = Pow2.roundToPowerOfTwo(capacity);
        this.mask = c - 1;
        this.maxSize = (int)(loadFactor * c);
        this.keys = (T[])new Object[c];
    }

    /** 线性探测插入；已存在则 false；超 maxSize 时 rehash。 */
    public boolean add(T value) {
        final T[] a = keys;
        final int m = mask;

        int pos = mix(value.hashCode()) & m;
        T curr = a[pos];
        if (curr != null) {
            if (curr.equals(value)) {
                return false;
            }
            for (;;) {
                pos = (pos + 1) & m;
                curr = a[pos];
                if (curr == null) {
                    break;
                }
                if (curr.equals(value)) {
                    return false;
                }
            }
        }
        a[pos] = value;
        if (++size >= maxSize) {
            rehash();
        }
        return true;
    }
    /** 线性探测删除；不存在则 false。 */
    public boolean remove(T value) {
        T[] a = keys;
        int m = mask;
        int pos = mix(value.hashCode()) & m;
        T curr = a[pos];
        if (curr == null) {
            return false;
        }
        if (curr.equals(value)) {
            return removeEntry(pos, a, m);
        }
        for (;;) {
            pos = (pos + 1) & m;
            curr = a[pos];
            if (curr == null) {
                return false;
            }
            if (curr.equals(value)) {
                return removeEntry(pos, a, m);
            }
        }
    }

    /** 删除 pos 处元素并回填后续可前移项（Knuth 算法 6.4R）。 */
    boolean removeEntry(int pos, T[] a, int m) {
        size--;

        int last;
        int slot;
        T curr;
        for (;;) {
            last = pos;
            pos = (pos + 1) & m;
            for (;;) {
                curr = a[pos];
                if (curr == null) {
                    a[last] = null;
                    return true;
                }
                slot = mix(curr.hashCode()) & m;

                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
                    break;
                }

                pos = (pos + 1) & m;
            }
            a[last] = curr;
        }
    }

    /** 容量翻倍并重新分布所有非 null 键。 */
    @SuppressWarnings("unchecked")
    void rehash() {
        T[] a = keys;
        int i = a.length;
        int newCap = i << 1;
        int m = newCap - 1;

        T[] b = (T[])new Object[newCap];

        for (int j = size; j-- != 0; ) {
            while (a[--i] == null) { } // NOPMD
            int pos = mix(a[i].hashCode()) & m;
            if (b[pos] != null) {
                for (;;) {
                    pos = (pos + 1) & m;
                    if (b[pos] == null) {
                        break;
                    }
                }
            }
            b[pos] = a[i];
        }

        this.mask = m;
        this.maxSize = (int)(newCap * loadFactor);
        this.keys = b;
    }

    /** 用 INT_PHI 乘法与高位异或打散 hashCode。 */
    static int mix(int x) {
        final int h = x * INT_PHI;
        return h ^ (h >>> 16);
    }

    /** 返回内部 keys 数组（仅供内部迭代，勿修改）。 */
    public Object[] keys() {
        return keys; // NOPMD
    }

    /** 当前元素个数。 */
    public int size() {
        return size;
    }
}
