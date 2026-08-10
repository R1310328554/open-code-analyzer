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
package io.netty.util;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.ObjectPool;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.UnstableApi;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.jctools.queues.MessagePassingQueue;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static io.netty.util.internal.PlatformDependent.newFixedMpmcQueue;
import static io.netty.util.internal.PlatformDependent.newMpscQueue;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Light-weight object pool based on a thread-local stack.
 *
 * @param <T> the type of the pooled object
 *
 * <p>基于线程本地池的轻量对象复用器：默认每线程独立 {@link LocalPool}，
 * 通过 {@link Handle#recycle(Object)} 归还实例；支持 Guarded（状态校验）与 Unguarded（高性能）两种模式，
 * 以及跨线程共享池、线程绑定（owner）等高级构造。</p>
 */
public abstract class Recycler<T> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Recycler.class);

    /** {@link EnhancedHandle} 的 LocalPool 专用实现，避免 NOOP/Default/Local 三种具体类。
     * We created this handle to avoid having more than 2 concrete implementations of {@link EnhancedHandle}
     * i.e. NOOP_HANDLE, {@link DefaultHandle} and the one used in the LocalPool.
     */
    private static final class LocalPoolHandle<T> extends EnhancedHandle<T> {
        private final UnguardedLocalPool<T> pool;

        private LocalPoolHandle(UnguardedLocalPool<T> pool) {
            this.pool = pool;
        }

        @Override
        public void recycle(T object) {
            UnguardedLocalPool<T> pool = this.pool;
            if (pool != null) {
                pool.release(object);
            }
        }

        @Override
        public void unguardedRecycle(final Object object) {
            UnguardedLocalPool<T> pool = this.pool;
            if (pool != null) {
                pool.release((T) object);
            }
        }
    }

    /** 禁用池化时的空 Handle。 */
    private static final EnhancedHandle<?> NOOP_HANDLE = new LocalPoolHandle<>(null);
    /** 容量为 0 时的空 LocalPool。 */
    private static final UnguardedLocalPool<?> NOOP_LOCAL_POOL = new UnguardedLocalPool<>(0);
    /** 每线程池默认最大容量（4096）。 */
    private static final int DEFAULT_INITIAL_MAX_CAPACITY_PER_THREAD = 4 * 1024; // Use 4k instances as default.
    /** 系统属性解析后的每线程最大容量。 */
    private static final int DEFAULT_MAX_CAPACITY_PER_THREAD;
    /** 采样比率：每 RATIO 次分配才允许一次入池，平滑扩容。 */
    private static final int RATIO;
    /** 本地 batch 数组块大小。 */
    private static final int DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD;
    /** 是否使用同步 BlockingMessageQueue（调试用）。 */
    private static final boolean BLOCKING_POOL;
    /** 是否仅对 FastThreadLocalThread 启用 batch 优化。 */
    private static final boolean BATCH_FAST_TL_ONLY;

    /** 从系统属性加载 recycler 全局配置。 */
    static {
        // In the future, we might have different maxCapacity for different object types.
        // e.g. io.netty.recycler.maxCapacity.writeTask
        //      io.netty.recycler.maxCapacity.outboundBuffer
        int maxCapacityPerThread = SystemPropertyUtil.getInt("io.netty.recycler.maxCapacityPerThread",
                SystemPropertyUtil.getInt("io.netty.recycler.maxCapacity", DEFAULT_INITIAL_MAX_CAPACITY_PER_THREAD));
        if (maxCapacityPerThread < 0) {
            maxCapacityPerThread = DEFAULT_INITIAL_MAX_CAPACITY_PER_THREAD;
        }

        DEFAULT_MAX_CAPACITY_PER_THREAD = maxCapacityPerThread;
        DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD = SystemPropertyUtil.getInt("io.netty.recycler.chunkSize", 32);

        // By default, we allow one push to a Recycler for each 8th try on handles that were never recycled before.
        // This should help to slowly increase the capacity of the recycler while not be too sensitive to allocation
        // bursts.
        RATIO = max(0, SystemPropertyUtil.getInt("io.netty.recycler.ratio", 8));

        BLOCKING_POOL = SystemPropertyUtil.getBoolean("io.netty.recycler.blocking", false);
        BATCH_FAST_TL_ONLY = SystemPropertyUtil.getBoolean("io.netty.recycler.batchFastThreadLocalOnly", true);

        if (logger.isDebugEnabled()) {
            if (DEFAULT_MAX_CAPACITY_PER_THREAD == 0) {
                logger.debug("-Dio.netty.recycler.maxCapacityPerThread: disabled");
                logger.debug("-Dio.netty.recycler.ratio: disabled");
                logger.debug("-Dio.netty.recycler.chunkSize: disabled");
                logger.debug("-Dio.netty.recycler.blocking: disabled");
                logger.debug("-Dio.netty.recycler.batchFastThreadLocalOnly: disabled");
            } else {
                logger.debug("-Dio.netty.recycler.maxCapacityPerThread: {}", DEFAULT_MAX_CAPACITY_PER_THREAD);
                logger.debug("-Dio.netty.recycler.ratio: {}", RATIO);
                logger.debug("-Dio.netty.recycler.chunkSize: {}", DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
                logger.debug("-Dio.netty.recycler.blocking: {}", BLOCKING_POOL);
                logger.debug("-Dio.netty.recycler.batchFastThreadLocalOnly: {}", BATCH_FAST_TL_ONLY);
            }
        }
    }

    /** 非 TLS 模式下的共享/绑定池；TLS 模式下为 null。 */
    private final LocalPool<?, T> localPool;
    /** TLS 模式下每线程 LocalPool；非 TLS 时为 null。 */
    private final FastThreadLocal<LocalPool<?, T>> threadLocalPool;

    /**
     * USE IT CAREFULLY!<br>
     * This is creating a shareable {@link Recycler} which {@code get()} can be called concurrently from different
     * {@link Thread}s.<br>
     * Usually {@link Recycler}s uses some form of thread-local storage, but this constructor is disabling it
     * and using a single pool of instances instead, sized as {@code maxCapacity}<br>
     * This is NOT enforcing pooled instances states to be validated if {@code unguarded = true}:
     * it means that {@link Handle#recycle(Object)} is not checking that {@code object} is the same which was
     * recycled and assume no other recycling happens concurrently
     * (similar to what {@link EnhancedHandle#unguardedRecycle(Object)} does).<br>
     */
    /**
     * 构造跨线程共享池（禁用 TLS）。
     * @see #Recycler(int, boolean)
     */
    protected Recycler(int maxCapacity, boolean unguarded) {
        if (maxCapacity <= 0) {
            maxCapacity = 0;
        } else {
            maxCapacity = max(4, maxCapacity);
        }
        threadLocalPool = null;
        if (maxCapacity == 0) {
            localPool = (LocalPool<?, T>) NOOP_LOCAL_POOL;
        } else {
            localPool = unguarded? new UnguardedLocalPool<>(maxCapacity) : new GuardedLocalPool<>(maxCapacity);
        }
    }

    /**
     * USE IT CAREFULLY!<br>
     * This is NOT enforcing pooled instances states to be validated if {@code unguarded = true}:
     * it means that {@link Handle#recycle(Object)} is not checking that {@code object} is the same which was
     * recycled and assume no other recycling happens concurrently
     * (similar to what {@link EnhancedHandle#unguardedRecycle(Object)} does).<br>
     */
    protected Recycler(boolean unguarded) {
        this(DEFAULT_MAX_CAPACITY_PER_THREAD, RATIO, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD, unguarded);
    }

    /**
     * USE IT CAREFULLY!<br>
     * This is NOT enforcing pooled instances states to be validated if {@code unguarded = true} as stated by
     * {@link #Recycler(boolean)} and allows to pin the recycler to a specific {@link Thread}, if {@code owner}
     * is not {@code null}.
     * <p>
     * Since this method has been introduced for performance-sensitive cases it doesn't validate if {@link #get()} is
     * called from the {@code owner} {@link Thread}: it assumes {@link #get()} to never happen concurrently.
     * <p>
     */
    protected Recycler(Thread owner, boolean unguarded) {
        this(DEFAULT_MAX_CAPACITY_PER_THREAD, RATIO, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD, owner, unguarded);
    }

    protected Recycler(int maxCapacityPerThread) {
        this(maxCapacityPerThread, RATIO, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
    }

    protected Recycler() {
        this(DEFAULT_MAX_CAPACITY_PER_THREAD);
    }

    /**
     * USE IT CAREFULLY!<br>
     * This is NOT enforcing pooled instances states to be validated if {@code unguarded = true} as stated by
     * {@link #Recycler(boolean)}, but it allows to tune the chunk size used for local pooling.
     */
    protected Recycler(int chunksSize, int maxCapacityPerThread, boolean unguarded) {
        this(maxCapacityPerThread, RATIO, chunksSize, unguarded);
    }

    /**
     * USE IT CAREFULLY!<br>
     * This is NOT enforcing pooled instances states to be validated if {@code unguarded = true} and allows pinning
     * the recycler to a specific {@link Thread}, as stated by {@link #Recycler(Thread, boolean)}.<br>
     * It also allows tuning the chunk size used for local pooling and the max capacity per thread.
     *
     * @throws IllegalArgumentException if {@code owner} is {@code null}.
     */
    protected Recycler(int chunkSize, int maxCapacityPerThread, Thread owner, boolean unguarded) {
        this(maxCapacityPerThread, RATIO, chunkSize, owner, unguarded);
    }

    /**
     * @deprecated Use one of the following instead:
     * {@link #Recycler()}, {@link #Recycler(int)}, {@link #Recycler(int, int, int)}.
     */
    @Deprecated
    @SuppressWarnings("unused") // Parameters we can't remove due to compatibility.
    protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor) {
        this(maxCapacityPerThread, RATIO, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
    }

    /**
     * @deprecated Use one of the following instead:
     * {@link #Recycler()}, {@link #Recycler(int)}, {@link #Recycler(int, int, int)}.
     */
    @Deprecated
    @SuppressWarnings("unused") // Parameters we can't remove due to compatibility.
    protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor,
                       int ratio, int maxDelayedQueuesPerThread) {
        this(maxCapacityPerThread, ratio, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
    }

    /**
     * @deprecated Use one of the following instead:
     * {@link #Recycler()}, {@link #Recycler(int)}, {@link #Recycler(int, int, int)}.
     */
    @Deprecated
    @SuppressWarnings("unused") // Parameters we can't remove due to compatibility.
    protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor,
                       int ratio, int maxDelayedQueuesPerThread, int delayedQueueRatio) {
        this(maxCapacityPerThread, ratio, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
    }

    protected Recycler(int maxCapacityPerThread, int interval, int chunkSize) {
        this(maxCapacityPerThread, interval, chunkSize, true, null, false);
    }

    /**
     * USE IT CAREFULLY!<br>
     * This is NOT enforcing pooled instances states to be validated if {@code unguarded =true}
     * as stated by {@link #Recycler(boolean)}.
     */
    protected Recycler(int maxCapacityPerThread, int interval, int chunkSize, boolean unguarded) {
        this(maxCapacityPerThread, interval, chunkSize, true, null, unguarded);
    }

    /**
     * USE IT CAREFULLY!<br>
     * This is NOT enforcing pooled instances states to be validated if {@code unguarded =true}
     * as stated by {@link #Recycler(boolean)}.
     */
    protected Recycler(int maxCapacityPerThread, int interval, int chunkSize, Thread owner, boolean unguarded) {
        this(maxCapacityPerThread, interval, chunkSize, false, owner, unguarded);
    }

    @SuppressWarnings("unchecked")
    private Recycler(int maxCapacityPerThread, int ratio, int chunkSize, boolean useThreadLocalStorage,
                     Thread owner, boolean unguarded) {
        final int interval = max(0, ratio);
        if (maxCapacityPerThread <= 0) {
            maxCapacityPerThread = 0;
            chunkSize = 0;
        } else {
            maxCapacityPerThread = max(4, maxCapacityPerThread);
            chunkSize = max(2, min(chunkSize, maxCapacityPerThread >> 1));
        }
        if (maxCapacityPerThread > 0 && useThreadLocalStorage) {
            final int finalMaxCapacityPerThread = maxCapacityPerThread;
            final int finalChunkSize = chunkSize;
            threadLocalPool = new FastThreadLocal<LocalPool<?, T>>() {
                @Override
                protected LocalPool<?, T> initialValue() {
                    return unguarded? new UnguardedLocalPool<>(finalMaxCapacityPerThread, interval, finalChunkSize) :
                            new GuardedLocalPool<>(finalMaxCapacityPerThread, interval, finalChunkSize);
                }

                @Override
                protected void onRemoval(LocalPool<?, T> value) throws Exception {
                    super.onRemoval(value);
                    MessagePassingQueue<?> handles = value.pooledHandles;
                    value.pooledHandles = null;
                    value.owner = null;
                    if (handles != null) {
                        handles.clear();
                    }
                }
            };
            localPool = null;
        } else {
            threadLocalPool = null;
            if (maxCapacityPerThread == 0) {
                localPool = (LocalPool<?, T>) NOOP_LOCAL_POOL;
            } else {
                Objects.requireNonNull(owner, "owner");
                localPool = unguarded? new UnguardedLocalPool<>(owner, maxCapacityPerThread, interval, chunkSize) :
                        new GuardedLocalPool<>(owner, maxCapacityPerThread, interval, chunkSize);
            }
        }
    }

    /** 从池中获取或新建对象；TLS/共享池分支在此选择。 */
    @SuppressWarnings("unchecked")
    public final T get() {
        if (localPool != null) {
            return localPool.getWith(this);
        } else {
            if (!FastThreadLocalThread.currentThreadWillCleanupFastThreadLocals()) {
                return newObject((Handle<T>) NOOP_HANDLE);
            }
            return threadLocalPool.get().getWith(this);
        }
    }

    /**
     * Disassociates the {@link Recycler} from the current {@link Thread} if it was pinned,
     * see {@link #Recycler(Thread, boolean)}.
     * <p>
     * Be aware that this method is not thread-safe: it's necessary to allow a {@link Thread} to
     * be garbage collected even if {@link Handle}s are still referenced by other objects.
     * <p>
     */
    /** 解除 owner 绑定，避免 Thread 无法 GC（非线程安全）。 */
    public static void unpinOwner(Recycler<?> recycler) {
        if (recycler.localPool != null) {
            recycler.localPool.owner = null;
        }
    }

    /**
     * @deprecated use {@link Handle#recycle(Object)}.
     */
    @Deprecated
    public final boolean recycle(T o, Handle<T> handle) {
        if (handle == NOOP_HANDLE) {
            return false;
        }

        handle.recycle(o);
        return true;
    }

    @VisibleForTesting
    final int threadLocalSize() {
        if (localPool != null) {
            return localPool.size();
        } else {
            if (!FastThreadLocalThread.currentThreadWillCleanupFastThreadLocals()) {
                return 0;
            }
            final LocalPool<?, T> pool = threadLocalPool.getIfExists();
            if (pool == null) {
                return 0;
            }
            return pool.size();
        }
    }

    /**
     * @param handle can NOT be null.
     */
    /** 池无可用实例时由子类创建新对象并绑定 Handle。 */
    protected abstract T newObject(Handle<T> handle);

    @SuppressWarnings("ClassNameSameAsAncestorName") // Can't change this due to compatibility.
    public interface Handle<T> extends ObjectPool.Handle<T>  { }

    /** 扩展 Handle，提供无守卫的 {@link #unguardedRecycle(Object)}。 */
    @UnstableApi
    public abstract static class EnhancedHandle<T> implements Handle<T> {

        public abstract void unguardedRecycle(Object object);

        private EnhancedHandle() {
        }
    }

    /** Guarded 模式下的 Handle：CLAIMED/AVAILABLE 状态机防止重复 recycle。 */
    private static final class DefaultHandle<T> extends EnhancedHandle<T> {
        /** 已借出。 */
        private static final int STATE_CLAIMED = 0;
        /** 在池中可用。 */
        private static final int STATE_AVAILABLE = 1;
        private static final AtomicIntegerFieldUpdater<DefaultHandle<?>> STATE_UPDATER;
        static {
            AtomicIntegerFieldUpdater<?> updater = AtomicIntegerFieldUpdater.newUpdater(DefaultHandle.class, "state");
            //noinspection unchecked
            STATE_UPDATER = (AtomicIntegerFieldUpdater<DefaultHandle<?>>) updater;
        }

        private volatile int state; // State is initialised to STATE_CLAIMED (aka. 0) so they can be released.
        private final GuardedLocalPool<T> localPool;
        private T value;

        DefaultHandle(GuardedLocalPool<T> localPool) {
            this.localPool = localPool;
        }

        @Override
        public void recycle(Object object) {
            if (object != value) {
                throw new IllegalArgumentException("object does not belong to handle");
            }
            toAvailable();
            localPool.release(this);
        }

        @Override
        public void unguardedRecycle(Object object) {
            if (object != value) {
                throw new IllegalArgumentException("object does not belong to handle");
            }
            unguardedToAvailable();
            localPool.release(this);
        }

        /** 从 AVAILABLE 转为 CLAIMED 并返回托管对象。 */
        T claim() {
            assert state == STATE_AVAILABLE;
            STATE_UPDATER.lazySet(this, STATE_CLAIMED);
            return value;
        }

        void set(T value) {
            this.value = value;
        }

        /** 原子置 AVAILABLE，重复 recycle 抛异常。 */
        private void toAvailable() {
            int prev = STATE_UPDATER.getAndSet(this, STATE_AVAILABLE);
            if (prev == STATE_AVAILABLE) {
                throw new IllegalStateException("Object has been recycled already.");
            }
        }

        private void unguardedToAvailable() {
            int prev = state;
            if (prev == STATE_AVAILABLE) {
                throw new IllegalStateException("Object has been recycled already.");
            }
            STATE_UPDATER.lazySet(this, STATE_AVAILABLE);
        }
    }

    /** 带状态校验的本地池，Handle 为 {@link DefaultHandle}。 */
    private static final class GuardedLocalPool<T> extends LocalPool<DefaultHandle<T>, T> {
        static {
            // Eagerly initiate DefaultHandle class-loading.
            int ignore = DefaultHandle.STATE_AVAILABLE;
        }

        GuardedLocalPool(int maxCapacity) {
            super(maxCapacity);
        }

        GuardedLocalPool(Thread owner, int maxCapacity, int ratioInterval, int chunkSize) {
            super(owner, maxCapacity, ratioInterval, chunkSize);
        }

        GuardedLocalPool(int maxCapacity, int ratioInterval, int chunkSize) {
            super(maxCapacity, ratioInterval, chunkSize);
        }

        @Override
        public T getWith(Recycler<T> recycler) {
            DefaultHandle<T> handle = acquire();
            T obj;
            if (handle == null) {
                handle = canAllocatePooled()? new DefaultHandle<>(this) : null;
                if (handle != null) {
                    obj = recycler.newObject(handle);
                    handle.set(obj);
                } else {
                    obj = recycler.newObject((Handle<T>) NOOP_HANDLE);
                }
            } else {
                obj = handle.claim();
            }
            return obj;
        }
    }

    /** 无守卫本地池：直接池化 T 实例，recycle 走 {@link LocalPoolHandle}。 */
    private static final class UnguardedLocalPool<T> extends LocalPool<T, T> {
        private final EnhancedHandle<T> handle;

        UnguardedLocalPool(int maxCapacity) {
            super(maxCapacity);
            handle = maxCapacity == 0? null : new LocalPoolHandle<>(this);
        }

        UnguardedLocalPool(Thread owner, int maxCapacity, int ratioInterval, int chunkSize) {
            super(owner, maxCapacity, ratioInterval, chunkSize);
            handle = new LocalPoolHandle<>(this);
        }

        UnguardedLocalPool(int maxCapacity, int ratioInterval, int chunkSize) {
            super(maxCapacity, ratioInterval, chunkSize);
            handle = new LocalPoolHandle<>(this);
        }

        @Override
        public T getWith(Recycler<T> recycler) {
            T obj = acquire();
            if (obj == null) {
                obj = recycler.newObject(canAllocatePooled()? handle : (Handle<T>) NOOP_HANDLE);
            }
            return obj;
        }
    }

    /**
     * 本地对象池核心：thread-local batch 栈 + 外部 MPSC/MPMC 队列。
     * acquire 优先 pop batch，release 优先 push batch（同 owner 线程），否则 offer 到外部队列。
     */
    private abstract static class LocalPool<H, T> {
        private final int ratioInterval;
        private final H[] batch;
        private int batchSize;
        private Thread owner;
        private MessagePassingQueue<H> pooledHandles;
        private int ratioCounter;

        LocalPool(int maxCapacity) {
            // if there's no capacity, we need to never allocate pooled objects.
            // if there's capacity, because there is a shared pool, we always pool them, since we cannot trust the
            // thread unsafe ratio counter.
            this.ratioInterval = maxCapacity == 0? -1 : 0;
            this.owner = null;
            batch = null;
            batchSize = 0;
            pooledHandles = createExternalMcPool(maxCapacity);
            ratioCounter = 0;
        }

        @SuppressWarnings("unchecked")
        LocalPool(Thread owner, int maxCapacity, int ratioInterval, int chunkSize) {
            this.ratioInterval = ratioInterval;
            this.owner = owner;
            batch = owner != null? (H[]) new Object[chunkSize] : null;
            batchSize = 0;
            pooledHandles = createExternalScPool(chunkSize, maxCapacity);
            ratioCounter = ratioInterval; // Start at interval so the first one will be recycled.
        }

        private static <H> MessagePassingQueue<H> createExternalMcPool(int maxCapacity) {
            if (maxCapacity == 0) {
                return null;
            }
            if (BLOCKING_POOL) {
                return new BlockingMessageQueue<>(maxCapacity);
            }
            return (MessagePassingQueue<H>) newFixedMpmcQueue(maxCapacity);
        }

        private static <H> MessagePassingQueue<H> createExternalScPool(int chunkSize, int maxCapacity) {
            if (maxCapacity == 0) {
                return null;
            }
            if (BLOCKING_POOL) {
                return new BlockingMessageQueue<>(maxCapacity);
            }
            return (MessagePassingQueue<H>) newMpscQueue(chunkSize, maxCapacity);
        }

        LocalPool(int maxCapacity, int ratioInterval, int chunkSize) {
            this(!BATCH_FAST_TL_ONLY || FastThreadLocalThread.currentThreadWillCleanupFastThreadLocals()
                         ? Thread.currentThread() : null, maxCapacity, ratioInterval, chunkSize);
        }

        /** 从 batch 或外部队列取一个 Handle/对象。 */
        protected final H acquire() {
            int size = batchSize;
            if (size == 0) {
                // it's ok to be racy; at worst we reuse something that won't return back to the pool
                final MessagePassingQueue<H> handles = pooledHandles;
                if (handles == null) {
                    return null;
                }
                return handles.relaxedPoll();
            }
            int top = size - 1;
            final H h = batch[top];
            batchSize = top;
            batch[top] = null;
            return h;
        }

        /** 归还到 batch 或外部队列；owner 已终止则丢弃池引用。 */
        protected final void release(H handle) {
            Thread owner = this.owner;
            if (owner != null && Thread.currentThread() == owner && batchSize < batch.length) {
                batch[batchSize] = handle;
                batchSize++;
            } else if (owner != null && isTerminated(owner)) {
                pooledHandles = null;
                this.owner = null;
            } else {
                MessagePassingQueue<H> handles = pooledHandles;
                if (handles != null) {
                    handles.relaxedOffer(handle);
                }
            }
        }

        /** J9 上用 isAlive 代替 getState 以避免性能问题。 */
        private static boolean isTerminated(Thread owner) {
            // Do not use `Thread.getState()` in J9 JVM because it's known to have a performance issue.
            // See: https://github.com/netty/netty/issues/13347#issuecomment-1518537895
            return PlatformDependent.isJ9Jvm()? !owner.isAlive() : owner.getState() == Thread.State.TERMINATED;
        }

        /** 按 ratioInterval 采样决定是否分配可池化的新 Handle/对象。 */
        boolean canAllocatePooled() {
            if (ratioInterval < 0) {
                return false;
            }
            if (ratioInterval == 0) {
                return true;
            }
            if (++ratioCounter >= ratioInterval) {
                ratioCounter = 0;
                return true;
            }
            return false;
        }

        abstract T getWith(Recycler<T> recycler);

        int size() {
            MessagePassingQueue<H> handles = pooledHandles;
            final int externalSize = handles != null? handles.size() : 0;
            return externalSize + (batch != null? batchSize : 0);
        }
    }

    /**
     * This is an implementation of {@link MessagePassingQueue}, similar to what might be returned from
     * {@link PlatformDependent#newMpscQueue(int)}, but intended to be used for debugging purpose.
     * The implementation relies on synchronised monitor locks for thread-safety.
     * The {@code fill} bulk operation is not supported by this implementation.
     */
    /** 调试用同步 MPMC 队列，基于 {@link ArrayDeque} + synchronized。 */
    private static final class BlockingMessageQueue<T> implements MessagePassingQueue<T> {
        private final Queue<T> deque;
        private final int maxCapacity;

        BlockingMessageQueue(int maxCapacity) {
            this.maxCapacity = maxCapacity;
            // This message passing queue is backed by an ArrayDeque instance,
            // made thread-safe by synchronising on `this` BlockingMessageQueue instance.
            // Why ArrayDeque?
            // We use ArrayDeque instead of LinkedList or LinkedBlockingQueue because it's more space efficient.
            // We use ArrayDeque instead of ArrayList because we need the queue APIs.
            // We use ArrayDeque instead of ConcurrentLinkedQueue because CLQ is unbounded and has O(n) size().
            // We use ArrayDeque instead of ArrayBlockingQueue because ABQ allocates its max capacity up-front,
            // and these queues will usually have large capacities, in potentially great numbers (one per thread),
            // but often only have comparatively few items in them.
            deque = new ArrayDeque<T>();
        }

        @Override
        public synchronized boolean offer(T e) {
            if (deque.size() == maxCapacity) {
                return false;
            }
            return deque.offer(e);
        }

        @Override
        public synchronized T poll() {
            return deque.poll();
        }

        @Override
        public synchronized T peek() {
            return deque.peek();
        }

        @Override
        public synchronized int size() {
            return deque.size();
        }

        @Override
        public synchronized void clear() {
            deque.clear();
        }

        @Override
        public synchronized boolean isEmpty() {
            return deque.isEmpty();
        }

        @Override
        public int capacity() {
            return maxCapacity;
        }

        @Override
        public boolean relaxedOffer(T e) {
            return offer(e);
        }

        @Override
        public T relaxedPoll() {
            return poll();
        }

        @Override
        public T relaxedPeek() {
            return peek();
        }

        @Override
        public int drain(Consumer<T> c, int limit) {
            T obj;
            int i = 0;
            for (; i < limit && (obj = poll()) != null; i++) {
                c.accept(obj);
            }
            return i;
        }

        @Override
        public int fill(Supplier<T> s, int limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int drain(Consumer<T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int fill(Supplier<T> s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void drain(Consumer<T> c, WaitStrategy wait, ExitCondition exit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void fill(Supplier<T> s, WaitStrategy wait, ExitCondition exit) {
            throw new UnsupportedOperationException();
        }
    }
}
