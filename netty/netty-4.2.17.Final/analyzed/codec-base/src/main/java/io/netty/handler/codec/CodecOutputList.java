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
package io.netty.handler.codec;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.MathUtil;

import java.util.AbstractList;
import java.util.RandomAccess;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 编解码基类内部使用的专用 {@link AbstractList}，支持线程本地池化与快速无边界访问。
 */
final class CodecOutputList extends AbstractList<Object> implements RandomAccess {

    private static final CodecOutputListRecycler NOOP_RECYCLER = new CodecOutputListRecycler() {
        @Override
        public void recycle(CodecOutputList object) {
            // 池耗尽时的新实例交给 GC
        }
    };

    private static final FastThreadLocal<CodecOutputLists> CODEC_OUTPUT_LISTS_POOL =
            new FastThreadLocal<CodecOutputLists>() {
                @Override
                protected CodecOutputLists initialValue() throws Exception {
                    // 每线程缓存 16 个 CodecOutputList
                    return new CodecOutputLists(16);
                }
            };

    private interface CodecOutputListRecycler {
        void recycle(CodecOutputList codecOutputList);
    }

    private static final class CodecOutputLists implements CodecOutputListRecycler {
        private final CodecOutputList[] elements;
        private final int mask;

        private int currentIdx;
        private int count;

        CodecOutputLists(int numElements) {
            elements = new CodecOutputList[MathUtil.safeFindNextPositivePowerOfTwo(numElements)];
            for (int i = 0; i < elements.length; ++i) {
                // 初始容量 16 对多数解码场景足够
                elements[i] = new CodecOutputList(this, 16);
            }
            count = elements.length;
            currentIdx = elements.length;
            mask = elements.length - 1;
        }

        public CodecOutputList getOrCreate() {
            if (count == 0) {
                // 池空时分配不可回收实例，初始容量 4 降低开销
                return new CodecOutputList(NOOP_RECYCLER, 4);
            }
            --count;

            int idx = (currentIdx - 1) & mask;
            CodecOutputList list = elements[idx];
            currentIdx = idx;
            return list;
        }

        @Override
        public void recycle(CodecOutputList codecOutputList) {
            int idx = currentIdx;
            elements[idx] = codecOutputList;
            currentIdx = (idx + 1) & mask;
            ++count;
            assert count <= elements.length;
        }
    }

    static CodecOutputList newInstance() {
        return CODEC_OUTPUT_LISTS_POOL.get().getOrCreate();
    }

    private final CodecOutputListRecycler recycler;
    private int size;
    private int maxSeenSize;
    private Object[] array;
    private boolean insertSinceRecycled;

    private CodecOutputList(CodecOutputListRecycler recycler, int size) {
        this.recycler = recycler;
        array = new Object[size];
    }

    @Override
    public Object get(int index) {
        checkIndex(index);
        return array[index];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean add(Object element) {
        checkNotNull(element, "element");
        try {
            insert(size, element);
        } catch (IndexOutOfBoundsException ignore) {
            // 扩容极少发生，捕获 IndexOutOfBounds 后扩容重试
            expandArray();
            insert(size, element);
        }
        ++ size;
        return true;
    }

    @Override
    public Object set(int index, Object element) {
        checkNotNull(element, "element");
        checkIndex(index);

        Object old = array[index];
        insert(index, element);
        return old;
    }

    @Override
    public void add(int index, Object element) {
        checkNotNull(element, "element");
        checkIndex(index);

        if (size == array.length) {
            expandArray();
        }

        if (index != size) {
            System.arraycopy(array, index, array, index + 1, size - index);
        }

        insert(index, element);
        ++ size;
    }

    @Override
    public Object remove(int index) {
        checkIndex(index);
        Object old = array[index];

        int len = size - index - 1;
        if (len > 0) {
            System.arraycopy(array, index + 1, array, index, len);
        }
        array[-- size] = null;

        return old;
    }

    @Override
    public void clear() {
        // clear 仅置 size=0；显式 null 数组须调用 recycle()
        maxSeenSize = Math.max(maxSeenSize, size);
        size = 0;
    }

    /** 自上次 {@link #recycle()} 以来是否写入过元素；用于判断是否需要 fireChannelRead。 */
    boolean insertSinceRecycled() {
        return insertSinceRecycled;
    }

    /** 归还线程本地池：清空数组元素并复位状态。 */
    void recycle() {
        int len = Math.max(maxSeenSize, size);
        for (int i = 0; i < len; i ++) {
            array[i] = null;
        }
        size = 0;
        maxSeenSize = 0;
        insertSinceRecycled = false;

        recycler.recycle(this);
    }

    /** 无边界检查的快速取元素，仅供内部 fireChannelRead 使用。 */
    Object getUnsafe(int index) {
        return array[index];
    }

    private void checkIndex(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("expected: index < ("
                    + size + "),but actual is (" + size + ")");
        }
    }

    private void insert(int index, Object element) {
        array[index] = element;
        insertSinceRecycled = true;
    }

    private void expandArray() {
        // 容量翻倍
        int newCapacity = array.length << 1;

        if (newCapacity < 0) {
            throw new OutOfMemoryError();
        }

        Object[] newArray = new Object[newCapacity];
        System.arraycopy(array, 0, newArray, 0, array.length);

        array = newArray;
    }
}
