/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

/* ===== [OCA 中文解析] =====
文件意图总览

Unicast 单播 Subject：仅允许一个 Observer，队列缓冲上游事件，终止时可触发 onTerminate Runnable。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.subjects;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.internal.functions.*;
import io.reactivex.rxjava4.internal.observers.BasicIntQueueDisposable;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;
import io.reactivex.rxjava4.operators.SimpleQueue;
import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/* ===== [OCA 中文解析] =====
class UnicastSubject — 意图说明

单 Observer 队列 Subject，UnicastQueueDisposable 协调 drain。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * A Subject that queues up events until a single {@link Observer} subscribes to it, replays
 * those events to it until the {@code Observer} catches up and then switches to relaying events live to
 * this single {@code Observer} until this {@code UnicastSubject} terminates or the {@code Observer} disposes.
 * <p>
 * <img width="640" height="370" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/UnicastSubject.png" alt="">
 * <p>
 * Note that {@code UnicastSubject} holds an unbounded internal buffer.
 * <p>
 * This subject does not have a public constructor by design; a new empty instance of this
 * {@code UnicastSubject} can be created via the following {@code create} methods that
 * allow specifying the retention policy for items:
 * <ul>
 * <li>{@link #create()} - creates an empty, unbounded {@code UnicastSubject} that
 *     caches all items and the terminal event it receives.</li>
 * <li>{@link #create(int)} - creates an empty, unbounded {@code UnicastSubject}
 *     with a hint about how many <b>total</b> items one expects to retain.</li>
 * <li>{@link #create(boolean)} - creates an empty, unbounded {@code UnicastSubject} that
 *     optionally delays an error it receives and replays it after the regular items have been emitted.</li>
 * <li>{@link #create(int, Runnable)} - creates an empty, unbounded {@code UnicastSubject}
 *     with a hint about how many <b>total</b> items one expects to retain and a callback that will be
 *     called exactly once when the {@code UnicastSubject} gets terminated or the single {@code Observer} disposes.</li>
 * <li>{@link #create(int, Runnable, boolean)} - creates an empty, unbounded {@code UnicastSubject}
 *     with a hint about how many <b>total</b> items one expects to retain and a callback that will be
 *     called exactly once when the {@code UnicastSubject} gets terminated or the single {@code Observer} disposes
 *     and optionally delays an error it receives and replays it after the regular items have been emitted.</li>
 * </ul>
 * <p>
 * If more than one {@code Observer} attempts to subscribe to this {@code UnicastSubject}, they
 * will receive an {@code IllegalStateException} indicating the single-use-only nature of this {@code UnicastSubject},
 * even if the {@code UnicastSubject} already terminated with an error.
 * <p>
 * Since a {@code Subject} is conceptionally derived from the {@code Processor} type in the Reactive Streams specification,
 * {@code null}s are not allowed (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.13">Rule 2.13</a>) as
 * parameters to {@link #onNext(Object)} and {@link #onError(Throwable)}. Such calls will result in a
 * {@link NullPointerException} being thrown and the subject's state is not changed.
 * <p>
 * Since a {@code UnicastSubject} is an {@link io.reactivex.rxjava4.core.Observable}, it does not support backpressure.
 * <p>
 * When this {@code UnicastSubject} is terminated via {@link #onError(Throwable)} the current or late single {@code Observer}
 * may receive the {@code Throwable} before any available items could be emitted. To make sure an onError event is delivered
 * to the {@code Observer} after the normal items, create a {@code UnicastSubject} with the {@link #create(boolean)} or
 * {@link #create(int, Runnable, boolean)} factory methods.
 * <p>
 * Even though {@code UnicastSubject} implements the {@code Observer} interface, calling
 * {@code onSubscribe} is not required (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.12">Rule 2.12</a>)
 * if the subject is used as a standalone source. However, calling {@code onSubscribe}
 * after the {@code UnicastSubject} reached its terminal state will result in the
 * given {@code Disposable} being disposed immediately.
 * <p>
 * Calling {@link #onNext(Object)}, {@link #onError(Throwable)} and {@link #onComplete()}
 * is required to be serialized (called from the same thread or called non-overlappingly from different threads
 * through external means of serialization). The {@link #toSerialized()} method available to all {@code Subject}s
 * provides such serialization and also protects against reentrance (i.e., when a downstream {@code Observer}
 * consuming this subject also wants to call {@link #onNext(Object)} on this subject recursively).
 * <p>
 * This {@code UnicastSubject} supports the standard state-peeking methods {@link #hasComplete()}, {@link #hasThrowable()},
 * {@link #getThrowable()} and {@link #hasObservers()}.
 * <dl>
 *  <dt><b>调度器：</b></dt>
 *  <dd>{@code UnicastSubject} does not operate by default on a particular {@link io.reactivex.rxjava4.core.Scheduler} and
 *  the single {@code Observer} gets notified on the thread the respective {@code onXXX} methods were invoked.</dd>
 *  <dt><b>错误处理：</b></dt>
 *  <dd>When the {@link #onError(Throwable)} is called, the {@code UnicastSubject} enters into a terminal state
 *  and emits the same {@code Throwable} instance to the current single {@code Observer}. During this emission,
 *  if the single {@code Observer}s disposes its respective {@code Disposable}, the
 *  {@code Throwable} is delivered to the global error handler via
 *  {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)}.
 *  If there were no {@code Observer}s subscribed to this {@code UnicastSubject} when the {@code onError()}
 *  was called, the global error handler is not invoked.
 *  </dd>
 * </dl>
 * <p>
 * 用法示例：
 * <pre><code>
 * UnicastSubject&lt;Integer&gt; subject = UnicastSubject.create();
 *
 * TestObserver&lt;Integer&gt; to1 = subject.test();
 *
 * // fresh UnicastSubjects are empty
 * to1.assertEmpty();
 *
 * TestObserver&lt;Integer&gt; to2 = subject.test();
 *
 * // A UnicastSubject only allows one Observer during its lifetime
 * to2.assertFailure(IllegalStateException.class);
 *
 * subject.onNext(1);
 * to1.assertValue(1);
 *
 * subject.onNext(2);
 * to1.assertValues(1, 2);
 *
 * subject.onComplete();
 * to1.assertResult(1, 2);
 *
 * // ----------------------------------------------------
 *
 * UnicastSubject&lt;Integer&gt; subject2 = UnicastSubject.create();
 *
 * // a UnicastSubject caches events until its single Observer subscribes
 * subject2.onNext(1);
 * subject2.onNext(2);
 * subject2.onComplete();
 *
 * TestObserver&lt;Integer&gt; to3 = subject2.test();
 *
 * // the cached events are emitted in order
 * to3.assertResult(1, 2);
 * </code></pre>
 * @param <T> value 类型 received and emitted by this Subject subclass
 * @since 2.0
 */
