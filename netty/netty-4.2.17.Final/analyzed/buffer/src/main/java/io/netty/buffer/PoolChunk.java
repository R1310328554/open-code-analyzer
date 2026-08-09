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

import io.netty.util.internal.CleanableDirectBuffer;
import io.netty.util.internal.LongLongHashMap;
import io.netty.util.internal.SystemPropertyUtil;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link PoolChunk} 内 PageRun / PoolSubpage 分配算法说明。
 * <p>
 * 术语：page 为最小分配单元；run 为连续 page 集合；chunk 为 run 集合；
 * {@code chunkSize = maxPages * pageSize}。
 * <p>
 * 分配时按 {@link SizeClasses} 归一化尺寸，在 chunk  backing 存储中定位足够空间，
 * 返回编码偏移与元数据的 {@code long} handle，并将该段标记为已占用。
 * <p>
 * Chunk 布局示意：多个 run / subpage / 空闲区交替排列。
 * <p>
 * handle 位布局（run）：
 * o(15) runOffset | s(15) pages | u isUsed | e isSubpage | b(32) bitmapIdx
 * <p>
 * {@code runsAvailMap}：key 为 run 首/末 page 偏移，value 为 handle。
 * {@code runsAvail}：按 run 页数分桶的 {@link IntPriorityQueue}，同桶内按偏移最小优先分配。
 * <p>
 * allocateRun：在 runsAvail 中 best-fit 取 run，过大则 split 尾部。
 * allocateSubpage：复用未满 Subpage 或新建 Subpage 并挂入 Arena 池。
 * free：Subpage 元素归还；run 标记空闲、合并相邻 avail run 后重新入队。
 */
final class PoolChunk<T> implements PoolChunkMetric, ChunkInfo {
    private static final int SIZE_BIT_LENGTH = 15;
    private static final int INUSED_BIT_LENGTH = 1;
    private static final int SUBPAGE_BIT_LENGTH = 1;
    private static final int BITMAP_IDX_BIT_LENGTH = 32;

    private static final boolean trackPinnedMemory =
            SystemPropertyUtil.getBoolean("io.netty.trackPinnedMemory", true);

    static final int IS_SUBPAGE_SHIFT = BITMAP_IDX_BIT_LENGTH;
    static final int IS_USED_SHIFT = SUBPAGE_BIT_LENGTH + IS_SUBPAGE_SHIFT;
    static final int SIZE_SHIFT = INUSED_BIT_LENGTH + IS_USED_SHIFT;
    static final int RUN_OFFSET_SHIFT = SIZE_BIT_LENGTH + SIZE_SHIFT;

    final PoolArena<T> arena;
    final CleanableDirectBuffer cleanable;
    final Object base;
    final T memory;
    final boolean unpooled;

    /** 记录每个可用 run 的首页与末页偏移 → handle */
    private final LongLongHashMap runsAvailMap;

    /** 按 run 页数索引的可用 run 最小堆数组 */
    private final IntPriorityQueue[] runsAvail;

    private final ReentrantLock runsAvailLock;

    /** 本 Chunk 内各 run 偏移处的 Subpage（若有） */
    private final PoolSubpage<T>[] subpages;

    /** 当前被 ByteBuf 占用的 pinned 字节计数（可选，由系统属性控制） */
    private final LongAdder pinnedBytes;

    final int pageSize;
    final int pageShifts;
    final int chunkSize;
    final int maxPageIdx;

    // 缓存由底层 memory 创建的 ByteBuffer duplicate，减少 Pooled*ByteBuf 路径上的 GC
    // around the memory itself. These are often needed for operations within the Pooled*ByteBuf and so
    // may produce extra GC, which can be greatly reduced by caching the duplicates.
    //
    // This may be null if the PoolChunk is unpooled as pooling the ByteBuffer instances does not make any sense here.
    private final Deque<ByteBuffer> cachedNioBuffers;

    int freeBytes;

    PoolChunkList<T> parent;
    PoolChunk<T> prev;
    PoolChunk<T> next;

