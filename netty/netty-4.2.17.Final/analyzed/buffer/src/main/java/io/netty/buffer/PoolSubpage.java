/*
 * Copyright 2012 The Netty Project
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

package io.netty.buffer;

import java.util.concurrent.locks.ReentrantLock;

import static io.netty.buffer.PoolChunk.RUN_OFFSET_SHIFT;
import static io.netty.buffer.PoolChunk.SIZE_SHIFT;
import static io.netty.buffer.PoolChunk.IS_USED_SHIFT;
import static io.netty.buffer.PoolChunk.IS_SUBPAGE_SHIFT;

/**
 * 将一页 run 切分为多个等长小对象的 Subpage 分配器。
 * <p>
 * 使用位图跟踪各元素占用；同尺寸 Subpage 在 {@link PoolArena#smallSubpagePools} 中组成双向链表。
 * 当 Subpage 完全空闲且池中存在同类 Subpage 时，整页 run 可归还 Chunk。
 */
final class PoolSubpage<T> implements PoolSubpageMetric {

    final PoolChunk<T> chunk;
    final int elemSize;
    private final int pageShifts;
    private final int runOffset;
    private final int runSize;
    private final long[] bitmap;
    private final int bitmapLength;
    private final int maxNumElems;
    final int headIndex;

    PoolSubpage<T> prev;
    PoolSubpage<T> next;

    boolean doNotDestroy;
    private int nextAvail;
    private int numAvail;

    final ReentrantLock lock;

    // TODO: Test if adding padding helps under contention
    //private long pad0, pad1, pad2, pad3, pad4, pad5, pad6, pad7;

    /** 构造 Subpage 池链表头节点（非真实 Subpage） */
    PoolSubpage(int headIndex) {
        chunk = null;
        lock = new ReentrantLock();
        pageShifts = -1;
        runOffset = -1;
        elemSize = -1;
        runSize = -1;
        bitmap = null;
        bitmapLength = -1;
        maxNumElems = 0;
        this.headIndex = headIndex;
    }

    PoolSubpage(PoolSubpage<T> head, PoolChunk<T> chunk, int pageShifts, int runOffset, int runSize, int elemSize) {
        this.headIndex = head.headIndex;
        this.chunk = chunk;
        this.pageShifts = pageShifts;
        this.runOffset = runOffset;
        this.runSize = runSize;
        this.elemSize = elemSize;

        doNotDestroy = true;

        maxNumElems = numAvail = runSize / elemSize;
        int bitmapLength = maxNumElems >>> 6;
        if ((maxNumElems & 63) != 0) {
            bitmapLength ++;
        }
        this.bitmapLength = bitmapLength;
        bitmap = new long[bitmapLength];
        nextAvail = 0;

        lock = null;
        addToPool(head);
    }

    /**
     * 分配一个元素，返回编码后的 handle（含 bitmap 索引）。
     */
    long allocate() {
        if (numAvail == 0 || !doNotDestroy) {
            return -1;
        }

        final int bitmapIdx = getNextAvail();
        if (bitmapIdx < 0) {
            removeFromPool(); // 状态异常，移出池以防重复错误
            throw new AssertionError("No next available bitmap index found (bitmapIdx = " + bitmapIdx + "), " +
                    "even though there are supposed to be (numAvail = " + numAvail + ") " +
                    "out of (maxNumElems = " + maxNumElems + ") available indexes.");
        }
        int q = bitmapIdx >>> 6;
        int r = bitmapIdx & 63;
        assert (bitmap[q] >>> r & 1) == 0;
        bitmap[q] |= 1L << r;

        if (-- numAvail == 0) {
            removeFromPool();
        }

        return toHandle(bitmapIdx);
    }

