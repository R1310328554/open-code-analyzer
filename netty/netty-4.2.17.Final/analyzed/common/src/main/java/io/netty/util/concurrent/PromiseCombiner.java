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
package io.netty.util.concurrent;

import io.netty.util.internal.ObjectUtil;

/**
 * <p>A promise combiner monitors the outcome of a number of discrete futures, then notifies a final, aggregate promise
 * when all of the combined futures are finished. The aggregate promise will succeed if and only if all of the combined
 * futures succeed. If any of the combined futures fail, the aggregate promise will fail. The cause failure for the
 * aggregate promise will be the failure for one of the failed combined futures; if more than one of the combined
 * futures fails, exactly which cause of failure will be assigned to the aggregate promise is undefined.</p>
 *
 * <p>Callers may populate a promise combiner with any number of futures to be combined via the
 * {@link PromiseCombiner#add(Future)} and {@link PromiseCombiner#addAll(Future[])} methods. When all futures to be
 * combined have been added, callers must provide an aggregate promise to be notified when all combined promises have
 * finished via the {@link PromiseCombiner#finish(Promise)} method.</p>
 *
 * <p>This implementation is <strong>NOT</strong> thread-safe and all methods must be called
 * from the {@link EventExecutor} thread.</p>
 *
 * <p>Promise 组合器：监听多个 {@link Future}，全部成功则聚合 Promise 成功，任一失败则聚合 Promise 失败
 * （失败原因取自第一个观测到的失败，多个失败时具体取哪一个未定义）。须在同一 {@link EventExecutor} 线程上
 * 调用 {@link #add}、{@link #addAll} 与 {@link #finish}，非线程安全。</p>
 */
public final class PromiseCombiner {
    /** 期望完成的 Future 总数（add 时递增）。 */
    private int expectedCount;
    /** 已完成的 Future 数量。 */
    private int doneCount;
    /** finish 时指定的聚合 Promise；全部完成后通过 tryPromise 通知。 */
    private Promise<Void> aggregatePromise;
    /** 第一个观测到的失败原因，用于聚合 Promise 的 tryFailure。 */
    private Throwable cause;
    /** 子 Future 完成时的统一监听器；若不在 executor 线程则 dispatch 回 executor。 */
    private final GenericFutureListener<Future<?>> listener = new GenericFutureListener<Future<?>>() {
        @Override
        public void operationComplete(final Future<?> future) {
            if (executor.inEventLoop()) {
                operationComplete0(future);
            } else {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        operationComplete0(future);
                    }
                });
            }
        }

        private void operationComplete0(Future<?> future) {
            assert executor.inEventLoop();
            ++doneCount;
            if (!future.isSuccess() && cause == null) {
                cause = future.cause();
            }
            if (doneCount == expectedCount && aggregatePromise != null) {
                tryPromise();
            }
        }
    };

    /** 所有 add/finish 操作必须在此 executor 的 event loop 线程执行。 */
    private final EventExecutor executor;

    /**
     * Deprecated use {@link PromiseCombiner#PromiseCombiner(EventExecutor)}.
     *
     * <p>已废弃：默认使用 {@link ImmediateEventExecutor#INSTANCE}。</p>
     */
    @Deprecated
    public PromiseCombiner() {
        this(ImmediateEventExecutor.INSTANCE);
    }

    /**
     * The {@link EventExecutor} to use for notifications. You must call {@link #add(Future)}, {@link #addAll(Future[])}
     * and {@link #finish(Promise)} from within the {@link EventExecutor} thread.
     *
     * @param executor the {@link EventExecutor} to use for notifications.
     *
     * <p>指定回调与状态更新所在的 EventExecutor；add/finish 须在其 event loop 中调用。</p>
     */
    public PromiseCombiner(EventExecutor executor) {
        this.executor = ObjectUtil.checkNotNull(executor, "executor");
    }

    /**
     * Adds a new promise to be combined. New promises may be added until an aggregate promise is added via the
     * {@link PromiseCombiner#finish(Promise)} method.
     *
     * @param promise the promise to add to this promise combiner
     *
     * @deprecated Replaced by {@link PromiseCombiner#add(Future)}.
     */
    @Deprecated
    public void add(Promise promise) {
        add((Future) promise);
    }

    /**
     * Adds a new future to be combined. New futures may be added until an aggregate promise is added via the
     * {@link PromiseCombiner#finish(Promise)} method.
     *
     * @param future the future to add to this promise combiner
     *
     * <p>注册待组合的 Future；finish 之后不可再 add。</p>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void add(Future future) {
        checkAddAllowed();
        checkInEventLoop();
        ++expectedCount;
        future.addListener(listener);
    }

    /**
     * Adds new promises to be combined. New promises may be added until an aggregate promise is added via the
     * {@link PromiseCombiner#finish(Promise)} method.
     *
     * @param promises the promises to add to this promise combiner
     *
     * @deprecated Replaced by {@link PromiseCombiner#addAll(Future[])}
     */
    @Deprecated
    public void addAll(Promise... promises) {
        addAll((Future[]) promises);
    }

    /**
     * Adds new futures to be combined. New futures may be added until an aggregate promise is added via the
     * {@link PromiseCombiner#finish(Promise)} method.
     *
     * @param futures the futures to add to this promise combiner
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addAll(Future... futures) {
        for (Future future : futures) {
            this.add(future);
        }
    }

    /**
     * <p>Sets the promise to be notified when all combined futures have finished. If all combined futures succeed,
     * then the aggregate promise will succeed. If one or more combined futures fails, then the aggregate promise will
     * fail with the cause of one of the failed futures. If more than one combined future fails, then exactly which
     * failure will be assigned to the aggregate promise is undefined.</p>
     *
     * <p>After this method is called, no more futures may be added via the {@link PromiseCombiner#add(Future)} or
     * {@link PromiseCombiner#addAll(Future[])} methods.</p>
     *
     * @param aggregatePromise the promise to notify when all combined futures have finished
     *
     * <p>指定聚合 Promise；若调用时所有 Future 已完成则立即 trySuccess/tryFailure。</p>
     */
    public void finish(Promise<Void> aggregatePromise) {
        ObjectUtil.checkNotNull(aggregatePromise, "aggregatePromise");
        checkInEventLoop();
        if (this.aggregatePromise != null) {
            throw new IllegalStateException("Already finished");
        }
        this.aggregatePromise = aggregatePromise;
        if (doneCount == expectedCount) {
            tryPromise();
        }
    }

    /** 确保在 executor 的 event loop 线程调用。 */
    private void checkInEventLoop() {
        if (!executor.inEventLoop()) {
            throw new IllegalStateException("Must be called from EventExecutor thread");
        }
    }

    /** 根据 cause 决定聚合 Promise 成功或失败。 */
    private boolean tryPromise() {
        return (cause == null) ? aggregatePromise.trySuccess(null) : aggregatePromise.tryFailure(cause);
    }

    /** finish 之后禁止再 add。 */
    private void checkAddAllowed() {
        if (aggregatePromise != null) {
            throw new IllegalStateException("Adding promises is not allowed after finished adding");
        }
    }
}