    @SuppressWarnings("unchecked")
    PoolChunk(PoolArena<T> arena, CleanableDirectBuffer cleanable, Object base, T memory, int pageSize, int pageShifts,
              int chunkSize, int maxPageIdx) {
        unpooled = false;
        this.arena = arena;
        this.cleanable = cleanable;
        this.base = base;
        this.memory = memory;
        this.pageSize = pageSize;
        this.pageShifts = pageShifts;
        this.chunkSize = chunkSize;
        this.maxPageIdx = maxPageIdx;
        freeBytes = chunkSize;

        runsAvail = newRunsAvailqueueArray(maxPageIdx);
        runsAvailLock = new ReentrantLock();
        runsAvailMap = new LongLongHashMap(-1);
        subpages = new PoolSubpage[chunkSize >> pageShifts];

        // 初始化：整 Chunk 作为一个可用 run 入队
        int pages = chunkSize >> pageShifts;
        long initHandle = (long) pages << SIZE_SHIFT;
        insertAvailRun(0, pages, initHandle);

        cachedNioBuffers = new ArrayDeque<>(8);
        this.pinnedBytes = trackPinnedMemory ? new LongAdder() : null;
    }

    /** 构造 Huge 档非池化 Chunk（无 runsAvail/subpages 结构） */
    PoolChunk(PoolArena<T> arena, CleanableDirectBuffer cleanable, Object base, T memory, int size) {
        unpooled = true;
        this.arena = arena;
        this.cleanable = cleanable;
        this.base = base;
        this.memory = memory;
        pageSize = 0;
        pageShifts = 0;
        maxPageIdx = 0;
        runsAvailMap = null;
        runsAvail = null;
        runsAvailLock = null;
        subpages = null;
        chunkSize = size;
        cachedNioBuffers = null;
        this.pinnedBytes = trackPinnedMemory ? new LongAdder() : null;
    }

    private static IntPriorityQueue[] newRunsAvailqueueArray(int size) {
        IntPriorityQueue[] queueArray = new IntPriorityQueue[size];
        for (int i = 0; i < queueArray.length; i++) {
            queueArray[i] = new IntPriorityQueue();
        }
        return queueArray;
    }

    private void insertAvailRun(int runOffset, int pages, long handle) {
        int pageIdxFloor = arena.sizeClass.pages2pageIdxFloor(pages);
        IntPriorityQueue queue = runsAvail[pageIdxFloor];
        assert isRun(handle);
        queue.offer((int) (handle >> BITMAP_IDX_BIT_LENGTH));

        // 记录 run 首页
        insertAvailRun0(runOffset, handle);
        if (pages > 1) {
            // 多页 run 同时记录末页
            insertAvailRun0(lastPage(runOffset, pages), handle);
        }
    }

    private void insertAvailRun0(int runOffset, long handle) {
        long pre = runsAvailMap.put(runOffset, handle);
        assert pre == -1;
    }

    private void removeAvailRun(long handle) {
        int pageIdxFloor = arena.sizeClass.pages2pageIdxFloor(runPages(handle));
        runsAvail[pageIdxFloor].remove((int) (handle >> BITMAP_IDX_BIT_LENGTH));
        removeAvailRun0(handle);
    }

    private void removeAvailRun0(long handle) {
        int runOffset = runOffset(handle);
        int pages = runPages(handle);
        // 移除 run 首页映射
        runsAvailMap.remove(runOffset);
        if (pages > 1) {
            // 移除 run 末页映射
            runsAvailMap.remove(lastPage(runOffset, pages));
        }
    }

    private static int lastPage(int runOffset, int pages) {
        return runOffset + pages - 1;
    }

    private long getAvailRunByOffset(int runOffset) {
        return runsAvailMap.get(runOffset);
    }

    @Override
    public int usage() {
        final int freeBytes;
        if (this.unpooled) {
            freeBytes = this.freeBytes;
        } else {
            runsAvailLock.lock();
            try {
                freeBytes = this.freeBytes;
            } finally {
                runsAvailLock.unlock();
            }
        }
        return usage(freeBytes);
    }

    private int usage(int freeBytes) {
        if (freeBytes == 0) {
            return 100;
        }

        int freePercentage = (int) (freeBytes * 100L / chunkSize);
        if (freePercentage == 0) {
            return 99;
        }
        return 100 - freePercentage;
    }

