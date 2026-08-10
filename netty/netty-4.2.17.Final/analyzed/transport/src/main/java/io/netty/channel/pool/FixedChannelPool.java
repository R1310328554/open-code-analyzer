/*
 * Copyright 2015 The Netty Project
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
package io.netty.channel.pool;

import static io.netty.util.internal.ObjectUtil.checkPositive;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ObjectUtil;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link ChannelPool} implementation that takes another {@link ChannelPool} implementation and enforce a maximum
 * number of concurrent connections.
 * <p>在 {@link SimpleChannelPool} 之上限制最大并发连接数；超出时 acquire 排队或超时处理。</p>
 */
public class FixedChannelPool extends SimpleChannelPool {

    /** acquire 等待超时时的行为 */
    public enum AcquireTimeoutAction {
        /**
         * Create a new connection when the timeout is detected.
         * <p>超时后仍尝试新建连接（不计入排队限制）。</p>
         */
        NEW,

        /**
         * Fail the {@link Future} of the acquire call with a {@link TimeoutException}.
         * <p>超时则使 acquire 的 Future 失败。</p>
         */
        FAIL
    }

    /** 串行化 acquire/release 与排队逻辑的 EventExecutor */
    private final EventExecutor executor;
    /** acquire 超时时间（纳秒），-1 表示不超时 */
    private final long acquireTimeoutNanos;
    /** 超时扫描任务（由 {@link AcquireTimeoutAction} 决定行为） */
    private final Runnable timeoutTask;

    // 队列与计数均由 executor 单线程修改，无需额外同步
    // There is no need to worry about synchronization as everything that modified the queue or counts is done
    // by the above EventExecutor.
    /** 等待可用连接槽位的 acquire 任务队列 */
    private final Queue<AcquireTask> pendingAcquireQueue = new ArrayDeque<AcquireTask>();
    /** 允许同时借出的最大连接数 */
    private final int maxConnections;
    /** 允许排队的最大 pending acquire 数 */
    private final int maxPendingAcquires;
    /** 当前已借出（尚未 release）的 channel 计数 */
    private final AtomicInteger acquiredChannelCount = new AtomicInteger();
    /** 当前排队中的 acquire 数量 */
    private int pendingAcquireCount;
    /** 池是否已关闭 */
    private boolean closed;

