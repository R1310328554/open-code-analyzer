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
package io.netty.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;

/**
 * 将 {@link Runnable}/{@link Callable} 包装为 {@link RunnableFuture} 的 {@link Promise} 实现。
 * 任务执行完成后用哨兵对象替换 {@code task} 引用以便 GC；外部不可直接 {@code setSuccess/setFailure}。
 */
class PromiseTask<V> extends DefaultPromise<V> implements RunnableFuture<V> {

    /** 将 {@link Runnable} 适配为 {@link Callable}，执行后返回预设结果。 */
    private static final class RunnableAdapter<T> implements Callable<T> {
        final Runnable task;
        final T result;

        RunnableAdapter(Runnable task, T result) {
            this.task = task;
            this.result = result;
        }

        @Override
        public T call() {
            task.run();
            return result;
        }

        @Override
        public String toString() {
            return "Callable(task: " + task + ", result: " + result + ')';
        }
    }

    private static final Runnable COMPLETED = new SentinelRunnable("COMPLETED");
    private static final Runnable CANCELLED = new SentinelRunnable("CANCELLED");
    private static final Runnable FAILED = new SentinelRunnable("FAILED");

    /** 任务完成/取消/失败后的占位哨兵，便于释放真实 task 引用。 */
    private static class SentinelRunnable implements Runnable {
        private final String name;

        SentinelRunnable(String name) {
            this.name = name;
        }

        @Override
        public void run() { } // no-op

        @Override
        public String toString() {
            return name;
        }
    }

    /** 底层任务，类型严格为 {@link Callable}{@code <V>} 或 {@link Runnable}。 */
    private Object task;

    PromiseTask(EventExecutor executor, Runnable runnable, V result) {
        super(executor);
        task = result == null ? runnable : new RunnableAdapter<V>(runnable, result);
    }

    PromiseTask(EventExecutor executor, Runnable runnable) {
        super(executor);
        task = runnable;
    }

    PromiseTask(EventExecutor executor, Callable<V> callable) {
        super(executor);
        task = callable;
    }

    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj;
    }

    /** 执行底层 Callable 或 Runnable 并返回结果（Runnable 返回 null）。 */
    @SuppressWarnings("unchecked")
    V runTask() throws Throwable {
        final Object task = this.task;
        if (task instanceof Callable) {
            return ((Callable<V>) task).call();
        }
        ((Runnable) task).run();
        return null;
    }

    /** EventLoop 线程入口：先标记不可取消，再执行并设置成功/失败。 */
    @Override
    public void run() {
        try {
            if (setUncancellableInternal()) {
                V result = runTask();
                setSuccessInternal(result);
            }
        } catch (Throwable e) {
            setFailureInternal(e);
        }
    }

    /** 完成时以哨兵替换 task，帮助周期性调度任务及时释放引用。 */
    private boolean clearTaskAfterCompletion(boolean done, Runnable result) {
        if (done) {
            // 周期性 ScheduledFutureTask 取消时可能与哨兵替换产生良性竞态，返回值不会被使用。
            // to be called is in the case of a periodic ScheduledFutureTask,
            // in which case it's a benign race with cancellation and the (null)
            // return value is not used.
            task = result;
        }
        return done;
    }

    /** 外部禁止直接设置失败，须通过内部 {@link #setFailureInternal}。 */
    @Override
    public final Promise<V> setFailure(Throwable cause) {
        throw new IllegalStateException();
    }

    /** 内部设置失败并清理 task 引用。 */
    protected final Promise<V> setFailureInternal(Throwable cause) {
        super.setFailure(cause);
        clearTaskAfterCompletion(true, FAILED);
        return this;
    }

    @Override
    public final boolean tryFailure(Throwable cause) {
        return false;
    }

    protected final boolean tryFailureInternal(Throwable cause) {
        return clearTaskAfterCompletion(super.tryFailure(cause), FAILED);
    }

    @Override
    public final Promise<V> setSuccess(V result) {
        throw new IllegalStateException();
    }

    /** 内部设置成功并清理 task 引用。 */
    protected final Promise<V> setSuccessInternal(V result) {
        super.setSuccess(result);
        clearTaskAfterCompletion(true, COMPLETED);
        return this;
    }

    @Override
    public final boolean trySuccess(V result) {
        return false;
    }

    protected final boolean trySuccessInternal(V result) {
        return clearTaskAfterCompletion(super.trySuccess(result), COMPLETED);
    }

    @Override
    public final boolean setUncancellable() {
        throw new IllegalStateException();
    }

    /** 任务开始执行前标记不可取消。 */
    protected final boolean setUncancellableInternal() {
        return super.setUncancellable();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return clearTaskAfterCompletion(super.cancel(mayInterruptIfRunning), CANCELLED);
    }

    @Override
    protected StringBuilder toStringBuilder() {
        StringBuilder buf = super.toStringBuilder();
        buf.setCharAt(buf.length() - 1, ',');

        return buf.append(" task: ")
                  .append(task)
                  .append(')');
    }
}
