/*
* Copyright 2014 The Netty Project
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
package io.netty.util.concurrent;

import io.netty.util.internal.InternalThreadLocalMap;
import io.netty.util.internal.LongLongHashMap;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A special {@link Thread} that provides fast access to {@link FastThreadLocal} variables.
 *
 * <p>专用于 {@link FastThreadLocal} 快路径的线程类型：内嵌 {@link InternalThreadLocalMap} 引用，
 * 避免普通 {@link ThreadLocal} 的哈希查找。{@link DefaultThreadFactory} 默认创建此类线程。</p>
 */
public class FastThreadLocalThread extends Thread {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(FastThreadLocalThread.class);

    /**
     * Set of thread IDs that are treated like {@link FastThreadLocalThread}.
     *
     * <p>无法继承本类但需 FastThreadLocal 支持的线程 ID 集合（如虚拟线程 fallback）。</p>
     */
    private static final AtomicReference<FallbackThreadSet> fallbackThreads =
            new AtomicReference<>(FallbackThreadSet.EMPTY);

    // 若 Runnable 经 FastThreadLocalRunnable 包装，run 结束时会清理 FastThreadLocal
    // This will be set to true if we have a chance to wrap the Runnable.
    private final boolean cleanupFastThreadLocals;

    /** 线程绑定的 InternalThreadLocalMap，快路径直接索引访问。 */
    private InternalThreadLocalMap threadLocalMap;

    public FastThreadLocalThread() {
        cleanupFastThreadLocals = false;
    }

    public FastThreadLocalThread(Runnable target) {
        super(FastThreadLocalRunnable.wrap(target));
        cleanupFastThreadLocals = true;
    }

    public FastThreadLocalThread(ThreadGroup group, Runnable target) {
        super(group, FastThreadLocalRunnable.wrap(target));
        cleanupFastThreadLocals = true;
    }

    public FastThreadLocalThread(String name) {
        super(name);
        cleanupFastThreadLocals = false;
    }

    public FastThreadLocalThread(ThreadGroup group, String name) {
        super(group, name);
        cleanupFastThreadLocals = false;
    }

    public FastThreadLocalThread(Runnable target, String name) {
        super(FastThreadLocalRunnable.wrap(target), name);
        cleanupFastThreadLocals = true;
    }

    public FastThreadLocalThread(ThreadGroup group, Runnable target, String name) {
        super(group, FastThreadLocalRunnable.wrap(target), name);
        cleanupFastThreadLocals = true;
    }