    /**
     * Creates a new instance using the {@link ChannelHealthChecker#ACTIVE}.
     * <p>使用 {@link ChannelHealthChecker#ACTIVE} 创建固定大小连接池。</p>
     *
     * @param bootstrap         the {@link Bootstrap} that is used for connections
     * @param handler           the {@link ChannelPoolHandler} that will be notified for the different pool actions
     * @param maxConnections    the number of maximal active connections, once this is reached new tries to acquire
     *                          a {@link Channel} will be delayed until a connection is returned to the pool again.
     */
    public FixedChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler, int maxConnections) {
        this(bootstrap, handler, maxConnections, Integer.MAX_VALUE);
    }

    /**
     * Creates a new instance using the {@link ChannelHealthChecker#ACTIVE}.
     * <p>指定最大连接数与最大排队 acquire 数。</p>
     *
     * @param bootstrap             the {@link Bootstrap} that is used for connections
     * @param handler               the {@link ChannelPoolHandler} that will be notified for the different pool actions
     * @param maxConnections        the number of maximal active connections, once this is reached new tries to
     *                              acquire a {@link Channel} will be delayed until a connection is returned to the
     *                              pool again.
     * @param maxPendingAcquires    the maximum number of pending acquires. Once this is exceed acquire tries will
     *                              be failed.
     */
    public FixedChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler, int maxConnections, int maxPendingAcquires) {
        this(bootstrap, handler, ChannelHealthChecker.ACTIVE, null, -1, maxConnections, maxPendingAcquires);
    }

    /**
     * Creates a new instance.
     *
     * @param bootstrap             the {@link Bootstrap} that is used for connections
     * @param handler               the {@link ChannelPoolHandler} that will be notified for the different pool actions
     * @param healthCheck           the {@link ChannelHealthChecker} that will be used to check if a {@link Channel} is
     *                              still healthy when obtain from the {@link ChannelPool}
     * @param action                the {@link AcquireTimeoutAction} to use or {@code null} if non should be used.
     *                              In this case {@param acquireTimeoutMillis} must be {@code -1}.
     * @param acquireTimeoutMillis  the time (in milliseconds) after which an pending acquire must complete or
     *                              the {@link AcquireTimeoutAction} takes place.
     * @param maxConnections        the number of maximal active connections, once this is reached new tries to
     *                              acquire a {@link Channel} will be delayed until a connection is returned to the
     *                              pool again.
     * @param maxPendingAcquires    the maximum number of pending acquires. Once this is exceed acquire tries will
     *                              be failed.
     */
    public FixedChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler,
                            ChannelHealthChecker healthCheck, AcquireTimeoutAction action,
                            final long acquireTimeoutMillis,
                            int maxConnections, int maxPendingAcquires) {
        this(bootstrap, handler, healthCheck, action, acquireTimeoutMillis, maxConnections, maxPendingAcquires, true);
    }

    /**
     * Creates a new instance.
     *
     * @param bootstrap             the {@link Bootstrap} that is used for connections
     * @param handler               the {@link ChannelPoolHandler} that will be notified for the different pool actions
     * @param healthCheck           the {@link ChannelHealthChecker} that will be used to check if a {@link Channel} is
     *                              still healthy when obtain from the {@link ChannelPool}
     * @param action                the {@link AcquireTimeoutAction} to use or {@code null} if non should be used.
     *                              In this case {@param acquireTimeoutMillis} must be {@code -1}.
     * @param acquireTimeoutMillis  the time (in milliseconds) after which an pending acquire must complete or
     *                              the {@link AcquireTimeoutAction} takes place.
     * @param maxConnections        the number of maximal active connections, once this is reached new tries to
     *                              acquire a {@link Channel} will be delayed until a connection is returned to the
     *                              pool again.
     * @param maxPendingAcquires    the maximum number of pending acquires. Once this is exceed acquire tries will
     *                              be failed.
     * @param releaseHealthCheck    will check channel health before offering back if this parameter set to
     *                              {@code true}.
     */
    public FixedChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler,
                            ChannelHealthChecker healthCheck, AcquireTimeoutAction action,
                            final long acquireTimeoutMillis,
                            int maxConnections, int maxPendingAcquires, final boolean releaseHealthCheck) {
        this(bootstrap, handler, healthCheck, action, acquireTimeoutMillis, maxConnections, maxPendingAcquires,
                releaseHealthCheck, true);
    }

    /**
     * Creates a new instance.
     *
     * @param bootstrap             the {@link Bootstrap} that is used for connections
     * @param handler               the {@link ChannelPoolHandler} that will be notified for the different pool actions
     * @param healthCheck           the {@link ChannelHealthChecker} that will be used to check if a {@link Channel} is
     *                              still healthy when obtain from the {@link ChannelPool}
     * @param action                the {@link AcquireTimeoutAction} to use or {@code null} if non should be used.
     *                              In this case {@param acquireTimeoutMillis} must be {@code -1}.
     * @param acquireTimeoutMillis  the time (in milliseconds) after which an pending acquire must complete or
     *                              the {@link AcquireTimeoutAction} takes place.
     * @param maxConnections        the number of maximal active connections, once this is reached new tries to
     *                              acquire a {@link Channel} will be delayed until a connection is returned to the
     *                              pool again.
     * @param maxPendingAcquires    the maximum number of pending acquires. Once this is exceed acquire tries will
     *                              be failed.
     * @param releaseHealthCheck    will check channel health before offering back if this parameter set to
     *                              {@code true}.
     * @param lastRecentUsed        {@code true} {@link Channel} selection will be LIFO, if {@code false} FIFO.
     */
    public FixedChannelPool(Bootstrap bootstrap,
                            ChannelPoolHandler handler,
                            ChannelHealthChecker healthCheck, AcquireTimeoutAction action,
                            final long acquireTimeoutMillis,
                            int maxConnections, int maxPendingAcquires,
                            boolean releaseHealthCheck, boolean lastRecentUsed) {
        super(bootstrap, handler, healthCheck, releaseHealthCheck, lastRecentUsed);
        checkPositive(maxConnections, "maxConnections");
        checkPositive(maxPendingAcquires, "maxPendingAcquires");
        if (action == null && acquireTimeoutMillis == -1) {
            timeoutTask = null;
            acquireTimeoutNanos = -1;
        } else if (action == null && acquireTimeoutMillis != -1) {
            throw new NullPointerException("action");
        } else if (action != null && acquireTimeoutMillis < 0) {
            throw new IllegalArgumentException("acquireTimeoutMillis: " + acquireTimeoutMillis + " (expected: >= 0)");
        } else {
            acquireTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(acquireTimeoutMillis);
            switch (action) {
            case FAIL:
                timeoutTask = new TimeoutTask() {
                    @Override
                    public void onTimeout(AcquireTask task) {
                        // Fail the promise as we timed out.
                        task.promise.setFailure(new AcquireTimeoutException());
                    }
                };
                break;
            case NEW:
                timeoutTask = new TimeoutTask() {
                    @Override
                    public void onTimeout(AcquireTask task) {
                        // Increment the acquire count and delegate to super to actually acquire a Channel which will
                        // create a new connection.
                        task.acquired();

                        FixedChannelPool.super.acquire(task.promise);
                    }
                };
                break;
            default:
                throw new Error("Unexpected AcquireTimeoutAction: " + action);
            }
        }
        executor = bootstrap.config().group().next();
        this.maxConnections = maxConnections;
        this.maxPendingAcquires = maxPendingAcquires;
    }

    /** Returns the number of acquired channels that this pool thinks it has.
     * <p>当前已借出、尚未归还的 channel 数量。</p> */
    public int acquiredChannelCount() {
        return acquiredChannelCount.get();
    }

    @Override
    public Future<Channel> acquire(final Promise<Channel> promise) {
        try {
            if (executor.inEventLoop()) {
                acquire0(promise);
            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        acquire0(promise);
                    }
                });
            }
        } catch (Throwable cause) {
            promise.tryFailure(cause);
        }
        return promise;
    }

    private void acquire0(final Promise<Channel> promise) {
        try {
            assert executor.inEventLoop();

            if (closed) {
                promise.setFailure(new IllegalStateException("FixedChannelPool was closed"));
                return;
            }
            if (acquiredChannelCount.get() < maxConnections) {
                assert acquiredChannelCount.get() >= 0;

                // 新建 Promise，确保 AcquireListener 在正确的 EventLoop 上运行
                // We need to create a new promise as we need to ensure the AcquireListener runs in the correct
                // EventLoop
                Promise<Channel> p = executor.newPromise();
                AcquireListener l = new AcquireListener(promise);
                l.acquired();
                p.addListener(l);
                super.acquire(p);
            } else {
                if (pendingAcquireCount >= maxPendingAcquires) {
                    tooManyOutstanding(promise);
                } else {
                    AcquireTask task = new AcquireTask(promise);
                    if (pendingAcquireQueue.offer(task)) {
                        ++pendingAcquireCount;

                        if (timeoutTask != null) {
                            task.timeoutFuture = executor.schedule(timeoutTask, acquireTimeoutNanos,
                                  TimeUnit.NANOSECONDS);
                        }
                    } else {
                        tooManyOutstanding(promise);
                    }
                }

                assert pendingAcquireCount > 0;
            }
        } catch (Throwable cause) {
            promise.tryFailure(cause);
        }
    }

    private void tooManyOutstanding(Promise<?> promise) {
        promise.setFailure(new IllegalStateException("Too many outstanding acquire operations"));
    }

    @Override
    public Future<Void> release(final Channel channel, final Promise<Void> promise) {
        ObjectUtil.checkNotNull(promise, "promise");
        final Promise<Void> p = executor.newPromise();
        super.release(channel, p.addListener((FutureListener<Void>) future -> {
            try {
                assert executor.inEventLoop();

                if (closed) {
                    // 池已关闭，只能关闭 channel
                    // Since the pool is closed, we have no choice but to close the channel
                    channel.close();
                    promise.setFailure(new IllegalStateException("FixedChannelPool was closed"));
                    return;
                }

                if (future.isSuccess()) {
                    decrementAndRunTaskQueue();
                    promise.setSuccess(null);
                } else {
                    Throwable cause = future.cause();
                    // 若非“归还给错误池”导致的异常，仍递减计数并唤醒排队任务
                    // Check if the exception was not because of we passed the Channel to the wrong pool.
                    if (!(cause instanceof IllegalArgumentException)) {
                        decrementAndRunTaskQueue();
                    }
                    promise.setFailure(future.cause());
                }
            } catch (Throwable cause) {
                promise.tryFailure(cause);
            }
        }));
        return promise;
    }

    private void decrementAndRunTaskQueue() {
        // 计数不应为负
        // We should never have a negative value.
        int currentCount = acquiredChannelCount.decrementAndGet();
        assert currentCount >= 0;

        // 先处理排队 acquire，再通知原 promise，避免 listener 内再次 acquire 时误判排队上限
        // Run the pending acquire tasks before notify the original promise so if the user would
        // try to acquire again from the ChannelFutureListener and the pendingAcquireCount is >=
        // maxPendingAcquires we may be able to run some pending tasks first and so allow to add
        // more.
        runTaskQueue();
    }

    private void runTaskQueue() {
        while (acquiredChannelCount.get() < maxConnections) {
            AcquireTask task = pendingAcquireQueue.poll();
            if (task == null) {
                break;
            }

            // 取消已调度的超时任务
            // Cancel the timeout if one was scheduled
            ScheduledFuture<?> timeoutFuture = task.timeoutFuture;
            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
            }

            --pendingAcquireCount;
            task.acquired();

            super.acquire(task.promise);
        }

        // We should never have a negative value.
        assert pendingAcquireCount >= 0;
        assert acquiredChannelCount.get() >= 0;
    }

    // AcquireTask 继承 AcquireListener 以减少对象分配与 GC 压力
    // AcquireTask extends AcquireListener to reduce object creations and so GC pressure
    private final class AcquireTask extends AcquireListener {
        final Promise<Channel> promise;
        final long expireNanoTime = System.nanoTime() + acquireTimeoutNanos;
        ScheduledFuture<?> timeoutFuture;

        AcquireTask(Promise<Channel> promise) {
            super(promise);
            // We need to create a new promise as we need to ensure the AcquireListener runs in the correct
            // EventLoop.
            this.promise = executor.<Channel>newPromise().addListener(this);
        }
    }

    private abstract class TimeoutTask implements Runnable {
        @Override
        public final void run() {
            assert executor.inEventLoop();
            long nanoTime = System.nanoTime();
            for (;;) {
                AcquireTask task = pendingAcquireQueue.peek();
                // 按 System.nanoTime() 语义比较过期时间（见 JDK 文档与 netty#3705）
                // Compare nanoTime as descripted in the javadocs of System.nanoTime()
                //
                // See https://docs.oracle.com/javase/7/docs/api/java/lang/System.html#nanoTime()
                // See https://github.com/netty/netty/issues/3705
                if (task == null || nanoTime - task.expireNanoTime < 0) {
                    break;
                }
                pendingAcquireQueue.remove();

                --pendingAcquireCount;
                onTimeout(task);
            }
        }

        public abstract void onTimeout(AcquireTask task);
    }

    private class AcquireListener implements FutureListener<Channel> {
        private final Promise<Channel> originalPromise;
        protected boolean acquired;

        AcquireListener(Promise<Channel> originalPromise) {
            this.originalPromise = originalPromise;
        }

        @Override
        public void operationComplete(Future<Channel> future) throws Exception {
            try {
                assert executor.inEventLoop();

                if (closed) {
                    if (future.isSuccess()) {
                        // 池已关闭，关闭刚 acquire 到的 channel
                        // Since the pool is closed, we have no choice but to close the channel
                        future.getNow().close();
                    }
                    originalPromise.setFailure(new IllegalStateException("FixedChannelPool was closed"));
                    return;
                }

                if (future.isSuccess()) {
                    originalPromise.setSuccess(future.getNow());
                } else {
                    if (acquired) {
                        decrementAndRunTaskQueue();
                    } else {
                        runTaskQueue();
                    }

                    originalPromise.setFailure(future.cause());
                }
            } catch (Throwable cause) {
                originalPromise.tryFailure(cause);
            }
        }

        public void acquired() {
            if (acquired) {
                return;
            }
            acquiredChannelCount.incrementAndGet();
            acquired = true;
        }
    }

    @Override
    public void close() {
        try {
            closeAsync().await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the pool in an async manner.
     * <p>异步关闭池：清空排队任务并在其他线程执行父类 close。</p>
     *
     * @return Future which represents completion of the close task
     */
    @Override
    public Future<Void> closeAsync() {
        if (executor.inEventLoop()) {
            return close0();
        } else {
            final Promise<Void> closeComplete = executor.newPromise();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    close0().addListener((FutureListener<Void>) f -> {
                        if (f.isSuccess()) {
                            closeComplete.setSuccess(null);
                        } else {
                            closeComplete.setFailure(f.cause());
                        }
                    });
                }
            });
            return closeComplete;
        }
    }

    private Future<Void> close0() {
        assert executor.inEventLoop();

        if (!closed) {
            closed = true;
            for (;;) {
                AcquireTask task = pendingAcquireQueue.poll();
                if (task == null) {
                    break;
                }
                ScheduledFuture<?> f = task.timeoutFuture;
                if (f != null) {
                    f.cancel(false);
                }
                task.promise.setFailure(new ClosedChannelException());
            }
            acquiredChannelCount.set(0);
            pendingAcquireCount = 0;

            // 在 GlobalEventExecutor 上执行 super.close()，避免在 EventExecutor 内阻塞
            // Ensure we dispatch this on another Thread as close0 will be called from the EventExecutor and we need
            // to ensure we will not block in a EventExecutor.
            return GlobalEventExecutor.INSTANCE.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    FixedChannelPool.super.close();
                    return null;
                }
            });
        }

        return GlobalEventExecutor.INSTANCE.newSucceededFuture(null);
    }

    private static final class AcquireTimeoutException extends TimeoutException {

        private AcquireTimeoutException() {
            super("Acquire operation took longer then configured maximum time");
        }

        // 无需同步，抑制 fillInStackTrace 警告
        // Suppress a warning since the method doesn't need synchronization
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}