    boolean allocate(PooledByteBuf<T> buf, int reqCapacity, int sizeIdx, PoolThreadCache cache) {
        final long handle;
        if (sizeIdx <= arena.sizeClass.smallMaxSizeIdx) {
            final PoolSubpage<T> nextSub;
            // small
            // 在 Arena 的 Subpage 池头同步（Small 分配路径）
            // This is need as we may add it back and so alter the linked-list structure.
            PoolSubpage<T> head = arena.smallSubpagePools[sizeIdx];
            head.lock();
            try {
                nextSub = head.next;
                if (nextSub != head) {
                    assert nextSub.doNotDestroy && nextSub.elemSize == arena.sizeClass.sizeIdx2size(sizeIdx) :
                            "doNotDestroy=" + nextSub.doNotDestroy + ", elemSize=" + nextSub.elemSize + ", sizeIdx=" +
                                    sizeIdx;
                    handle = nextSub.allocate();
                    assert handle >= 0;
                    assert isSubpage(handle);
                    nextSub.chunk.initBufWithSubpage(buf, null, handle, reqCapacity, cache, false);
                    return true;
                }
                handle = allocateSubpage(sizeIdx, head);
                if (handle < 0) {
                    return false;
                }
                assert isSubpage(handle);
            } finally {
                head.unlock();
            }
        } else {
            // Normal 档：分配整 run
            // runSize 须为 pageSize 整数倍
            int runSize = arena.sizeClass.sizeIdx2size(sizeIdx);
            handle = allocateRun(runSize);
            if (handle < 0) {
                return false;
            }
            assert !isSubpage(handle);
        }

        ByteBuffer nioBuffer = cachedNioBuffers != null? cachedNioBuffers.pollLast() : null;
        initBuf(buf, nioBuffer, handle, reqCapacity, cache, false);
        return true;
    }

    private long allocateRun(int runSize) {
        int pages = runSize >> pageShifts;
        int pageIdx = arena.sizeClass.pages2pageIdx(pages);

        runsAvailLock.lock();
        try {
            // 从 pageIdx 起找第一个非空且足够大的 run 队列
            int queueIdx = runFirstBestFit(pageIdx);
            if (queueIdx == -1) {
                return -1;
            }

            // 取该队列堆顶（最小偏移 run）
            IntPriorityQueue queue = runsAvail[queueIdx];
            long handle = queue.poll();
            assert handle != IntPriorityQueue.NO_VALUE;
            handle <<= BITMAP_IDX_BIT_LENGTH;
            assert !isUsed(handle) : "invalid handle: " + handle;

            removeAvailRun0(handle);

            handle = splitLargeRun(handle, pages);

            int pinnedSize = runSize(pageShifts, handle);
            freeBytes -= pinnedSize;
            return handle;
        } finally {
            runsAvailLock.unlock();
        }
    }

    private int calculateRunSize(int sizeIdx) {
        int maxElements = 1 << pageShifts - SizeClasses.LOG2_QUANTUM;
        int runSize = 0;
        int nElements;

        final int elemSize = arena.sizeClass.sizeIdx2size(sizeIdx);

        // 求 pageSize 与 elemSize 的最小公倍数，确定 Subpage run 大小
        do {
            runSize += pageSize;
            nElements = runSize / elemSize;
        } while (nElements < maxElements && runSize != nElements * elemSize);

        while (nElements > maxElements) {
            runSize -= pageSize;
            nElements = runSize / elemSize;
        }

        assert nElements > 0;
        assert runSize <= chunkSize;
        assert runSize >= elemSize;

        return runSize;
    }

