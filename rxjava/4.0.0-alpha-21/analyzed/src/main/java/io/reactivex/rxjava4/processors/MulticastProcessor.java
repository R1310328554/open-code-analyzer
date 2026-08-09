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

MulticastProcessor：多播 Processor，共享上游并向多路 Subscriber 广播，支持 connect/onNext 序列化与背压协调。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.processors;

import java.io.Serial;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.internal.functions.ObjectHelper;
import io.reactivex.rxjava4.internal.subscriptions.*;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.operators.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * A {@link FlowableProcessor} implementation that coordinates downstream requests through
 * a front-buffer and stable-prefetching, optionally canceling the upstream if all
 * subscribers have cancelled.
 * <p>
 * <img width="640" height="360" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/MulticastProcessor.png" alt="">
 * <p>
 * This processor does not have a public constructor by design; a new empty instance of this
 * {@code MulticastProcessor} can be created via the following {@code create} methods that
 * allow configuring it:
 * <ul>
 * <li>{@link #create()}: create an empty {@code MulticastProcessor} with
 *      {@link io.reactivex.rxjava4.core.Flowable#bufferSize() Flowable.bufferSize()} prefetch amount
 *      and no reference counting behavior.</li>
 * <li>{@link #create(int)}: create an empty {@code MulticastProcessor} with
 *      the given prefetch amount and no reference counting behavior.</li>
 * <li>{@link #create(boolean)}: create an empty {@code MulticastProcessor} with
 *      {@link io.reactivex.rxjava4.core.Flowable#bufferSize() Flowable.bufferSize()} prefetch amount
 *      and an optional reference counting behavior.</li>
 * <li>{@link #create(int, boolean)}: create an empty {@code MulticastProcessor} with
 *      the given prefetch amount and an optional reference counting behavior.</li>
 * </ul>
 * <p>
 * When the reference counting behavior is enabled, the {@code MulticastProcessor} cancels its
 * upstream when all {@link Subscriber}s have cancelled. Late {@code Subscriber}s will then be
 * immediately completed.
 * <p>
 * Because {@code MulticastProcessor} implements the {@link Subscriber} interface, calling
 * {@code onSubscribe} is mandatory (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.12">Rule 2.12</a>).
 * If {@code MulticastProcessor} should run standalone, i.e., without subscribing the {@code MulticastProcessor} to another {@link Publisher},
 * use {@link #start()} or {@link #startUnbounded()} methods to initialize the internal buffer.
 * Failing to do so will lead to a {@link NullPointerException} at runtime.
 * <p>
 * Use {@link #offer(Object)} to try and offer/emit items but don't fail if the
 * internal buffer is full.
 * <p>
 * A {@code MulticastProcessor} is a {@link Processor} type in the Reactive Streams specification,
 * {@code null}s are not allowed (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.13">Rule 2.13</a>) as
 * parameters to {@link #onSubscribe(Subscription)}, {@link #offer(Object)}, {@link #onNext(Object)} and {@link #onError(Throwable)}.
 * Such calls will result in a {@link NullPointerException} being thrown and the processor's state is not changed.
 * <p>
 * Since a {@code MulticastProcessor} is a {@link io.reactivex.rxjava4.core.Flowable}, it supports backpressure.
 * The backpressure from the currently subscribed {@link Subscriber}s are coordinated by emitting upstream
 * items only if all of those {@code Subscriber}s have requested at least one item. This behavior
 * is also called <em>lockstep-mode</em> because even if some {@code Subscriber}s can take any number
 * of items, other {@code Subscriber}s requesting less or infrequently will slow down the overall
 * throughput of the flow.
 * <p>
 * Calling {@link #onNext(Object)}, {@link #offer(Object)}, {@link #onError(Throwable)} and {@link #onComplete()}
 * is required to be serialized (called from the same thread or called non-overlappingly from different threads
 * through external means of serialization). The {@link #toSerialized()} method available to all {@link FlowableProcessor}s
 * provides such serialization and also protects against reentrance (i.e., when a downstream {@code Subscriber}
 * consuming this processor also wants to call {@link #onNext(Object)} on this processor recursively).
 * <p>
 * This {@code MulticastProcessor} supports the standard state-peeking methods {@link #hasComplete()}, {@link #hasThrowable()},
 * {@link #getThrowable()} and {@link #hasSubscribers()}. This processor doesn't allow peeking into its buffer.
 * <p>
 * When this {@code MulticastProcessor} is terminated via {@link #onError(Throwable)} or {@link #onComplete()},
 * all previously signaled but not yet consumed items will be still available to {@code Subscriber}s and the respective
 * terminal even is only emitted when all previous items have been successfully delivered to {@code Subscriber}s.
 * If there are no {@code Subscriber}s, the remaining items will be buffered indefinitely.
 * <p>
 * The {@code MulticastProcessor} does not support clearing its cached events (to appear empty again).
 * <dl>
 *  <dt><b>背压：</b></dt>
 *  <dd>The backpressure from the currently subscribed {@code Subscriber}s are coordinated by emitting upstream
 *  items only if all of those {@code Subscriber}s have requested at least one item. This behavior
 *  is also called <em>lockstep-mode</em> because even if some {@code Subscriber}s can take any number
 *  of items, other {@code Subscriber}s requesting less or infrequently will slow down the overall
 *  throughput of the flow.</dd>
 *  <dt><b>调度器：</b></dt>
 *  <dd>{@code MulticastProcessor} does not operate by default on a particular {@link io.reactivex.rxjava4.core.Scheduler} and
 *  the {@code Subscriber}s get notified on an arbitrary thread in a serialized fashion.</dd>
 * </dl>
 * <p>
 * Example:
 * <pre><code>
    MulticastProcessor&lt;Integer&gt; mp = Flowable.range(1, 10)
    .subscribeWith(MulticastProcessor.create());

    mp.test().assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    // --------------------

    MulticastProcessor&lt;Integer&gt; mp2 = MulticastProcessor.create(4);
    mp2.start();

    assertTrue(mp2.offer(1));
    assertTrue(mp2.offer(2));
    assertTrue(mp2.offer(3));
    assertTrue(mp2.offer(4));

    assertFalse(mp2.offer(5));

    mp2.onComplete();

    mp2.test().assertResult(1, 2, 3, 4);
 * </code></pre>
 * <p>History: 2.1.14 - experimental
 * @param <T> the input and output value type
 * @since 2.2
 */
@BackpressureSupport(BackpressureKind.FULL)
@SchedulerSupport(SchedulerSupport.NONE)
/* ===== [OCA 中文解析] =====
class MulticastProcessor — 意图说明

多播 FlowableProcessor：create 后 onNext 向全部 Subscriber 广播。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * 多播 FlowableProcessor：create 后 onNext 向全部 Subscriber 广播。
 */
public final class MulticastProcessor<@NonNull T> extends FlowableProcessor<T> {

    final AtomicInteger wip;

    final AtomicReference<Subscription> upstream;

    final AtomicReference<MulticastSubscription<T>[]> subscribers;

    final int bufferSize;

    final int limit;

    final boolean refcount;

    volatile SimpleQueue<T> queue;

    volatile boolean done;
    volatile Throwable error;

    int consumed;

    int fusionMode;

    @SuppressWarnings("rawtypes")
    static final MulticastSubscription[] EMPTY = new MulticastSubscription[0];

    @SuppressWarnings("rawtypes")
    static final MulticastSubscription[] TERMINATED = new MulticastSubscription[0];

    /**
     * Constructs a fresh instance with the default Flowable.bufferSize() prefetch
     * amount and no refCount-behavior.
     * @param <T> the input and output value type
     * @return 新的 MulticastProcessor 实例
     */
    @CheckReturnValue
    @NonNull
    public static <T> MulticastProcessor<T> create() {
        return new MulticastProcessor<>(bufferSize(), false);
    }

    /**
     * Constructs a fresh instance with the default Flowable.bufferSize() prefetch
     * amount and the optional refCount-behavior.
     * @param <T> the input and output value type
     * @param refCount if true and if all Subscribers have cancelled, the upstream
     * is cancelled
     * @return 新的 MulticastProcessor 实例
     */
    @CheckReturnValue
    @NonNull
    public static <T> MulticastProcessor<T> create(boolean refCount) {
        return new MulticastProcessor<>(bufferSize(), refCount);
    }

    /**
     * Constructs a fresh instance with the given prefetch amount and no refCount behavior.
     * @param bufferSize the prefetch amount
     * @param <T> the input and output value type
     * @return 新的 MulticastProcessor 实例
     * @throws IllegalArgumentException 若 {@code bufferSize} is non-positive
     */
    @CheckReturnValue
    @NonNull
    public static <T> MulticastProcessor<T> create(int bufferSize) {
        ObjectHelper.verifyPositive(bufferSize, "bufferSize");
        return new MulticastProcessor<>(bufferSize, false);
    }

    /**
     * Constructs a fresh instance with the given prefetch amount and the optional
     * refCount-behavior.
     * @param bufferSize the prefetch amount
     * @param refCount if true and if all Subscribers have cancelled, the upstream
     * is cancelled
     * @param <T> the input and output value type
     * @return 新的 MulticastProcessor 实例
     * @throws IllegalArgumentException 若 {@code bufferSize} is non-positive
     */
    @CheckReturnValue
    @NonNull
    public static <T> MulticastProcessor<T> create(int bufferSize, boolean refCount) {
        ObjectHelper.verifyPositive(bufferSize, "bufferSize");
        return new MulticastProcessor<>(bufferSize, refCount);
    }

    /**
 * 【说明】Constructs a fresh instance with the given prefetch amount and the optional；r...
     * Constructs a fresh instance with the given prefetch amount and the optional
     * refCount-behavior.
     * @param bufferSize the prefetch amount
     * @param refCount if true and if all Subscribers have cancelled, the upstream
     * is cancelled
     */
    @SuppressWarnings("unchecked")
    MulticastProcessor(int bufferSize, boolean refCount) {
        this.bufferSize = bufferSize;
        this.limit = bufferSize - (bufferSize >> 2);
        this.wip = new AtomicInteger();
        this.subscribers = new AtomicReference<MulticastSubscription<T>[]>(EMPTY);
        this.upstream = new AtomicReference<>();
        this.refcount = refCount;
    }

    /**
 * 【说明】Initializes this Processor by setting an upstream Subscription that；ignores r...
     * Initializes this Processor by setting an upstream Subscription that
     * ignores request amounts, uses a fixed buffer
     * and allows using the onXXX and offer methods
     * afterward.
     */
    /** 方法 start：void 类型 API。 */

    public void start() {
        if (SubscriptionHelper.setOnce(upstream, EmptySubscription.INSTANCE)) {
            queue = new SpscArrayQueue<>(bufferSize);
        }
    }

    /**
 * 【说明】Initializes this Processor by setting an upstream Subscription that；ignores r...
     * Initializes this Processor by setting an upstream Subscription that
     * ignores request amounts, uses an unbounded buffer
     * and allows using the onXXX and offer methods
     * afterward.
     */
    /** 方法 startUnbounded：void 类型 API。 */

    public void startUnbounded() {
        if (SubscriptionHelper.setOnce(upstream, EmptySubscription.INSTANCE)) {
            queue = new SpscLinkedArrayQueue<>(bufferSize);
        }
    }

    @Override
    /** 方法 onSubscribe：void 类型 API。 */

    public void onSubscribe(@NonNull Subscription s) {
        if (SubscriptionHelper.setOnce(upstream, s)) {
            if (s instanceof QueueSubscription) {
                @SuppressWarnings("unchecked")
                QueueSubscription<T> qs = (QueueSubscription<T>)s;

                int m = qs.requestFusion(QueueSubscription.ANY);
                if (m == QueueSubscription.SYNC) {
                    fusionMode = m;
                    queue = qs;
                    done = true;
                    drain();
                    return;
                }
                if (m == QueueSubscription.ASYNC) {
                    fusionMode = m;
                    queue = qs;

                    s.request(bufferSize);
                    return;
                }
            }

            queue = new SpscArrayQueue<>(bufferSize);

            s.request(bufferSize);
        }
    }

    /** 处理上游 onNext 并转发或缓存。 */


    @Override


    /** 方法 onNext：void 类型 API。 */



    public void onNext(@NonNull T t) {
        if (done) {
            return;
        }
        if (fusionMode == QueueSubscription.NONE) {
            ExceptionHelper.nullCheck(t, "onNext called with a null value.");
            if (!queue.offer(t)) {
                SubscriptionHelper.cancel(upstream);
                onError(MissingBackpressureException.createDefault());
                return;
            }
        }
        drain();
    }

    /**
     * Tries to offer an item into the internal queue and returns false
     * if the queue is full.
     * @param t 元素 to offer, not {@code null}
     * @return true if successful, false if the queue is full
     * @throws NullPointerException 若 {@code t} is {@code null}
     * @throws IllegalStateException if the processor is in fusion mode
     */
    @CheckReturnValue
    /** 方法 offer：boolean 类型 API。 */

    public boolean offer(@NonNull T t) {
        ExceptionHelper.nullCheck(t, "offer called with a null value.");
        if (done) {
            return false;
        }
        if (fusionMode == QueueSubscription.NONE) {
            if (queue.offer(t)) {
                drain();
                return true;
            }
            return false;
        }
        throw new IllegalStateException("offer() should not be called in fusion mode!");
    }

    /** 处理 onError 并按策略终止或延迟错误。 */


    @Override


    /** 方法 onError：void 类型 API。 */



    public void onError(@NonNull Throwable t) {
        ExceptionHelper.nullCheck(t, "onError called with a null Throwable.");
        if (!done) {
            error = t;
            done = true;
            drain();
            return;
        }
        RxJavaPlugins.onError(t);
    }

    /** 上游/Processor 完成：清理并向下游发送 onComplete。 */


    @Override


    /** 方法 onComplete：void 类型 API。 */



    public void onComplete() {
        done = true;
        drain();
    }

    @Override
    @CheckReturnValue
    /** 方法 hasSubscribers：boolean 类型 API。 */

    public boolean hasSubscribers() {
        return subscribers.get().length != 0;
    }

    @Override
    @CheckReturnValue
    /** 方法 hasThrowable：boolean 类型 API。 */

    public boolean hasThrowable() {
        return done && error != null;
    }

    @Override
    @CheckReturnValue
    /** 方法 hasComplete：boolean 类型 API。 */

    public boolean hasComplete() {
        return done && error == null;
    }

    @Override
    @CheckReturnValue
    /** 方法 getThrowable：Throwable 类型 API。 */

    public Throwable getThrowable() {
        return done ? error : null;
    }

    /** 组装内部 Subscriber/Observer 并订阅上游。 */


    @Override


    protected void subscribeActual(@NonNull Subscriber<? super T> s) {
        MulticastSubscription<T> ms = new MulticastSubscription<>(s, this);
        s.onSubscribe(ms);
        if (add(ms)) {
            if (ms.get() == Long.MIN_VALUE) {
                remove(ms);
            } else {
                drain();
            }
        } else {
            if (done) {
                Throwable ex = error;
                if (ex != null) {
                    s.onError(ex);
                    return;
                }
            }
            s.onComplete();
        }
    }

    boolean add(MulticastSubscription<T> inner) {
        for (;;) {
            MulticastSubscription<T>[] a = subscribers.get();
            if (a == TERMINATED) {
                return false;
            }
            int n = a.length;
            @SuppressWarnings("unchecked")
            MulticastSubscription<T>[] b = new MulticastSubscription[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = inner;
            if (subscribers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    void remove(MulticastSubscription<T> inner) {
        for (;;) {
            MulticastSubscription<T>[] a = subscribers.get();
            int n = a.length;
            if (n == 0) {
                return;
            }

            int j = -1;
            for (int i = 0; i < n; i++) {
                if (a[i] == inner) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                break;
            }

            if (n == 1) {
                if (refcount) {
                    if (subscribers.compareAndSet(a, TERMINATED)) {
                        SubscriptionHelper.cancel(upstream);
                        done = true;
                        break;
                    }
                } else {
                    if (subscribers.compareAndSet(a, EMPTY)) {
                        break;
                    }
                }
            } else {
                MulticastSubscription<T>[] b = new MulticastSubscription[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
                if (subscribers.compareAndSet(a, b)) {
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    void drain() {
        if (wip.getAndIncrement() != 0) {
            return;
        }

        int missed = 1;
        AtomicReference<MulticastSubscription<T>[]> subs = subscribers;
        int c = consumed;
        int lim = limit;
        int fm = fusionMode;

        outer:
        for (;;) {

            SimpleQueue<T> q = queue;

            if (q != null) {
                MulticastSubscription<T>[] as = subs.get();
                int n = as.length;

                if (n != 0) {
                    long r = -1L;

                    for (MulticastSubscription<T> a : as) {
                        long ra = a.get();
                        if (ra >= 0L) {
                            if (r == -1L) {
                                r = ra - a.emitted;
                            } else {
                                r = Math.min(r, ra - a.emitted);
                            }
                        }
                    }

                    while (r > 0L) {
                        MulticastSubscription<T>[] bs = subs.get();

                        if (bs == TERMINATED) {
                            q.clear();
                            return;
                        }

                        if (as != bs) {
                            continue outer;
                        }

                        boolean d = done;

                        T v;

                        try {
                            v = q.poll();
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            SubscriptionHelper.cancel(upstream);
                            d = true;
                            v = null;
                            error = ex;
                            done = true;
                        }
                        boolean empty = v == null;

                        if (d && empty) {
                            Throwable ex = error;
                            if (ex != null) {
                                for (MulticastSubscription<T> inner : subs.getAndSet(TERMINATED)) {
                                    inner.onError(ex);
                                }
                            } else {
                                for (MulticastSubscription<T> inner : subs.getAndSet(TERMINATED)) {
                                    inner.onComplete();
                                }
                            }
                            return;
                        }

                        if (empty) {
                            break;
                        }

                        for (MulticastSubscription<T> inner : as) {
                            inner.onNext(v);
                        }

                        r--;

                        if (fm != QueueSubscription.SYNC) {
                            if (++c == lim) {
                                c = 0;
                                upstream.get().request(lim);
                            }
                        }
                    }

                    if (r == 0) {
                        MulticastSubscription<T>[] bs = subs.get();

                        if (bs == TERMINATED) {
                            q.clear();
                            return;
                        }

                        if (as != bs) {
                            continue;
                        }

                        if (done && q.isEmpty()) {
                            Throwable ex = error;
                            if (ex != null) {
                                for (MulticastSubscription<T> inner : subs.getAndSet(TERMINATED)) {
                                    inner.onError(ex);
                                }
                            } else {
                                for (MulticastSubscription<T> inner : subs.getAndSet(TERMINATED)) {
                                    inner.onComplete();
                                }
                            }
                            return;
                        }
                    }
                }
            }

            consumed = c;
            missed = wip.addAndGet(-missed);
            if (missed == 0) {
                break;
            }
        }
    }

    /** 内部 MulticastSubscription。 */


    static final class MulticastSubscription<@NonNull T> extends AtomicLong implements Subscription {

        @Serial
        private static final long serialVersionUID = -363282618957264509L;

        final Subscriber<? super T> downstream;

        final MulticastProcessor<T> parent;

        long emitted;

        MulticastSubscription(Subscriber<? super T> actual, MulticastProcessor<T> parent) {
            this.downstream = actual;
            this.parent = parent;
        }

        /** 处理下游背压 request。 */


        @Override


        /** 方法 request：void 类型 API。 */



        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                long r = BackpressureHelper.addCancel(this, n);
                if (r != Long.MIN_VALUE && r != Long.MAX_VALUE) {
                    parent.drain();
                }
            }
        }

        /** 取消订阅并释放资源。 */


        @Override


        /** 方法 cancel：void 类型 API。 */



        public void cancel() {
            if (getAndSet(Long.MIN_VALUE) != Long.MIN_VALUE) {
                parent.remove(this);
            }
        }

        void onNext(T t) {
            if (get() != Long.MIN_VALUE) {
                emitted++;
                downstream.onNext(t);
            }
        }

        void onError(Throwable t) {
            if (get() != Long.MIN_VALUE) {
                downstream.onError(t);
            }
        }

        void onComplete() {
            if (get() != Long.MIN_VALUE) {
                downstream.onComplete();
            }
        }
    }
}
