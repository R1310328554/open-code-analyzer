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

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.ThreadExecutorMap;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于 jemalloc 风格 arena/chunk 的池化 {@link ByteBufAllocator} 实现。
 * 支持堆与直接内存、线程本地 {@link PoolThreadCache}，并可暴露 JFR 分配事件。
 */
public class PooledByteBufAllocator extends AbstractByteBufAllocator implements ByteBufAllocatorMetricProvider {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PooledByteBufAllocator.class);
    private static final int DEFAULT_NUM_HEAP_ARENA;
    private static final int DEFAULT_NUM_DIRECT_ARENA;

    private static final int DEFAULT_PAGE_SIZE;
    private static final int DEFAULT_MAX_ORDER; // 8192 << 9，每 chunk 约 4 MiB
    private static final int DEFAULT_SMALL_CACHE_SIZE;
    private static final int DEFAULT_NORMAL_CACHE_SIZE;
    static final int DEFAULT_MAX_CACHED_BUFFER_CAPACITY;
    private static final int DEFAULT_CACHE_TRIM_INTERVAL;
    private static final long DEFAULT_CACHE_TRIM_INTERVAL_MILLIS;
    private static final boolean DEFAULT_USE_CACHE_FOR_ALL_THREADS;
    private static final int DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT;
    static final int DEFAULT_MAX_CACHED_BYTEBUFFERS_PER_CHUNK;
    private static final boolean DEFAULT_DISABLE_CACHE_FINALIZERS_FOR_FAST_THREAD_LOCAL_THREADS;

    private static final int MIN_PAGE_SIZE = 4096;
    private static final int MAX_CHUNK_SIZE = (int) (((long) Integer.MAX_VALUE + 1) / 2);

    private static final int CACHE_NOT_USED = 0;

    private final Runnable trimTask = new Runnable() {
        @Override
        public void run() {
            PooledByteBufAllocator.this.trimCurrentThreadCache();
        }
    };

    static {
        int defaultAlignment = SystemPropertyUtil.getInt(
                "io.netty.allocator.directMemoryCacheAlignment", 0);
        int defaultPageSize = SystemPropertyUtil.getInt("io.netty.allocator.pageSize", 8192);
        Throwable pageSizeFallbackCause = null;
        try {
            validateAndCalculatePageShifts(defaultPageSize, defaultAlignment);
        } catch (Throwable t) {
            pageSizeFallbackCause = t;
            defaultPageSize = 8192;
            defaultAlignment = 0;
        }
        DEFAULT_PAGE_SIZE = defaultPageSize;
        DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT = defaultAlignment;

        int defaultMaxOrder = SystemPropertyUtil.getInt("io.netty.allocator.maxOrder", 9);
        Throwable maxOrderFallbackCause = null;
        try {
            validateAndCalculateChunkSize(DEFAULT_PAGE_SIZE, defaultMaxOrder);
        } catch (Throwable t) {
            maxOrderFallbackCause = t;
            defaultMaxOrder = 9;
        }
        DEFAULT_MAX_ORDER = defaultMaxOrder;

        // 根据可用内存估算堆/直接 arena 默认数量；假设每 arena 约 3 个 chunk，池占用不超过约 50% 内存。
        final Runtime runtime = Runtime.getRuntime();

        /*
         * 默认使用 2×CPU 核数以降低争用（与 NIO/EPOLL EventLoop 数量一致）。
         * arena 过少会导致分配/释放在 {@link PoolArena} 上热点同步。
         * 见 https://github.com/netty/netty/issues/3888。
         */
        final int defaultMinNumArena = NettyRuntime.availableProcessors() * 2;
        final int defaultChunkSize = DEFAULT_PAGE_SIZE << DEFAULT_MAX_ORDER;
        DEFAULT_NUM_HEAP_ARENA = Math.max(0,
                SystemPropertyUtil.getInt(
                        "io.netty.allocator.numHeapArenas",
                        (int) Math.min(
                                defaultMinNumArena,
                                runtime.maxMemory() / defaultChunkSize / 2 / 3)));
        DEFAULT_NUM_DIRECT_ARENA = Math.max(0,
                SystemPropertyUtil.getInt(
                        "io.netty.allocator.numDirectArenas",
                        (int) Math.min(
                                defaultMinNumArena,
                                PlatformDependent.maxDirectMemory() / defaultChunkSize / 2 / 3)));

        // 线程本地缓存大小
        DEFAULT_SMALL_CACHE_SIZE = SystemPropertyUtil.getInt("io.netty.allocator.smallCacheSize", 256);
        DEFAULT_NORMAL_CACHE_SIZE = SystemPropertyUtil.getInt("io.netty.allocator.normalCacheSize", 64);

        // 默认缓存缓冲区最大 32KB，策略类似 jemalloc 论文
        DEFAULT_MAX_CACHED_BUFFER_CAPACITY = SystemPropertyUtil.getInt(
                "io.netty.allocator.maxCachedBufferCapacity", 32 * 1024);

        // 分配次数达到该阈值时，trim 不常用的缓存项
        DEFAULT_CACHE_TRIM_INTERVAL = SystemPropertyUtil.getInt(
                "io.netty.allocator.cacheTrimInterval", 8192);

        if (SystemPropertyUtil.contains("io.netty.allocation.cacheTrimIntervalMillis")) {
            logger.warn("-Dio.netty.allocation.cacheTrimIntervalMillis is deprecated," +
                    " use -Dio.netty.allocator.cacheTrimIntervalMillis");

            if (SystemPropertyUtil.contains("io.netty.allocator.cacheTrimIntervalMillis")) {
                // 两个系统属性均存在时，使用非废弃项。
                DEFAULT_CACHE_TRIM_INTERVAL_MILLIS = SystemPropertyUtil.getLong(
                        "io.netty.allocator.cacheTrimIntervalMillis", 0);
            } else {
                DEFAULT_CACHE_TRIM_INTERVAL_MILLIS = SystemPropertyUtil.getLong(
                        "io.netty.allocation.cacheTrimIntervalMillis", 0);
            }
        } else {
            DEFAULT_CACHE_TRIM_INTERVAL_MILLIS = SystemPropertyUtil.getLong(
                    "io.netty.allocator.cacheTrimIntervalMillis", 0);
        }

        DEFAULT_USE_CACHE_FOR_ALL_THREADS = SystemPropertyUtil.getBoolean(
                "io.netty.allocator.useCacheForAllThreads", false);

        DEFAULT_DISABLE_CACHE_FINALIZERS_FOR_FAST_THREAD_LOCAL_THREADS = SystemPropertyUtil.getBoolean(
                "io.netty.allocator.disableCacheFinalizersForFastThreadLocalThreads", false);

        // 默认 1023：ArrayDeque 内部数组为 1024，避免浪费到 2048。
        DEFAULT_MAX_CACHED_BYTEBUFFERS_PER_CHUNK = SystemPropertyUtil.getInt(
                "io.netty.allocator.maxCachedByteBuffersPerChunk", 1023);

        if (logger.isDebugEnabled()) {
            logger.debug("-Dio.netty.allocator.numHeapArenas: {}", DEFAULT_NUM_HEAP_ARENA);
            logger.debug("-Dio.netty.allocator.numDirectArenas: {}", DEFAULT_NUM_DIRECT_ARENA);
            if (pageSizeFallbackCause == null) {
                logger.debug("-Dio.netty.allocator.pageSize: {}", DEFAULT_PAGE_SIZE);
            } else {
                logger.debug("-Dio.netty.allocator.pageSize: {}", DEFAULT_PAGE_SIZE, pageSizeFallbackCause);
            }
            if (maxOrderFallbackCause == null) {
                logger.debug("-Dio.netty.allocator.maxOrder: {}", DEFAULT_MAX_ORDER);
            } else {
                logger.debug("-Dio.netty.allocator.maxOrder: {}", DEFAULT_MAX_ORDER, maxOrderFallbackCause);
            }
            logger.debug("-Dio.netty.allocator.chunkSize: {}", DEFAULT_PAGE_SIZE << DEFAULT_MAX_ORDER);
            logger.debug("-Dio.netty.allocator.smallCacheSize: {}", DEFAULT_SMALL_CACHE_SIZE);
            logger.debug("-Dio.netty.allocator.normalCacheSize: {}", DEFAULT_NORMAL_CACHE_SIZE);
            logger.debug("-Dio.netty.allocator.maxCachedBufferCapacity: {}", DEFAULT_MAX_CACHED_BUFFER_CAPACITY);
            logger.debug("-Dio.netty.allocator.cacheTrimInterval: {}", DEFAULT_CACHE_TRIM_INTERVAL);
            logger.debug("-Dio.netty.allocator.cacheTrimIntervalMillis: {}", DEFAULT_CACHE_TRIM_INTERVAL_MILLIS);
            logger.debug("-Dio.netty.allocator.useCacheForAllThreads: {}", DEFAULT_USE_CACHE_FOR_ALL_THREADS);
            logger.debug("-Dio.netty.allocator.maxCachedByteBuffersPerChunk: {}",
                    DEFAULT_MAX_CACHED_BYTEBUFFERS_PER_CHUNK);
            logger.debug("-Dio.netty.allocator.disableCacheFinalizersForFastThreadLocalThreads: {}",
                         DEFAULT_DISABLE_CACHE_FINALIZERS_FOR_FAST_THREAD_LOCAL_THREADS);
        }
    }

    /** 全局默认池化分配器。 */
    /** 全局默认池化分配器。 */
    public static final PooledByteBufAllocator DEFAULT =
            new PooledByteBufAllocator(!PlatformDependent.isExplicitNoPreferDirect());

    private final PoolArena<byte[]>[] heapArenas;
    private final PoolArena<ByteBuffer>[] directArenas;
    private final int smallCacheSize;
    private final int normalCacheSize;
    private final List<PoolArenaMetric> heapArenaMetrics;
    private final List<PoolArenaMetric> directArenaMetrics;
    private final PoolThreadLocalCache threadCache;
    private final int chunkSize;
    private final PooledByteBufAllocatorMetric metric;

    public PooledByteBufAllocator() {
        this(false);
    }

    @SuppressWarnings("deprecation")
    public PooledByteBufAllocator(boolean preferDirect) {
        this(preferDirect, DEFAULT_NUM_HEAP_ARENA, DEFAULT_NUM_DIRECT_ARENA, DEFAULT_PAGE_SIZE, DEFAULT_MAX_ORDER);
    }

    @SuppressWarnings("deprecation")
    public PooledByteBufAllocator(int nHeapArena, int nDirectArena, int pageSize, int maxOrder) {
        this(false, nHeapArena, nDirectArena, pageSize, maxOrder);
    }

    /**
     * @deprecated 请使用
     * {@link PooledByteBufAllocator#PooledByteBufAllocator(boolean, int, int, int, int, int, int, boolean)}
     */
    @Deprecated
    public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder) {
        this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder,
             0, DEFAULT_SMALL_CACHE_SIZE, DEFAULT_NORMAL_CACHE_SIZE);
    }

    /**
     * @deprecated use
     * {@link PooledByteBufAllocator#PooledByteBufAllocator(boolean, int, int, int, int, int, int, boolean)}
     */
    @Deprecated
    public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder,
                                  int tinyCacheSize, int smallCacheSize, int normalCacheSize) {
        this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder, smallCacheSize,
             normalCacheSize, DEFAULT_USE_CACHE_FOR_ALL_THREADS, DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT);
    }

    /**
     * @deprecated use
     * {@link PooledByteBufAllocator#PooledByteBufAllocator(boolean, int, int, int, int, int, int, boolean)}
     */
    @Deprecated
    public PooledByteBufAllocator(boolean preferDirect, int nHeapArena,
                                  int nDirectArena, int pageSize, int maxOrder, int tinyCacheSize,
                                  int smallCacheSize, int normalCacheSize,
                                  boolean useCacheForAllThreads) {
        this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder,
             smallCacheSize, normalCacheSize,
             useCacheForAllThreads);
    }

    public PooledByteBufAllocator(boolean preferDirect, int nHeapArena,
                                  int nDirectArena, int pageSize, int maxOrder,
                                  int smallCacheSize, int normalCacheSize,
                                  boolean useCacheForAllThreads) {
        this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder,
             smallCacheSize, normalCacheSize,
             useCacheForAllThreads, DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT);
    }

    /**
     * @deprecated use
     * {@link PooledByteBufAllocator#PooledByteBufAllocator(boolean, int, int, int, int, int, int, boolean, int)}
     */
    @Deprecated
    public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder,
                                  int tinyCacheSize, int smallCacheSize, int normalCacheSize,
                                  boolean useCacheForAllThreads, int directMemoryCacheAlignment) {
        this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder,
             smallCacheSize, normalCacheSize,
             useCacheForAllThreads, directMemoryCacheAlignment);
    }

    public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder,
                                  int smallCacheSize, int normalCacheSize,
                                  boolean useCacheForAllThreads, int directMemoryCacheAlignment) {
        super(preferDirect);
        threadCache = new PoolThreadLocalCache(useCacheForAllThreads);
        this.smallCacheSize = smallCacheSize;
        this.normalCacheSize = normalCacheSize;

        if (directMemoryCacheAlignment != 0) {
            if (!PlatformDependent.hasAlignDirectByteBuffer()) {
                throw new UnsupportedOperationException("Buffer alignment is not supported. " +
                        "Either Unsafe or ByteBuffer.alignSlice() must be available.");
            }

            // 保证 pageSize 为 alignment 的整数倍。
            pageSize = (int) PlatformDependent.align(pageSize, directMemoryCacheAlignment);
        }

        chunkSize = validateAndCalculateChunkSize(pageSize, maxOrder);

        checkPositiveOrZero(nHeapArena, "nHeapArena");
        checkPositiveOrZero(nDirectArena, "nDirectArena");

        checkPositiveOrZero(directMemoryCacheAlignment, "directMemoryCacheAlignment");
        if (directMemoryCacheAlignment > 0 && !isDirectMemoryCacheAlignmentSupported()) {
            throw new IllegalArgumentException("directMemoryCacheAlignment is not supported");
        }

        if ((directMemoryCacheAlignment & -directMemoryCacheAlignment) != directMemoryCacheAlignment) {
            throw new IllegalArgumentException("directMemoryCacheAlignment: "
                    + directMemoryCacheAlignment + " (expected: power of two)");
        }

        int pageShifts = validateAndCalculatePageShifts(pageSize, directMemoryCacheAlignment);

        if (nHeapArena > 0) {
            heapArenas = newArenaArray(nHeapArena);
            List<PoolArenaMetric> metrics = new ArrayList<PoolArenaMetric>(heapArenas.length);
            final SizeClasses sizeClasses = new SizeClasses(pageSize, pageShifts, chunkSize, 0);
            for (int i = 0; i < heapArenas.length; i ++) {
                PoolArena.HeapArena arena = new PoolArena.HeapArena(this, sizeClasses);
                heapArenas[i] = arena;
                metrics.add(arena);
            }
            heapArenaMetrics = Collections.unmodifiableList(metrics);
        } else {
            heapArenas = null;
            heapArenaMetrics = Collections.emptyList();
        }

        if (nDirectArena > 0) {
            directArenas = newArenaArray(nDirectArena);
            List<PoolArenaMetric> metrics = new ArrayList<PoolArenaMetric>(directArenas.length);
            final SizeClasses sizeClasses = new SizeClasses(pageSize, pageShifts, chunkSize,
                    directMemoryCacheAlignment);
            for (int i = 0; i < directArenas.length; i ++) {
                PoolArena.DirectArena arena = new PoolArena.DirectArena(this, sizeClasses);
                directArenas[i] = arena;
                metrics.add(arena);
            }
            directArenaMetrics = Collections.unmodifiableList(metrics);
        } else {
            directArenas = null;
            directArenaMetrics = Collections.emptyList();
        }
        metric = new PooledByteBufAllocatorMetric(this);
    }

    @SuppressWarnings("unchecked")
    private static <T> PoolArena<T>[] newArenaArray(int size) {
        return new PoolArena[size];
    }

    private static int validateAndCalculatePageShifts(int pageSize, int alignment) {
        if (pageSize < MIN_PAGE_SIZE) {
            throw new IllegalArgumentException("pageSize: " + pageSize + " (expected: " + MIN_PAGE_SIZE + ')');
        }

        if ((pageSize & pageSize - 1) != 0) {
            throw new IllegalArgumentException("pageSize: " + pageSize + " (expected: power of 2)");
        }

        if (pageSize < alignment) {
            throw new IllegalArgumentException("Alignment cannot be greater than page size. " +
                    "Alignment: " + alignment + ", page size: " + pageSize + '.');
        }

        // 以 2 为底的对数；此时 pageSize 已为 2 的幂。
        return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(pageSize);
    }

    private static int validateAndCalculateChunkSize(int pageSize, int maxOrder) {
        if (maxOrder < 0 || maxOrder > 14) {
            throw new IllegalArgumentException("maxOrder: " + maxOrder + " (expected: 0-14)");
        }

        // 确保 chunkSize 不溢出。
        int chunkSize = pageSize;
        for (int i = maxOrder; i > 0; i --) {
            if (chunkSize > MAX_CHUNK_SIZE / 2) {
                throw new IllegalArgumentException(String.format(
                        "pageSize (%d) << maxOrder (%d) must not exceed %d", pageSize, maxOrder, MAX_CHUNK_SIZE));
            }
            chunkSize <<= 1;
        }
        return chunkSize;
    }

    @Override
    protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
        PoolThreadCache cache = threadCache.get();
        PoolArena<byte[]> heapArena = cache.heapArena;

        final AbstractByteBuf buf;
        if (heapArena != null) {
            buf = heapArena.allocate(cache, initialCapacity, maxCapacity);
        } else {
            buf = PlatformDependent.hasUnsafe() ?
                    new UnpooledUnsafeHeapByteBuf(this, initialCapacity, maxCapacity) :
                    new UnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
            onAllocateBuffer(buf, false, false);
        }
        return toLeakAwareBuffer(buf);
    }

    @Override
    protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
        PoolThreadCache cache = threadCache.get();
        PoolArena<ByteBuffer> directArena = cache.directArena;

        final AbstractByteBuf buf;
        if (directArena != null) {
            buf = directArena.allocate(cache, initialCapacity, maxCapacity);
        } else {
            buf = UnsafeByteBufUtil.newDirectByteBuf(this, initialCapacity, maxCapacity);
            onAllocateBuffer(buf, false, false);
        }
        return toLeakAwareBuffer(buf);
    }

    /**
     * 默认堆 arena 数量；系统属性 {@code io.netty.allocator.numHeapArenas}，默认 2×核数。
     */
    public static int defaultNumHeapArena() {
        return DEFAULT_NUM_HEAP_ARENA;
    }

    /**
     * 默认直接内存 arena 数量；系统属性 {@code io.netty.allocator.numDirectArenas}，默认 2×核数。
     */
    public static int defaultNumDirectArena() {
        return DEFAULT_NUM_DIRECT_ARENA;
    }

    /**
     * 默认页大小；系统属性 {@code io.netty.allocator.pageSize}，默认 8192。
     */
    public static int defaultPageSize() {
        return DEFAULT_PAGE_SIZE;
    }

    /**
     * 默认 chunk 阶数；系统属性 {@code io.netty.allocator.maxOrder}，默认 9。
     */
    public static int defaultMaxOrder() {
        return DEFAULT_MAX_ORDER;
    }

    /**
     * 是否为 FastThreadLocalThread 禁用 PoolThreadCache finalizer；
     * 系统属性 {@code io.netty.allocator.disableCacheFinalizersForFastThreadLocalThreads}，默认 false。
     */
    public static boolean defaultDisableCacheFinalizersForFastThreadLocalThreads() {
        return DEFAULT_DISABLE_CACHE_FINALIZERS_FOR_FAST_THREAD_LOCAL_THREADS;
    }

    /**
     * 是否对所有线程启用缓存；系统属性 {@code io.netty.allocator.useCacheForAllThreads}，默认 false。
     */
    public static boolean defaultUseCacheForAllThreads() {
        return DEFAULT_USE_CACHE_FOR_ALL_THREADS;
    }

    /**
     * 是否优先直接内存；与系统属性 {@code io.netty.noPreferDirect} 相反，默认 false。
     */
    public static boolean defaultPreferDirect() {
        return PlatformDependent.directBufferPreferred();
    }

    /**
     * 默认 tiny 缓存大小，现为 0。
     *
     * @deprecated Tiny 缓存已并入 small 缓存。
     */
    @Deprecated
    public static int defaultTinyCacheSize() {
        return 0;
    }

    /**
     * 默认 small 缓存大小；系统属性 {@code io.netty.allocator.smallCacheSize}，默认 256。
     */
    public static int defaultSmallCacheSize() {
        return DEFAULT_SMALL_CACHE_SIZE;
    }

    /**
     * 默认 normal 缓存大小；系统属性 {@code io.netty.allocator.normalCacheSize}，默认 64。
     */
    public static int defaultNormalCacheSize() {
        return DEFAULT_NORMAL_CACHE_SIZE;
    }

    /**
     * 若支持直接内存缓存对齐则返回 {@code true}。
     */
    public static boolean isDirectMemoryCacheAlignmentSupported() {
        return PlatformDependent.hasUnsafe();
    }

    @Override
    public boolean isDirectBufferPooled() {
        return directArenas != null;
    }

    /**
     * @deprecated 将移除
     * 若当前 {@link Thread} 已绑定 {@link ThreadLocal} 缓冲区缓存则返回 {@code true}。
     */
    @Deprecated
    public boolean hasThreadLocalCache() {
        return threadCache.isSet();
    }

    /**
     * @deprecated 将移除
     * 释放当前 {@link Thread} 缓存的全部缓冲区。
     */
    @Deprecated
    public void freeThreadLocalCache() {
        threadCache.remove();
    }

    private final class PoolThreadLocalCache extends FastThreadLocal<PoolThreadCache> {
        private final boolean useCacheForAllThreads;

        PoolThreadLocalCache(boolean useCacheForAllThreads) {
            this.useCacheForAllThreads = useCacheForAllThreads;
        }

        @Override
        protected synchronized PoolThreadCache initialValue() {
            final PoolArena<byte[]> heapArena = leastUsedArena(heapArenas);
            final PoolArena<ByteBuffer> directArena = leastUsedArena(directArenas);

            final Thread current = Thread.currentThread();
            final EventExecutor executor = ThreadExecutorMap.currentExecutor();

            if (useCacheForAllThreads ||
                    // FastThreadLocalThread 始终使用线程缓存
                    FastThreadLocalThread.currentThreadHasFastThreadLocal() ||
                    // EventExecutor 线程预期频繁分配，启用缓存
                    executor != null) {
                final PoolThreadCache cache = new PoolThreadCache(
                        heapArena, directArena, smallCacheSize, normalCacheSize,
                        DEFAULT_MAX_CACHED_BUFFER_CAPACITY, DEFAULT_CACHE_TRIM_INTERVAL, useCacheFinalizers());

                if (DEFAULT_CACHE_TRIM_INTERVAL_MILLIS > 0) {
                    if (executor != null) {
                        executor.scheduleAtFixedRate(trimTask, DEFAULT_CACHE_TRIM_INTERVAL_MILLIS,
                                DEFAULT_CACHE_TRIM_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
                    }
                }
                return cache;
            }
            // 不使用缓存，缓存大小设为 0。
            return new PoolThreadCache(heapArena, directArena, 0, 0, 0, 0, false);
        }

        @Override
        protected void onRemoval(PoolThreadCache threadCache) {
            threadCache.free(false);
        }

        private <T> PoolArena<T> leastUsedArena(PoolArena<T>[] arenas) {
            if (arenas == null || arenas.length == 0) {
                return null;
            }

            PoolArena<T> minArena = arenas[0];
            // 优化：首次使用时直接返回 minArena，减少循环比较
            if (minArena.numThreadCaches.get() == CACHE_NOT_USED) {
                return minArena;
            }
            for (int i = 1; i < arenas.length; i++) {
                PoolArena<T> arena = arenas[i];
                if (arena.numThreadCaches.get() < minArena.numThreadCaches.get()) {
                    minArena = arena;
                }
            }

            return minArena;
        }
    }

    private static boolean useCacheFinalizers() {
        if (!defaultDisableCacheFinalizersForFastThreadLocalThreads()) {
            return true;
        }
        return FastThreadLocalThread.currentThreadWillCleanupFastThreadLocals();
    }

    @Override
    public PooledByteBufAllocatorMetric metric() {
        return metric;
    }

    /**
     * 返回堆 arena 数量。
     *
     * @deprecated 请使用 {@link PooledByteBufAllocatorMetric#numHeapArenas()}。
     */
    @Deprecated
    public int numHeapArenas() {
        return heapArenaMetrics.size();
    }

    /**
     * 返回直接内存 arena 数量。
     *
     * @deprecated 请使用 {@link PooledByteBufAllocatorMetric#numDirectArenas()}。
     */
    @Deprecated
    public int numDirectArenas() {
        return directArenaMetrics.size();
    }

    /**
     * 返回此池提供的全部堆 {@link PoolArenaMetric}。
     *
     * @deprecated 请使用 {@link PooledByteBufAllocatorMetric#heapArenas()}。
     */
    @Deprecated
    public List<PoolArenaMetric> heapArenas() {
        return heapArenaMetrics;
    }

    /**
     * 返回此池提供的全部直接 {@link PoolArenaMetric}。
     *
     * @deprecated 请使用 {@link PooledByteBufAllocatorMetric#directArenas()}。
     */
    @Deprecated
    public List<PoolArenaMetric> directArenas() {
        return directArenaMetrics;
    }

    /**
     * 返回此 {@link PooledByteBufAllocator} 使用的线程本地缓存数量。
     *
     * @deprecated 请使用 {@link PooledByteBufAllocatorMetric#numThreadLocalCaches()}。
     */
    @Deprecated
    public int numThreadLocalCaches() {
        return Math.max(numThreadLocalCaches(heapArenas), numThreadLocalCaches(directArenas));
    }

    private static int numThreadLocalCaches(PoolArena<?>[] arenas) {
        if (arenas == null) {
            return 0;
        }

        int total = 0;
        for (PoolArena<?> arena : arenas) {
            total += arena.numThreadCaches.get();
        }

        return total;
    }

    /**
     * 返回 tiny 缓存大小。
     *
     * @deprecated 请使用 {@link PooledByteBufAllocatorMetric#tinyCacheSize()}。
     */
    @Deprecated
    public int tinyCacheSize() {
        return 0;
    }

    /**
     * 返回 small 缓存大小。
     *
     * @deprecated 请使用 {@link PooledByteBufAllocatorMetric#smallCacheSize()}。
     */
    @Deprecated
    public int smallCacheSize() {
        return smallCacheSize;
    }

    /**
     * 返回 normal 缓存大小。
     *
     * @deprecated 请使用 {@link PooledByteBufAllocatorMetric#normalCacheSize()}。
     */
    @Deprecated
    public int normalCacheSize() {
        return normalCacheSize;
    }

    /**
     * 返回每个 arena 的 chunk 字节大小。
     *
     * @deprecated 请使用 {@link PooledByteBufAllocatorMetric#chunkSize()}。
     */
    @Deprecated
    public final int chunkSize() {
        return chunkSize;
    }

    final long usedHeapMemory() {
        return usedMemory(heapArenas);
    }

    final long usedDirectMemory() {
        return usedMemory(directArenas);
    }

    private static long usedMemory(PoolArena<?>[] arenas) {
        if (arenas == null) {
            return -1;
        }
        long used = 0;
        for (PoolArena<?> arena : arenas) {
            used += arena.numActiveBytes();
            if (used < 0) {
                return Long.MAX_VALUE;
            }
        }
        return used;
    }

    /**
     * 返回 {@link ByteBufAllocator} 当前钉住（pinned）的堆内存字节数；未知时 {@code -1}。
     * 因分配器实现，钉住量可能大于 {@linkplain ByteBuf#capacity() 逻辑容量}。
     */
    public final long pinnedHeapMemory() {
        return pinnedMemory(heapArenas);
    }

    /**
     * 返回 {@link ByteBufAllocator} 当前钉住的直接内存字节数；未知时 {@code -1}。
     * 因分配器实现，钉住量可能大于 {@linkplain ByteBuf#capacity() 逻辑容量}。
     */
    public final long pinnedDirectMemory() {
        return pinnedMemory(directArenas);
    }

    private static long pinnedMemory(PoolArena<?>[] arenas) {
        if (arenas == null) {
            return -1;
        }
        long used = 0;
        for (PoolArena<?> arena : arenas) {
            used += arena.numPinnedBytes();
            if (used < 0) {
                return Long.MAX_VALUE;
            }
        }
        return used;
    }

    final PoolThreadCache threadCache() {
        PoolThreadCache cache =  threadCache.get();
        assert cache != null;
        return cache;
    }

    /**
     * 对当前 {@link Thread} 的本地缓存执行 trim，归还不常用的缓存内存。
     *
     * @return 若存在并已 trim 缓存则为 {@code true}，否则 {@code false}。
     */
    public boolean trimCurrentThreadCache() {
        PoolThreadCache cache = threadCache.getIfExists();
        if (cache != null) {
            cache.trim();
            return true;
        }
        return false;
    }

    /**
     * 以字符串返回分配器状态（含全部指标）；开销较大，不宜频繁调用。
     */
    public String dumpStats() {
        int heapArenasLen = heapArenas == null ? 0 : heapArenas.length;
        StringBuilder buf = new StringBuilder(512)
                .append(heapArenasLen)
                .append(" heap arena(s):")
                .append(StringUtil.NEWLINE);
        if (heapArenasLen > 0) {
            for (PoolArena<byte[]> a: heapArenas) {
                buf.append(a);
            }
        }

        int directArenasLen = directArenas == null ? 0 : directArenas.length;

        buf.append(directArenasLen)
           .append(" direct arena(s):")
           .append(StringUtil.NEWLINE);
        if (directArenasLen > 0) {
            for (PoolArena<ByteBuffer> a: directArenas) {
                buf.append(a);
            }
        }

        return buf.toString();
    }

    static void onAllocateBuffer(AbstractByteBuf buf, boolean pooled, boolean threadLocal) {
        if (PlatformDependent.isJfrEnabled() && AllocateBufferEvent.isEventEnabled()) {
            AllocateBufferEvent event = new AllocateBufferEvent();
            if (event.shouldCommit()) {
                event.fill(buf, PooledByteBufAllocator.class);
                event.chunkPooled = pooled;
                event.chunkThreadLocal = threadLocal;
                event.commit();
            }
        }
    }

    static void onDeallocateBuffer(AbstractByteBuf buf) {
        if (PlatformDependent.isJfrEnabled() && FreeBufferEvent.isEventEnabled()) {
            FreeBufferEvent event = new FreeBufferEvent();
            if (event.shouldCommit()) {
                event.fill(buf, PooledByteBufAllocator.class);
                event.commit();
            }
        }
    }

    static void onReallocateBuffer(AbstractByteBuf buf, int newCapacity) {
        if (PlatformDependent.isJfrEnabled() && ReallocateBufferEvent.isEventEnabled()) {
            ReallocateBufferEvent event = new ReallocateBufferEvent();
            if (event.shouldCommit()) {
                event.fill(buf, PooledByteBufAllocator.class);
                event.newCapacity = newCapacity;
                event.commit();
            }
        }
    }

    static void onAllocateChunk(ChunkInfo chunk, boolean pooled) {
        if (PlatformDependent.isJfrEnabled() && AllocateChunkEvent.isEventEnabled()) {
            AllocateChunkEvent event = new AllocateChunkEvent();
            if (event.shouldCommit()) {
                event.fill(chunk, PooledByteBufAllocator.class);
                event.pooled = pooled;
                event.threadLocal = false; // 池化分配器的 chunk 始终共享
                event.commit();
            }
        }
    }

    static void onDeallocateChunk(ChunkInfo chunk, boolean pooled) {
        if (PlatformDependent.isJfrEnabled() && FreeChunkEvent.isEventEnabled()) {
            FreeChunkEvent event = new FreeChunkEvent();
            if (event.shouldCommit()) {
                event.fill(chunk, PooledByteBufAllocator.class);
                event.pooled = pooled;
                event.commit();
            }
        }
    }
}