    private int runFirstBestFit(int pageIdx) {
        if (freeBytes == chunkSize) {
            return arena.sizeClass.nPSizes - 1;
        }
        for (int i = pageIdx; i < arena.sizeClass.nPSizes; i++) {
            IntPriorityQueue queue = runsAvail[i];
            if (queue != null && !queue.isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private long splitLargeRun(long handle, int needPages) {
        assert needPages > 0;

        int totalPages = runPages(handle);
        assert needPages <= totalPages;

        int remPages = totalPages - needPages;

        if (remPages > 0) {
            int runOffset = runOffset(handle);

            // 拆分后尾部未用 page 作为新 avail run 入队
            int availOffset = runOffset + needPages;
            long availRun = toRunHandle(availOffset, remPages, 0);
            insertAvailRun(availOffset, remPages, availRun);

            // not avail
            return toRunHandle(runOffset, needPages, 1);
        }

        // 恰好匹配时标记 isUsed
        handle |= 1L << IS_USED_SHIFT;
        return handle;
    }

    /**
     * 为指定 sizeIdx 分配 run 并初始化 {@link PoolSubpage}，挂入 Arena 的 Subpage 池。
     *
     * @param sizeIdx 归一化尺寸索引
     * @param head Subpage 链表头
     * @return 分配得到的 handle
     */
    private long allocateSubpage(int sizeIdx, PoolSubpage<T> head) {
        //allocate a new run
        int runSize = calculateRunSize(sizeIdx);
        //runSize must be multiples of pageSize
        long runHandle = allocateRun(runSize);
        if (runHandle < 0) {
            return -1;
        }

        int runOffset = runOffset(runHandle);
        assert subpages[runOffset] == null;
        int elemSize = arena.sizeClass.sizeIdx2size(sizeIdx);

        PoolSubpage<T> subpage = new PoolSubpage<T>(head, this, pageShifts, runOffset,
                runSize(pageShifts, runHandle), elemSize);

        subpages[runOffset] = subpage;
        return subpage.allocate();
    }

    /**
     * 释放 Subpage 元素或整 run。
     * <p>
     * Subpage 完全空闲且 Arena 池中存在同类 Subpage 时，可整页归还为 avail run。
     *
     * @param handle 待释放的 handle
     */
    void free(long handle, int normCapacity, ByteBuffer nioBuffer) {
        if (isSubpage(handle)) {
            int sIdx = runOffset(handle);
            PoolSubpage<T> subpage = subpages[sIdx];
            assert subpage != null;
            PoolSubpage<T> head = subpage.chunk.arena.smallSubpagePools[subpage.headIndex];
            // Obtain the head of the PoolSubPage pool that is owned by the PoolArena and synchronize on it.
            // This is need as we may add it back and so alter the linked-list structure.
            head.lock();
            try {
                assert subpage.doNotDestroy;
                if (subpage.free(head, bitmapIdx(handle))) {
                    // Subpage 仍有占用，不继续释放 run
                    return;
                }
                assert !subpage.doNotDestroy;
                // Subpage 已销毁，清空数组槽位
                subpages[sIdx] = null;
            } finally {
                head.unlock();
            }
        }

        int runSize = runSize(pageShifts, handle);
        // 开始释放 run：合并相邻空闲区并更新 freeBytes
        runsAvailLock.lock();
        try {
            // 向前后合并连续 avail run
            // will be removed from runsAvail and runsAvailMap
            long finalRun = collapseRuns(handle);

            // 清除 isUsed / isSubpage 标记
            finalRun &= ~(1L << IS_USED_SHIFT);
            //if it is a subpage, set it to run
            finalRun &= ~(1L << IS_SUBPAGE_SHIFT);

            insertAvailRun(runOffset(finalRun), runPages(finalRun), finalRun);
            freeBytes += runSize;
        } finally {
            runsAvailLock.unlock();
        }

        if (nioBuffer != null && cachedNioBuffers != null &&
            cachedNioBuffers.size() < PooledByteBufAllocator.DEFAULT_MAX_CACHED_BYTEBUFFERS_PER_CHUNK) {
            cachedNioBuffers.offer(nioBuffer);
        }
    }

    private long collapseRuns(long handle) {
        return collapseNext(collapsePast(handle));
    }

    private long collapsePast(long handle) {
        for (;;) {
            int runOffset = runOffset(handle);
            int runPages = runPages(handle);

            long pastRun = getAvailRunByOffset(runOffset - 1);
            if (pastRun == -1) {
                return handle;
            }

            int pastOffset = runOffset(pastRun);
            int pastPages = runPages(pastRun);

            // 与前/后 run 连续，合并
            if (pastRun != handle && pastOffset + pastPages == runOffset) {
                // 移除被合并的前 run
                removeAvailRun(pastRun);
                handle = toRunHandle(pastOffset, pastPages + runPages, 0);
            } else {
                return handle;
            }
        }
    }

    private long collapseNext(long handle) {
        for (;;) {
            int runOffset = runOffset(handle);
            int runPages = runPages(handle);

            long nextRun = getAvailRunByOffset(runOffset + runPages);
            if (nextRun == -1) {
                return handle;
            }

            int nextOffset = runOffset(nextRun);
            int nextPages = runPages(nextRun);

            //is continuous
            if (nextRun != handle && runOffset + runPages == nextOffset) {
                // 移除被合并的后 run
                removeAvailRun(nextRun);
                handle = toRunHandle(runOffset, runPages + nextPages, 0);
            } else {
                return handle;
            }
        }
    }

    private static long toRunHandle(int runOffset, int runPages, int inUsed) {
        return (long) runOffset << RUN_OFFSET_SHIFT
               | (long) runPages << SIZE_SHIFT
               | (long) inUsed << IS_USED_SHIFT;
    }

    void initBuf(PooledByteBuf<T> buf, ByteBuffer nioBuffer, long handle, int reqCapacity,
                 PoolThreadCache threadCache, boolean threadLocal) {
        if (isSubpage(handle)) {
            initBufWithSubpage(buf, nioBuffer, handle, reqCapacity, threadCache, threadLocal);
        } else {
            int maxLength = runSize(pageShifts, handle);
            buf.init(this, nioBuffer, handle, runOffset(handle) << pageShifts,
                    reqCapacity, maxLength, arena.parent.threadCache(), threadLocal);
        }
    }

    void initBufWithSubpage(PooledByteBuf<T> buf, ByteBuffer nioBuffer, long handle, int reqCapacity,
                            PoolThreadCache threadCache, boolean threadLocal) {
        int runOffset = runOffset(handle);
        int bitmapIdx = bitmapIdx(handle);

        PoolSubpage<T> s = subpages[runOffset];
        assert s.isDoNotDestroy();
        assert reqCapacity <= s.elemSize : reqCapacity + "<=" + s.elemSize;

        int offset = (runOffset << pageShifts) + bitmapIdx * s.elemSize;
        buf.init(this, nioBuffer, handle, offset, reqCapacity, s.elemSize, threadCache, threadLocal);
    }

    void incrementPinnedMemory(int delta) {
        assert delta > 0;
        if (pinnedBytes != null) {
            pinnedBytes.add(delta);
        }
    }

    void decrementPinnedMemory(int delta) {
        assert delta > 0;
        if (pinnedBytes != null) {
            pinnedBytes.add(-delta);
        }
    }

    @Override
    public int chunkSize() {
        return chunkSize;
    }

    @Override
    public int freeBytes() {
        if (this.unpooled) {
            return freeBytes;
        }
        runsAvailLock.lock();
        try {
            return freeBytes;
        } finally {
            runsAvailLock.unlock();
        }
    }

    public int pinnedBytes() {
        return pinnedBytes == null ? 0 : (int) pinnedBytes.sum();
    }

    @Override
    public String toString() {
        final int freeBytes;
        if (this.unpooled) {
            freeBytes = this.freeBytes;
        } else {
            runsAvailLock.lock();
            try {
                freeBytes = this.freeBytes;
            } finally {
                runsAvailLock.unlock();
            }
        }

        return new StringBuilder()
                .append("Chunk(")
                .append(Integer.toHexString(System.identityHashCode(this)))
                .append(": ")
                .append(usage(freeBytes))
                .append("%, ")
                .append(chunkSize - freeBytes)
                .append('/')
                .append(chunkSize)
                .append(')')
                .toString();
    }

    void destroy() {
        arena.destroyChunk(this);
    }

    static int runOffset(long handle) {
        return (int) (handle >> RUN_OFFSET_SHIFT);
    }

    static int runSize(int pageShifts, long handle) {
        return runPages(handle) << pageShifts;
    }

    static int runPages(long handle) {
        return (int) (handle >> SIZE_SHIFT & 0x7fff);
    }

    static boolean isUsed(long handle) {
        return (handle >> IS_USED_SHIFT & 1) == 1L;
    }

    static boolean isRun(long handle) {
        return !isSubpage(handle);
    }

    static boolean isSubpage(long handle) {
        return (handle >> IS_SUBPAGE_SHIFT & 1) == 1L;
    }

    static int bitmapIdx(long handle) {
        return (int) handle;
    }

    @Override
    public int capacity() {
        return chunkSize;
    }

    @Override
    public boolean isDirect() {
        return cleanable != null && cleanable.buffer().isDirect();
    }

    @Override
    public long memoryAddress() {
        return cleanable != null && cleanable.hasMemoryAddress() ? cleanable.memoryAddress() : 0L;
    }
}
