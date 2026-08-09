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


import io.netty.buffer.PoolArena.SizeClass;
import io.netty.util.Recycler;
import io.netty.util.Recycler.EnhancedHandle;
import io.netty.util.internal.MathUtil;
import io.netty.util.internal.ObjectPool.Handle;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * 线程本地分配缓存，设计借鉴
 * <a href="https://people.freebsd.org/~jasone/jemalloc/bsdcan2006/jemalloc.pdf">jemalloc</a> 及
 * <a href="https://www.facebook.com/notes/facebook-engineering/scalable-memory-allocation-using-jemalloc/480222803919">
 * Facebook 关于 jemalloc 可扩展内存分配</a> 的思路。
 * <p>
 * 按 Small/Normal 尺寸档维护 {@link MemoryRegionCache} 队列，
 * 分配优先命中缓存，释放时尝试入缓存以减少 Arena 锁争用。
 */
final class PoolThreadCache {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PoolThreadCache.class);
    private static final int INTEGER_SIZE_MINUS_ONE = Integer.SIZE - 1;

    final PoolArena<byte[]> heapArena;
    final PoolArena<ByteBuffer> directArena;

    // 各尺寸档（Small / Normal）的堆/direct 缓存数组
    private final MemoryRegionCache<byte[]>[] smallSubPageHeapCaches;
    private final MemoryRegionCache<ByteBuffer>[] smallSubPageDirectCaches;
    private final MemoryRegionCache<byte[]>[] normalHeapCaches;
    private final MemoryRegionCache<ByteBuffer>[] normalDirectCaches;

    private final int freeSweepAllocationThreshold;
    private final AtomicBoolean freed = new AtomicBoolean();
    @SuppressWarnings("unused") // 仅用于 finalizer 路径持有引用
    private final FreeOnFinalize freeOnFinalize;

    private int allocations;

    // TODO: Test if adding padding helps under contention
    //private long pad0, pad1, pad2, pad3, pad4, pad5, pad6, pad7;

    PoolThreadCache(PoolArena<byte[]> heapArena, PoolArena<ByteBuffer> directArena,
                    int smallCacheSize, int normalCacheSize, int maxCachedBufferCapacity,
                    int freeSweepAllocationThreshold, boolean useFinalizer) {
        checkPositiveOrZero(maxCachedBufferCapacity, "maxCachedBufferCapacity");
        this.freeSweepAllocationThreshold = freeSweepAllocationThreshold;
        this.heapArena = heapArena;
        this.directArena = directArena;
        if (directArena != null) {
            smallSubPageDirectCaches = createSubPageCaches(smallCacheSize, directArena.sizeClass.nSubpages);
            normalDirectCaches = createNormalCaches(normalCacheSize, maxCachedBufferCapacity, directArena);
            directArena.numThreadCaches.getAndIncrement();
        } else {
            // 未配置 direct Arena，direct 侧缓存置 null
            smallSubPageDirectCaches = null;
            normalDirectCaches = null;
        }
        if (heapArena != null) {
            // 为堆 Arena 创建对应缓存
            smallSubPageHeapCaches = createSubPageCaches(smallCacheSize, heapArena.sizeClass.nSubpages);
            normalHeapCaches = createNormalCaches(normalCacheSize, maxCachedBufferCapacity, heapArena);
            heapArena.numThreadCaches.getAndIncrement();
        } else {
            // 未配置 heap Arena，heap 侧缓存置 null
            smallSubPageHeapCaches = null;
            normalHeapCaches = null;
        }

        // 仅在有缓存启用时校验 sweep 阈值
        if ((smallSubPageDirectCaches != null || normalDirectCaches != null
                || smallSubPageHeapCaches != null || normalHeapCaches != null)
                && freeSweepAllocationThreshold < 1) {
            throw new IllegalArgumentException("freeSweepAllocationThreshold: "
                    + freeSweepAllocationThreshold + " (expected: > 0)");
        }
        freeOnFinalize = useFinalizer ? new FreeOnFinalize(this) : null;
    }

    private static <T> MemoryRegionCache<T>[] createSubPageCaches(
            int cacheSize, int numCaches) {
        if (cacheSize > 0 && numCaches > 0) {
            @SuppressWarnings("unchecked")
            MemoryRegionCache<T>[] cache = new MemoryRegionCache[numCaches];
            for (int i = 0; i < cache.length; i++) {
                // TODO: 可考虑 cacheSize / cache.length 分摊
                cache[i] = new SubPageMemoryRegionCache<T>(cacheSize);
            }
            return cache;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> MemoryRegionCache<T>[] createNormalCaches(
            int cacheSize, int maxCachedBufferCapacity, PoolArena<T> area) {
        if (cacheSize > 0 && maxCachedBufferCapacity > 0) {
            int max = Math.min(area.sizeClass.chunkSize, maxCachedBufferCapacity);
            // 按 sizeIdx 与 maxCachedBufferCapacity 上限创建 Normal 档缓存条目
            // bound is that we want to cache in general.
            List<MemoryRegionCache<T>> cache = new ArrayList<MemoryRegionCache<T>>() ;
            for (int idx = area.sizeClass.nSubpages; idx < area.sizeClass.nSizes &&
                    area.sizeClass.sizeIdx2size(idx) <= max; idx++) {
                cache.add(new NormalMemoryRegionCache<T>(cacheSize));
            }
            return cache.toArray(new MemoryRegionCache[0]);
        } else {
            return null;
        }
    }

    // 计算 val 的 log2，要求 val > 0
    static int log2(int val) {
        return INTEGER_SIZE_MINUS_ONE - Integer.numberOfLeadingZeros(val);
    }

    /**
     * 尝试从线程缓存分配 Small 缓冲；成功返回 {@code true}。
     */
    boolean allocateSmall(PoolArena<?> area, PooledByteBuf<?> buf, int reqCapacity, int sizeIdx) {
        return allocate(cacheForSmall(area, sizeIdx), buf, reqCapacity);
    }

    /**
     * 尝试从线程缓存分配 Normal 缓冲；成功返回 {@code true}。
     */
    boolean allocateNormal(PoolArena<?> area, PooledByteBuf<?> buf, int reqCapacity, int sizeIdx) {
        return allocate(cacheForNormal(area, sizeIdx), buf, reqCapacity);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean allocate(MemoryRegionCache<?> cache, PooledByteBuf buf, int reqCapacity) {
        if (cache == null) {
            // 无对应缓存，回退 Arena 分配
            return false;
        }
        boolean allocated = cache.allocate(buf, reqCapacity, this);
        if (++ allocations >= freeSweepAllocationThreshold) {
            allocations = 0;
            trim();
        }
        return allocated;
    }

    /**
     * 若缓存有空间，将 {@link PoolChunk} 与 handle 入缓存。
     * @return 入缓存成功为 {@code true}，否则需直接归还 Arena
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    boolean add(PoolArena<?> area, PoolChunk chunk, ByteBuffer nioBuffer,
                long handle, int normCapacity, SizeClass sizeClass) {
        int sizeIdx = area.sizeClass.size2SizeIdx(normCapacity);
        MemoryRegionCache<?> cache = cache(area, sizeIdx, sizeClass);
        if (cache == null) {
            return false;
        }
        if (freed.get()) {
            return false;
        }
        return cache.add(chunk, nioBuffer, handle, normCapacity);
    }

    private MemoryRegionCache<?> cache(PoolArena<?> area, int sizeIdx, SizeClass sizeClass) {
        switch (sizeClass) {
        case Normal:
            return cacheForNormal(area, sizeIdx);
        case Small:
            return cacheForSmall(area, sizeIdx);
        default:
            throw new Error("Unexpected size class: " + sizeClass);
        }
    }

    /**
     * 线程退出或 FastThreadLocal 移除时应调用，释放缓存中全部 region。
     */
    void free(boolean finalizer) {
        // free() 可能由 finalizer 或 FastThreadLocal.onRemoval 调用，须保证只执行一次
        // we only call this one time.
        if (freed.compareAndSet(false, true)) {
            if (freeOnFinalize != null) {
                // 协助 GC：可能与 finalizer 竞态，但无论如何置 null
                freeOnFinalize.cache = null;
            }
            int numFreed = free(smallSubPageDirectCaches, finalizer) +
                           free(normalDirectCaches, finalizer) +
                           free(smallSubPageHeapCaches, finalizer) +
                           free(normalHeapCaches, finalizer);

            if (numFreed > 0 && logger.isDebugEnabled()) {
                logger.debug("Freed {} thread-local buffer(s) from thread: {}", numFreed,
                             Thread.currentThread().getName());
            }

            if (directArena != null) {
                directArena.numThreadCaches.getAndDecrement();
            }

            if (heapArena != null) {
                heapArena.numThreadCaches.getAndDecrement();
            }
        }
    }

    private static int free(MemoryRegionCache<?>[] caches, boolean finalizer) {
        if (caches == null) {
            return 0;
        }

        int numFreed = 0;
        for (MemoryRegionCache<?> c: caches) {
            numFreed += free(c, finalizer);
        }
        return numFreed;
    }

    private static int free(MemoryRegionCache<?> cache, boolean finalizer) {
        if (cache == null) {
            return 0;
        }
        return cache.free(finalizer);
    }

    void trim() {
        trim(smallSubPageDirectCaches);
        trim(normalDirectCaches);
        trim(smallSubPageHeapCaches);
        trim(normalHeapCaches);
    }

    private static void trim(MemoryRegionCache<?>[] caches) {
        if (caches == null) {
            return;
        }
        for (MemoryRegionCache<?> c: caches) {
            trim(c);
        }
    }

    private static void trim(MemoryRegionCache<?> cache) {
        if (cache == null) {
            return;
        }
        cache.trim();
    }

    private MemoryRegionCache<?> cacheForSmall(PoolArena<?> area, int sizeIdx) {
        if (area.isDirect()) {
            return cache(smallSubPageDirectCaches, sizeIdx);
        }
        return cache(smallSubPageHeapCaches, sizeIdx);
    }

    private MemoryRegionCache<?> cacheForNormal(PoolArena<?> area, int sizeIdx) {
        // sizeIdx 为全局索引，Normal 缓存下标需减去 nSubpages
        int idx = sizeIdx - area.sizeClass.nSubpages;
        if (area.isDirect()) {
            return cache(normalDirectCaches, idx);
        }
        return cache(normalHeapCaches, idx);
    }

    private static <T> MemoryRegionCache<T> cache(MemoryRegionCache<T>[] cache, int sizeIdx) {
        if (cache == null || sizeIdx >= cache.length) {
            return null;
        }
        return cache[sizeIdx];
    }

    /** Small 档（Subpage）内存区域缓存 */
    private static final class SubPageMemoryRegionCache<T> extends MemoryRegionCache<T> {
        SubPageMemoryRegionCache(int size) {
            super(size, SizeClass.Small);
        }

        @Override
        protected void initBuf(
                PoolChunk<T> chunk, ByteBuffer nioBuffer, long handle, PooledByteBuf<T> buf, int reqCapacity,
                PoolThreadCache threadCache) {
            chunk.initBufWithSubpage(buf, nioBuffer, handle, reqCapacity, threadCache, true);
        }
    }

    /** Normal 档（整 run）内存区域缓存 */
    private static final class NormalMemoryRegionCache<T> extends MemoryRegionCache<T> {
        NormalMemoryRegionCache(int size) {
            super(size, SizeClass.Normal);
        }

        @Override
        protected void initBuf(
                PoolChunk<T> chunk, ByteBuffer nioBuffer, long handle, PooledByteBuf<T> buf, int reqCapacity,
                PoolThreadCache threadCache) {
            chunk.initBuf(buf, nioBuffer, handle, reqCapacity, threadCache, true);
        }
    }

    private abstract static class MemoryRegionCache<T> {
        private final int size;
        private final Queue<Entry<T>> queue;
        private final SizeClass sizeClass;
        private int allocations;

        MemoryRegionCache(int size, SizeClass sizeClass) {
            this.size = MathUtil.safeFindNextPositivePowerOfTwo(size);
            queue = PlatformDependent.newFixedMpscUnpaddedQueue(this.size);
            this.sizeClass = sizeClass;
        }

        /** 用 chunk/handle 初始化 {@link PooledByteBuf}，由子类区分 Subpage 与 run */
        protected abstract void initBuf(PoolChunk<T> chunk, ByteBuffer nioBuffer, long handle,
                                        PooledByteBuf<T> buf, int reqCapacity, PoolThreadCache threadCache);

        /** 缓存未满时将 entry 入队 */
        @SuppressWarnings("unchecked")
        public final boolean add(PoolChunk<T> chunk, ByteBuffer nioBuffer, long handle, int normCapacity) {
            Entry<T> entry = newEntry(chunk, nioBuffer, handle, normCapacity);
            boolean queued = queue.offer(entry);
            if (!queued) {
                // 队列已满时立即回收 Entry 对象
                entry.unguardedRecycle();
            }

            return queued;
        }

        /** 从缓存 poll 一条 entry 并完成 buf 初始化 */
        public final boolean allocate(PooledByteBuf<T> buf, int reqCapacity, PoolThreadCache threadCache) {
            Entry<T> entry = queue.poll();
            if (entry == null) {
                return false;
            }
            initBuf(entry.chunk, entry.nioBuffer, entry.handle, buf, reqCapacity, threadCache);
            entry.unguardedRecycle();

            // allocations 非线程安全，仅在同一线程调用
            ++ allocations;
            return true;
        }

        /** 清空缓存并将全部 region 归还 Arena */
        public final int free(boolean finalizer) {
            return free(Integer.MAX_VALUE, finalizer);
        }

        private int free(int max, boolean finalizer) {
            int numFreed = 0;
            for (; numFreed < max; numFreed++) {
                Entry<T> entry = queue.poll();
                if (entry != null) {
                    freeEntry(entry, finalizer);
                } else {
                    // 队列已空
                    return numFreed;
                }
            }
            return numFreed;
        }

        /** 按分配频率 trim：未充分复用的 entry 归还 Arena */
        public final void trim() {
            int free = size - allocations;
            allocations = 0;

            // 本周期分配次数不足 size，释放多余缓存
            if (free > 0) {
                free(free, false);
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private  void freeEntry(Entry entry, boolean finalizer) {
            // 回收 Entry 前先捕获 chunk/handle 等字段
            PoolChunk chunk = entry.chunk;
            long handle = entry.handle;
            ByteBuffer nioBuffer = entry.nioBuffer;
            int normCapacity = entry.normCapacity;

            if (!finalizer) {
                // 非 finalizer 路径立即 recycle Entry，便于 PoolChunk GC
                // finalizer 路径则跳过 recycle
                entry.recycle();
            }

            chunk.arena.freeChunk(chunk, handle, normCapacity, sizeClass, nioBuffer, finalizer);
        }

        static final class Entry<T> {
            final EnhancedHandle<Entry<?>> recyclerHandle;
            PoolChunk<T> chunk;
            ByteBuffer nioBuffer;
            long handle = -1;
            int normCapacity;

            Entry(Handle<Entry<?>> recyclerHandle) {
                this.recyclerHandle = (EnhancedHandle<Entry<?>>) recyclerHandle;
            }

            void recycle() {
                chunk = null;
                nioBuffer = null;
                handle = -1;
                recyclerHandle.recycle(this);
            }

            void unguardedRecycle() {
                chunk = null;
                nioBuffer = null;
                handle = -1;
                recyclerHandle.unguardedRecycle(this);
            }
        }

        @SuppressWarnings("rawtypes")
        private static Entry newEntry(PoolChunk<?> chunk, ByteBuffer nioBuffer, long handle, int normCapacity) {
            Entry entry = RECYCLER.get();
            entry.chunk = chunk;
            entry.nioBuffer = nioBuffer;
            entry.handle = handle;
            entry.normCapacity = normCapacity;
            return entry;
        }

        @SuppressWarnings("rawtypes")
        private static final Recycler<Entry> RECYCLER = new Recycler<Entry>() {
            @Override
            @SuppressWarnings("unchecked")
            protected Entry newObject(Handle<Entry> handle) {
                return new Entry(handle);
            }
        };
    }

    private static final class FreeOnFinalize {

        private volatile PoolThreadCache cache;

        private FreeOnFinalize(PoolThreadCache cache) {
            this.cache = cache;
        }

        /// TODO: 未来迁移 Java 9+ 时可改用 java.lang.ref.Cleaner
        @SuppressWarnings({"FinalizeDeclaration", "deprecation"})
        @Override
        protected void finalize() throws Throwable {
            try {
                PoolThreadCache cache = this.cache;
                // 可能与非 finalizer 线程的 free 竞态；无论谁赢 cache 都会被置 null
                // null out
                this.cache = null;
                if (cache != null) {
                    cache.free(true);
                }
            } finally {
                super.finalize();
            }
        }
    }
}