    /**
     * 释放指定 bitmap 索引的元素。
     * @return {@code true} 表示 Subpage 仍在使用；{@code false} 表示可整页归还 Chunk
     */
    boolean free(PoolSubpage<T> head, int bitmapIdx) {
        int q = bitmapIdx >>> 6;
        int r = bitmapIdx & 63;
        assert (bitmap[q] >>> r & 1) != 0;
        bitmap[q] ^= 1L << r;

        setNextAvail(bitmapIdx);

        if (numAvail ++ == 0) {
            addToPool(head);
            /* maxNumElems == 1 时 numAvail 最大也为 1；
             * 此类 Subpage 每次 free 都会进入此分支；
             * 若此处直接 return true，后续回收逻辑不可达，
             * Subpage 无法被回收；故仅在 maxNumElems > 1 时 return true */
            if (maxNumElems > 1) {
                return true;
            }
        }

        if (numAvail != maxNumElems) {
            return true;
        } else {
            // Subpage 已完全空闲（numAvail == maxNumElems）
            if (prev == next) {
                // 池中仅剩本 Subpage 时不移除，保留空池头
                return true;
            }

            // 池中尚有其他 Subpage 时移出本节点，允许整页释放
            doNotDestroy = false;
            removeFromPool();
            return false;
        }
    }

    private void addToPool(PoolSubpage<T> head) {
        assert prev == null && next == null;
        prev = head;
        next = head.next;
        next.prev = this;
        head.next = this;
    }

    private void removeFromPool() {
        assert prev != null && next != null;
        prev.next = next;
        next.prev = prev;
        next = null;
        prev = null;
    }

    private void setNextAvail(int bitmapIdx) {
        nextAvail = bitmapIdx;
    }

    private int getNextAvail() {
        int nextAvail = this.nextAvail;
        if (nextAvail >= 0) {
            this.nextAvail = -1;
            return nextAvail;
        }
        return findNextAvail();
    }

    private int findNextAvail() {
        for (int i = 0; i < bitmapLength; i ++) {
            long bits = bitmap[i];
            if (~bits != 0) {
                return findNextAvail0(i, bits);
            }
        }
        return -1;
    }

    private int findNextAvail0(int i, long bits) {
        final int baseVal = i << 6;
        for (int j = 0; j < 64; j ++) {
            if ((bits & 1) == 0) {
                int val = baseVal | j;
                if (val < maxNumElems) {
                    return val;
                } else {
                    break;
                }
            }
            bits >>>= 1;
        }
        return -1;
    }

    private long toHandle(int bitmapIdx) {
        int pages = runSize >> pageShifts;
        return (long) runOffset << RUN_OFFSET_SHIFT
               | (long) pages << SIZE_SHIFT
               | 1L << IS_USED_SHIFT
               | 1L << IS_SUBPAGE_SHIFT
               | bitmapIdx;
    }

    @Override
    public String toString() {
        final int numAvail;
        if (chunk == null) {
            // 链表头节点无需同步
            numAvail = 0;
        } else {
            final boolean doNotDestroy;
            PoolSubpage<T> head = chunk.arena.smallSubpagePools[headIndex];
            head.lock();
            try {
                doNotDestroy = this.doNotDestroy;
                numAvail = this.numAvail;
            } finally {
                head.unlock();
            }
            if (!doNotDestroy) {
                // 已标记销毁，toString 仅返回占位信息
                return "(" + runOffset + ": not in use)";
            }
        }

        return "(" + this.runOffset + ": " + (this.maxNumElems - numAvail) + '/' + this.maxNumElems +
                ", offset: " + this.runOffset + ", length: " + this.runSize + ", elemSize: " + this.elemSize + ')';
    }

    @Override
    public int maxNumElements() {
        return maxNumElems;
    }

    @Override
    public int numAvailable() {
        if (chunk == null) {
            // 链表头节点
            return 0;
        }
        PoolSubpage<T> head = chunk.arena.smallSubpagePools[headIndex];
        head.lock();
        try {
            return numAvail;
        } finally {
            head.unlock();
        }
    }

    @Override
    public int elementSize() {
        return elemSize;
    }

    @Override
    public int pageSize() {
        return 1 << pageShifts;
    }

    boolean isDoNotDestroy() {
        if (chunk == null) {
            // It's the head.
            return true;
        }
        PoolSubpage<T> head = chunk.arena.smallSubpagePools[headIndex];
        head.lock();
        try {
            return doNotDestroy;
        } finally {
            head.unlock();
        }
    }

    void destroy() {
        if (chunk != null) {
            chunk.destroy();
        }
    }

    void lock() {
        lock.lock();
    }

    void unlock() {
        lock.unlock();
    }
}