    public FastThreadLocalThread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, FastThreadLocalRunnable.wrap(target), name, stackSize);
        cleanupFastThreadLocals = true;
    }

    /**
     * Returns the internal data structure that keeps the thread-local variables bound to this thread.
     * Note that this method is for internal use only, and thus is subject to change at any time.
     *
     * <p>返回线程绑定的 {@link InternalThreadLocalMap}；仅内部使用，跨线程访问会告警。</p>
     */
    public final InternalThreadLocalMap threadLocalMap() {
        if (this != Thread.currentThread() && logger.isWarnEnabled()) {
            logger.warn(new RuntimeException("It's not thread-safe to get 'threadLocalMap' " +
                    "which doesn't belong to the caller thread"));
        }
        return threadLocalMap;
    }

    /**
     * Sets the internal data structure that keeps the thread-local variables bound to this thread.
     * Note that this method is for internal use only, and thus is subject to change at any time.
     *
     * <p>设置线程的 {@link InternalThreadLocalMap}；仅内部使用。</p>
     */
    public final void setThreadLocalMap(InternalThreadLocalMap threadLocalMap) {
        if (this != Thread.currentThread() && logger.isWarnEnabled()) {
            logger.warn(new RuntimeException("It's not thread-safe to set 'threadLocalMap' " +
                    "which doesn't belong to the caller thread"));
        }
        this.threadLocalMap = threadLocalMap;
    }

    /**
     * Returns {@code true} if {@link FastThreadLocal#removeAll()} will be called once {@link #run()} completes.
     *
     * @deprecated Use {@link FastThreadLocalThread#currentThreadWillCleanupFastThreadLocals()} instead
     *
     * <p>实例方法：本线程 run 完成后是否会自动清理 FastThreadLocal。</p>
     */
    @Deprecated
    public boolean willCleanupFastThreadLocals() {
        return cleanupFastThreadLocals;
    }

    /**
     * Returns {@code true} if {@link FastThreadLocal#removeAll()} will be called once {@link Thread#run()} completes.
     *
     * @deprecated Use {@link FastThreadLocalThread#currentThreadWillCleanupFastThreadLocals()} instead
     */
    @Deprecated
    public static boolean willCleanupFastThreadLocals(Thread thread) {
        return thread instanceof FastThreadLocalThread &&
                ((FastThreadLocalThread) thread).willCleanupFastThreadLocals();
    }

    /**
     * Returns {@code true} if {@link FastThreadLocal#removeAll()} will be called once {@link Thread#run()} completes.
     *
     * <p>当前线程 run 结束是否会清理 FastThreadLocal（含 fallback 虚拟线程路径）。</p>
     */
    public static boolean currentThreadWillCleanupFastThreadLocals() {
        // intentionally doesn't accept a thread parameter to work with ScopedValue in the future
        Thread currentThread = currentThread();
        if (currentThread instanceof FastThreadLocalThread) {
            return ((FastThreadLocalThread) currentThread).willCleanupFastThreadLocals();
        }
        return isFastThreadLocalVirtualThread();
    }

    /**
     * Returns {@code true} if this thread supports {@link FastThreadLocal}.
     *
     * <p>当前线程是否支持 FastThreadLocal 快路径（真实 FastThreadLocalThread 或 fallback 注册线程）。</p>
     */
    public static boolean currentThreadHasFastThreadLocal() {
        // intentionally doesn't accept a thread parameter to work with ScopedValue in the future
        return currentThread() instanceof FastThreadLocalThread || isFastThreadLocalVirtualThread();
    }

    /** 当前线程是否在 fallback 集合中（通过 {@link #runWithFastThreadLocal} 注册）。 */
    private static boolean isFastThreadLocalVirtualThread() {
        return fallbackThreads.get().contains(currentThread().getId());
    }

    /**
     * Run the given task with {@link FastThreadLocal} support. This call should wrap the runnable for any thread that
     * is long-running enough to make treating it as a {@link FastThreadLocalThread} reasonable, but that can't
     * actually extend this class (e.g. because it's a virtual thread). Netty will use optimizations for recyclers and
     * allocators as if this was a {@link FastThreadLocalThread}.
     * <p>This method will clean up any {@link FastThreadLocal}s at the end, and
     * {@link #currentThreadWillCleanupFastThreadLocals()} will return {@code true}.
     * <p>At the moment, {@link FastThreadLocal} uses normal {@link ThreadLocal} as the backing storage here, but in
     * the future this may be replaced with scoped values, if semantics can be preserved and performance is good.
     *
     * @param runnable The task to run
     *
     * <p>在无法继承本类的长生命周期线程（如虚拟线程）上启用 FastThreadLocal 支持；
     * 执行完毕自动 {@link FastThreadLocal#removeAll()}，并临时注册线程 ID 到 fallback 集合。</p>
     */
    public static void runWithFastThreadLocal(Runnable runnable) {
        Thread current = currentThread();
        if (current instanceof FastThreadLocalThread) {
            throw new IllegalStateException("Caller is a real FastThreadLocalThread");
        }
        long id = current.getId();
        fallbackThreads.updateAndGet(set -> {
            if (set.contains(id)) {
                throw new IllegalStateException("Reentrant call to run()");
            }
            return set.add(id);
        });

        try {
            runnable.run();
        } finally {
            fallbackThreads.getAndUpdate(set -> set.remove(id));
            FastThreadLocal.removeAll();
        }
    }

    /**
     * Query whether this thread is allowed to perform blocking calls or not.
     * {@link FastThreadLocalThread}s are often used in event-loops, where blocking calls are forbidden in order to
     * prevent event-loop stalls, so this method returns {@code false} by default.
     * <p>
     * Subclasses of {@link FastThreadLocalThread} can override this method if they are not meant to be used for
     * running event-loops.
     *
     * @return {@code false}, unless overriden by a subclass.
     *
     * <p>是否允许阻塞调用；EventLoop 线程默认 {@code false}，子类可覆盖。</p>
     */
    public boolean permitBlockingCalls() {
        return false;
    }

    /**
     * Immutable, thread-safe helper class that wraps {@link LongLongHashMap}
     *
     * <p>不可变位图集合：用 {@link LongLongHashMap} 按线程 ID 存储占用位，CAS 更新 fallback 集合。</p>
     */
    private static final class FallbackThreadSet {
        static final FallbackThreadSet EMPTY = new FallbackThreadSet();
        private static final long EMPTY_VALUE = 0L;

        private final LongLongHashMap map;

        private FallbackThreadSet() {
            this.map = new LongLongHashMap(EMPTY_VALUE);
        }

        private FallbackThreadSet(LongLongHashMap map) {
            this.map = map;
        }

        /** 线程 ID 是否在位图中。 */
        public boolean contains(long threadId) {
            long key = threadId >>> 6;
            long bit = 1L << (threadId & 63);

            long bitmap = map.get(key);
            return (bitmap & bit) != 0;
        }

        /** 返回加入 threadId 后的新不可变集合。 */
        public FallbackThreadSet add(long threadId) {
            long key = threadId >>> 6;
            long bit = 1L << (threadId & 63);

            LongLongHashMap newMap = new LongLongHashMap(map);
            long oldBitmap = newMap.get(key);
            long newBitmap = oldBitmap | bit;
            newMap.put(key, newBitmap);

            return new FallbackThreadSet(newMap);
        }

        /** 返回移除 threadId 后的新不可变集合。 */
        public FallbackThreadSet remove(long threadId) {
            long key = threadId >>> 6;
            long bit = 1L << (threadId & 63);

            long oldBitmap = map.get(key);
            if ((oldBitmap & bit) == 0) {
                return this;
            }

            LongLongHashMap newMap = new LongLongHashMap(map);
            long newBitmap = oldBitmap & ~bit;

            if (newBitmap != EMPTY_VALUE) {
                newMap.put(key, newBitmap);
            } else {
                newMap.remove(key);
            }

            return new FallbackThreadSet(newMap);
        }
    }
}
