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

package io.netty.util;

import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Checks if a thread is alive periodically and runs a task when a thread dies.
 * <p>
 * This thread starts a daemon thread to check the state of the threads being watched and to invoke their
 * associated {@link Runnable}s.  When there is no thread to watch (i.e. all threads are dead), the daemon thread
 * will terminate itself, and a new daemon thread will be started again when a new watch is added.
 * </p>
 *
 * @deprecated will be removed in the next major release
 *
 * <p>已废弃：后台守护线程每秒轮询被监视线程是否存活，死亡时执行关联 {@link Runnable}；
 * 主要用于 {@link ReferenceCountUtil#releaseLater(Object)} 单测场景，下一大版本将移除。</p>
 */
@Deprecated
public final class ThreadDeathWatcher {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ThreadDeathWatcher.class);
    // visible for testing
    /** 创建 Watcher 守护线程的工厂。 */
    static final ThreadFactory threadFactory;

    // 使用 MPMC 队列，因 isEmpty 可能由多线程并发调用
    // Use a MPMC queue as we may end up checking isEmpty() from multiple threads which may not be allowed to do
    // concurrently depending on the implementation of it in a MPSC queue.
    /** 待处理的 watch/unwatch 请求队列。 */
    private static final Queue<Entry> pendingEntries = new ConcurrentLinkedQueue<Entry>();
    /** 单例 Watcher 任务。 */
    private static final Watcher watcher = new Watcher();
    /** Watcher 线程是否已启动。 */
    private static final AtomicBoolean started = new AtomicBoolean();
    /** 当前 Watcher 守护线程引用。 */
    private static volatile Thread watcherThread;

    static {
        String poolName = "threadDeathWatcher";
        String serviceThreadPrefix = SystemPropertyUtil.get("io.netty.serviceThreadPrefix");
        if (!StringUtil.isNullOrEmpty(serviceThreadPrefix)) {
            poolName = serviceThreadPrefix + poolName;
        }
        // 单例 Watcher 可能从任意线程组提交任务，工厂不绑定特定 ThreadGroup
        // because the ThreadDeathWatcher is a singleton, tasks submitted to it can come from arbitrary threads and
        // this can trigger the creation of a thread from arbitrary thread groups; for this reason, the thread factory
        // must not be sticky about its thread group
        threadFactory = new DefaultThreadFactory(poolName, true, Thread.MIN_PRIORITY, null);
    }

    /**
     * Schedules the specified {@code task} to run when the specified {@code thread} dies.
     *
     * <p>注册监视：thread 终止后执行 task；thread 必须仍存活。</p>
     *
     * @param thread the {@link Thread} to watch
     * @param task the {@link Runnable} to run when the {@code thread} dies
     *
     * @throws IllegalArgumentException if the specified {@code thread} is not alive
     */
    public static void watch(Thread thread, Runnable task) {
        ObjectUtil.checkNotNull(thread, "thread");
        ObjectUtil.checkNotNull(task, "task");

        if (!thread.isAlive()) {
            throw new IllegalArgumentException("thread must be alive.");
        }

        schedule(thread, task, true);
    }

    /**
     * Cancels the task scheduled via {@link #watch(Thread, Runnable)}.
     *
     * <p>取消先前注册的 watch。</p>
     */
    public static void unwatch(Thread thread, Runnable task) {
        schedule(ObjectUtil.checkNotNull(thread, "thread"),
                ObjectUtil.checkNotNull(task, "task"),
                false);
    }

    /** 将 watch/unwatch 请求入队并懒启动 Watcher 线程。 */
    private static void schedule(Thread thread, Runnable task, boolean isWatch) {
        pendingEntries.add(new Entry(thread, task, isWatch));

        if (started.compareAndSet(false, true)) {
            final Thread watcherThread = threadFactory.newThread(watcher);
            // 清空 ContextClassLoader，避免类加载器泄漏
            // Set to null to ensure we not create classloader leaks by holds a strong reference to the inherited
            // classloader.
            // See:
            // - https://github.com/netty/netty/issues/7290
            // - https://bugs.openjdk.java.net/browse/JDK-7008595
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    watcherThread.setContextClassLoader(null);
                    return null;
                }
            });

            watcherThread.start();
            ThreadDeathWatcher.watcherThread = watcherThread;
        }
    }

    /**
     * Waits until the thread of this watcher has no threads to watch and terminates itself.
     * Because a new watcher thread will be started again on {@link #watch(Thread, Runnable)},
     * this operation is only useful when you want to ensure that the watcher thread is terminated
     * <strong>after</strong> your application is shut down and there's no chance of calling
     * {@link #watch(Thread, Runnable)} afterwards.
     *
     * <p>等待 Watcher 守护线程在无监视对象时自行退出；仅适用于应用关闭后不再 watch 的场景。</p>
     *
     * @return {@code true} if and only if the watcher thread has been terminated
     */
    public static boolean awaitInactivity(long timeout, TimeUnit unit) throws InterruptedException {
        ObjectUtil.checkNotNull(unit, "unit");

        Thread watcherThread = ThreadDeathWatcher.watcherThread;
        if (watcherThread != null) {
            watcherThread.join(unit.toMillis(timeout));
            return !watcherThread.isAlive();
        } else {
            return true;
        }
    }

    private ThreadDeathWatcher() { }

    /** 后台轮询：拉取 pending、检测 watchees 死亡并执行任务。 */
    private static final class Watcher implements Runnable {

        /** 当前正在监视的 (thread, task) 列表。 */
        private final List<Entry> watchees = new ArrayList<Entry>();

        @Override
        public void run() {
            for (;;) {
                fetchWatchees();
                notifyWatchees();

                // 再拉取一次，应对 notify 期间新增的 watch/unwatch
                // Try once again just in case notifyWatchees() triggered watch() or unwatch().
                fetchWatchees();
                notifyWatchees();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                    // 忽略中断，直到所有任务执行完毕
                    // Ignore the interrupt; do not terminate until all tasks are run.
                }

                if (watchees.isEmpty() && pendingEntries.isEmpty()) {

                    // Mark the current worker thread as stopped.
                    // The following CAS must always success and must be uncontended,
                    // because only one watcher thread should be running at the same time.
                    boolean stopped = started.compareAndSet(true, false);
                    assert stopped;

                    // Check if there are pending entries added by watch() while we do CAS above.
                    if (pendingEntries.isEmpty()) {
                        // A) watch() 未再被调用，安全退出
                        // B) 新 Watcher 已启动并处理完毕，本线程可退出
                        // A) watch() was not invoked and thus there's nothing to handle
                        //    -> safe to terminate because there's nothing left to do
                        // B) a new watcher thread started and handled them all
                        //    -> safe to terminate the new watcher thread will take care the rest
                        break;
                    }

                    // watch() 在 CAS 期间又添加了条目
                    // There are pending entries again, added by watch()
                    if (!started.compareAndSet(false, true)) {
                        // 已有新 Watcher 线程，本线程退出
                        // watch() started a new watcher thread and set 'started' to true.
                        // -> terminate this thread so that the new watcher reads from pendingEntries exclusively.
                        break;
                    }

                    // 本线程继续处理新条目
                    // watch() added an entry, but this worker was faster to set 'started' to true.
                    // i.e. a new watcher thread was not started
                    // -> keep this thread alive to handle the newly added entries.
                }
            }
        }

        /** 从 pendingEntries 合并到 watchees。 */
        private void fetchWatchees() {
            for (;;) {
                Entry e = pendingEntries.poll();
                if (e == null) {
                    break;
                }

                if (e.isWatch) {
                    watchees.add(e);
                } else {
                    watchees.remove(e);
                }
            }
        }

        /** 对已死亡线程执行 task 并从列表移除。 */
        private void notifyWatchees() {
            List<Entry> watchees = this.watchees;
            for (int i = 0; i < watchees.size();) {
                Entry e = watchees.get(i);
                if (!e.thread.isAlive()) {
                    watchees.remove(i);
                    try {
                        e.task.run();
                    } catch (Throwable t) {
                        logger.warn("Thread death watcher task raised an exception:", t);
                    }
                } else {
                    i ++;
                }
            }
        }
    }

    /** watch/unwatch 队列条目，equals 基于 thread 与 task 引用。 */
    private static final class Entry {
        final Thread thread;
        final Runnable task;
        final boolean isWatch;

        Entry(Thread thread, Runnable task, boolean isWatch) {
            this.thread = thread;
            this.task = task;
            this.isWatch = isWatch;
        }

        @Override
        public int hashCode() {
            return thread.hashCode() ^ task.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof Entry)) {
                return false;
            }

            Entry that = (Entry) obj;
            return thread == that.thread && task == that.task;
        }
    }
}