public /** 内部 UnicastSubject：单播队列 drain 与终端处理。 */
 final class UnicastSubject<T> extends Subject<T> {
    /** The queue that buffers the source events. */
    final SpscLinkedArrayQueue<T> queue;

    /** The single Observer. */
    final AtomicReference<Observer<? super T>> downstream;

    /** The optional callback when the Subject gets cancelled or terminates. */
    final AtomicReference<Runnable> onTerminate;

    /** deliver onNext events before error event. */
    final boolean delayError;

    /** Indicates the single observer has cancelled. */
    volatile boolean disposed;

    /** Indicates the source has terminated. */
    volatile boolean done;
    /**
 * 【说明】The terminal error if not null.；Must be set before writing to done and read a...
     * The terminal error if not null.
     * Must be set before writing to done and read after done == true.
     */
    Throwable error;

    /** Set to 1 atomically for the first and only Subscriber. */
    final AtomicBoolean once;

    /** The wip counter and QueueDisposable surface. */
    final BasicIntQueueDisposable<T> wip;

    boolean enableOperatorFusion;

    /**
     * 创建n UnicastSubject with an internal buffer capacity hint 16.
     * @param <T> value 类型
     * @return an UnicastSubject instance
     */
    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create() {
        return new UnicastSubject<>(bufferSize(), null, true);
    }

    /**
     * 创建n UnicastSubject with the given internal buffer capacity hint.
     * @param <T> value 类型
     * @param capacityHint 队列容量提示 hint to size the internal unbounded buffer
     * @return an UnicastSubject instance
     * @throws IllegalArgumentException 若参数非法 {@code capacityHint} is non-positive
     */
    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create(int capacityHint) {
        ObjectHelper.verifyPositive(capacityHint, "capacityHint");
        return new UnicastSubject<>(capacityHint, null, true);
    }

    /**
     * 创建n UnicastSubject with the given internal buffer capacity hint and a callback for
     * the case when the single Subscriber cancels its subscription
     * or the subject is terminated.
     *
     * <p>The callback, if not null, is called exactly once and
     * non-overlapped with any active replay.
     *
     * @param <T> value 类型
     * @param capacityHint 队列容量提示 hint to size the internal unbounded buffer
     * @param onTerminate 终止时 Runnable 回调 callback to run when the Subject is terminated or cancelled, null not allowed
     * @return an UnicastSubject instance
     * @throws NullPointerException 若参数为 null {@code onTerminate} is {@code null}
     * @throws IllegalArgumentException 若参数非法 {@code capacityHint} is non-positive
     */
    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create(int capacityHint, @NonNull Runnable onTerminate) {
        ObjectHelper.verifyPositive(capacityHint, "capacityHint");
        Objects.requireNonNull(onTerminate, "onTerminate");
        return new UnicastSubject<>(capacityHint, onTerminate, true);
    }

    /**
     * 创建n UnicastSubject with the given internal buffer capacity hint, delay error flag and
     * a callback for the case when the single Observer disposes its {@link Disposable}
     * or the subject is terminated.
     *
     * <p>The callback, if not null, is called exactly once and
     * non-overlapped with any active replay.
     * <p>版本历史： 2.0.8 - experimental
     * @param <T> value 类型
     * @param capacityHint 队列容量提示 hint to size the internal unbounded buffer
     * @param onTerminate 终止时 Runnable 回调 callback to run when the Subject is terminated or cancelled, null not allowed
     * @param delayError deliver pending onNext events before onError
     * @return an UnicastSubject instance
     * @throws NullPointerException 若参数为 null {@code onTerminate} is {@code null}
     * @throws IllegalArgumentException 若参数非法 {@code capacityHint} is non-positive
     * @since 2.2
     */
    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create(int capacityHint, @NonNull Runnable onTerminate, boolean delayError) {
        ObjectHelper.verifyPositive(capacityHint, "capacityHint");
        Objects.requireNonNull(onTerminate, "onTerminate");
        return new UnicastSubject<>(capacityHint, onTerminate, delayError);
    }

    /**
     * 创建n UnicastSubject with an internal buffer capacity hint 16 and given delay error flag.
     *
     * <p>The callback, if not null, is called exactly once and
     * non-overlapped with any active replay.
     * <p>版本历史： 2.0.8 - experimental
     * @param <T> value 类型
     * @param delayError deliver pending onNext events before onError
     * @return an UnicastSubject instance
     * @since 2.2
     */
    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create(boolean delayError) {
        return new UnicastSubject<>(bufferSize(), null, delayError);
    }

    /**
     * 创建n UnicastSubject with the given capacity hint, delay error flag and callback
     * for when the Subject is terminated normally or its single Subscriber cancels.
     * <p>版本历史： 2.0.8 - experimental
     * @param capacityHint 队列容量提示 capacity hint for the internal, unbounded queue
     * @param onTerminate 终止时 Runnable 回调 callback to run when the Subject is terminated or cancelled, null not allowed
     * @param delayError deliver pending onNext events before onError
     * @since 2.2
     */
    /**
     * 构造 UnicastSubject。
     * @param int int 参数
     * @param Runnable Runnable 参数
     * @param boolean boolean 参数
     */
    UnicastSubject(int capacityHint, Runnable onTerminate, boolean delayError) {
        this.queue = new SpscLinkedArrayQueue<>(capacityHint);
        this.onTerminate = new AtomicReference<>(onTerminate);
        this.delayError = delayError;
        this.downstream = new AtomicReference<>();
        this.once = new AtomicBoolean();
        this.wip = new UnicastQueueDisposable();
    }

    /** 组装内部 Subscriber/Observer 并订阅上游。 */


    @Override


    protected void subscribeActual(Observer<? super T> observer) {
        if (!once.get() && once.compareAndSet(false, true)) {
            observer.onSubscribe(wip);
            downstream.lazySet(observer); // full barrier in drain
            if (disposed) {
                downstream.lazySet(null);
                return;
            }
            drain();
        } else {
            EmptyDisposable.error(new IllegalStateException("Only a single observer allowed."), observer);
        }
    }

    void doTerminate() {
        Runnable r = onTerminate.get();
        if (r != null && onTerminate.compareAndSet(r, null)) {
            r.run();
        }
    }

    @Override
    /** 实例方法 void：Processor/Subject 状态或发射 API。 */

    public void onSubscribe(Disposable d) {
        if (done || disposed) {
            d.dispose();
        }
    }

    @Override
    /** 实例方法 void：Processor/Subject 状态或发射 API。 */

    public void onNext(T t) {
        ExceptionHelper.nullCheck(t, "onNext called with a null value.");
        if (done || disposed) {
            return;
        }
        queue.offer(t);
        drain();
    }

    @Override
    /** 实例方法 void：Processor/Subject 状态或发射 API。 */

    public void onError(Throwable t) {
        ExceptionHelper.nullCheck(t, "onError called with a null Throwable.");
        if (done || disposed) {
            RxJavaPlugins.onError(t);
            return;
        }
        error = t;
        done = true;

        doTerminate();

        drain();
    }

    @Override
    /** 实例方法 void：Processor/Subject 状态或发射 API。 */

    public void onComplete() {
        if (done || disposed) {
            return;
        }
        done = true;

        doTerminate();

        drain();
    }

    void drainNormal(Observer<? super T> a) {
        int missed = 1;
        SimpleQueue<T> q = queue;
        boolean failFast = !this.delayError;
        boolean canBeError = true;
        for (;;) {
            for (;;) {

                if (disposed) {
                    downstream.lazySet(null);
                    q.clear();
                    return;
                }

                boolean d = this.done;
                T v = queue.poll();
                boolean empty = v == null;

                if (d) {
                    if (failFast && canBeError) {
                        if (failedFast(q, a)) {
                            return;
                        } else {
                            canBeError = false;
                        }
                    }

                    if (empty) {
                        errorOrComplete(a);
                        return;
                    }
                }

                if (empty) {
                    break;
                }

                a.onNext(v);
            }

            missed = wip.addAndGet(-missed);
            if (missed == 0) {
                break;
            }
        }
    }

    void drainFused(Observer<? super T> a) {
        int missed = 1;

        final SpscLinkedArrayQueue<T> q = queue;
        final boolean failFast = !delayError;

        for (;;) {

            if (disposed) {
                downstream.lazySet(null);
                return;
            }
            boolean d = done;

            if (failFast && d) {
                if (failedFast(q, a)) {
                    return;
                }
            }

            a.onNext(null);

            if (d) {
                errorOrComplete(a);
                return;
            }

            missed = wip.addAndGet(-missed);
            if (missed == 0) {
                break;
            }
        }
    }

    void errorOrComplete(Observer<? super T> a) {
        downstream.lazySet(null);
        Throwable ex = error;
        if (ex != null) {
            a.onError(ex);
        } else {
            a.onComplete();
        }
    }

    boolean failedFast(final SimpleQueue<T> q, Observer<? super T> a) {
        Throwable ex = error;
        if (ex != null) {
            downstream.lazySet(null);
            q.clear();
            a.onError(ex);
            return true;
        } else {
            return false;
        }
    }

    /** drain 循环：按 request 从队列取元素发射。 */


    void drain() {
        if (wip.getAndIncrement() != 0) {
            return;
        }

        Observer<? super T> a = downstream.get();
        int missed = 1;

        for (;;) {

            if (a != null) {
                if (enableOperatorFusion) {
                    drainFused(a);
                } else {
                    drainNormal(a);
                }
                return;
            }

            missed = wip.addAndGet(-missed);
            if (missed == 0) {
                break;
            }

            a = downstream.get();
        }
    }

    @Override
    @CheckReturnValue
    /** 实例方法 boolean：Processor/Subject 状态或发射 API。 */

    /** 是否存在活跃 Observer。 */


    public boolean hasObservers() {
        return downstream.get() != null;
    }

    @Override
    @Nullable
    @CheckReturnValue
    /** 实例方法 Throwable：Processor/Subject 状态或发射 API。 */

    /** 返回终端 Throwable（若有）。 */


    public Throwable getThrowable() {
        if (done) {
            return error;
        }
        return null;
    }

    @Override
    @CheckReturnValue
    /** 实例方法 boolean：Processor/Subject 状态或发射 API。 */

    /** 是否因错误终止。 */


    public boolean hasThrowable() {
        return done && error != null;
    }

    @Override
    @CheckReturnValue
    /** 实例方法 boolean：Processor/Subject 状态或发射 API。 */

    /** 是否已正常完成（onComplete）。 */


    public boolean hasComplete() {
        return done && error == null;
    }

    /** 内部 UnicastQueueDisposable：单播队列 drain 与终端处理。 */


    final class UnicastQueueDisposable extends BasicIntQueueDisposable<T> {

        @Serial
        private static final long serialVersionUID = 7926949470189395511L;

        @Override
        /** 实例方法 int：Processor/Subject 状态或发射 API。 */

        public int requestFusion(int mode) {
            if ((mode & ASYNC) != 0) {
                enableOperatorFusion = true;
                return ASYNC;
            }
            return NONE;
        }

        @Nullable
        @Override
        /** 实例方法 T：Processor/Subject 状态或发射 API。 */

        public T poll() {
            return queue.poll();
        }

        @Override
        /** 实例方法 boolean：Processor/Subject 状态或发射 API。 */

        public boolean isEmpty() {
            return queue.isEmpty();
        }

        @Override
        /** 实例方法 void：Processor/Subject 状态或发射 API。 */

        public void clear() {
            queue.clear();
        }

        @Override
        /** 实例方法 void：Processor/Subject 状态或发射 API。 */

        public void dispose() {
            if (!disposed) {
                disposed = true;

                doTerminate();

                downstream.lazySet(null);
                if (wip.getAndIncrement() == 0) {
                    downstream.lazySet(null);
                    if (!enableOperatorFusion) {
                        queue.clear();
                    }
                }
            }
        }

        @Override
        /** 实例方法 boolean：Processor/Subject 状态或发射 API。 */

        public boolean isDisposed() {
            return disposed;
        }

    }
}
