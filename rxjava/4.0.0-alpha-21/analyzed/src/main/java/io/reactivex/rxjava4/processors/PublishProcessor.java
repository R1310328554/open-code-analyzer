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

Publish 多播 Processor：将后续 onNext 广播给当前全部 Subscriber，不缓存历史项，支持 offer 非阻塞发射与 toSerialized 串行化。
===== [OCA 中文解析结束] ===== */
package io.reactivex.rxjava4.processors;

import java.io.Serial;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.exceptions.MissingBackpressureException;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.internal.util.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/* ===== [OCA 中文解析] =====
class PublishProcessor — 意图说明

无缓存多播 FlowableProcessor，PublishSubscription 管理各 Subscriber 背压。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * 多播 Processor：将之后观察到的所有项广播给当前全部 Subscriber。
 *
 * <p>
 * <img width="640" height="278" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/PublishProcessor.png" alt="">
 * <p>
 * This processor does not have a public constructor by design; a new empty instance of this
 * {@code PublishProcessor} can be created via the {@link #create()} method.
 * <p>
 * Since a {@code PublishProcessor} is a Reactive Streams {@code Processor} type,
 * {@code null}s are not allowed (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.13">Rule 2.13</a>) as
 * parameters to {@link #onNext(Object)} and {@link #onError(Throwable)}. Such calls will result in a
 * {@link NullPointerException} being thrown and the processor's state is not changed.
 * <p>
 * {@code PublishProcessor} is a {@link io.reactivex.rxjava4.core.Flowable} as well as a {@link FlowableProcessor},
 * however, it does not coordinate backpressure between different subscribers and between an
 * upstream source and a subscriber. If an upstream item is received via {@link #onNext(Object)}, if
 * a subscriber is not ready to receive an item, that subscriber is terminated via a {@link MissingBackpressureException}.
 * To avoid this case, use {@link #offer(Object)} and retry sometime later if it returned false.
 * The {@code PublishProcessor}'s {@link Subscriber}-side consumes items in an unbounded manner.
 * <p>
 * For a multicasting processor type that also coordinates between the downstream {@code Subscriber}s and the upstream
 * source as well, consider using {@link MulticastProcessor}.
 * <p>
 * When this {@code PublishProcessor} is terminated via {@link #onError(Throwable)} or {@link #onComplete()},
 * late {@link Subscriber}s only receive the respective terminal event.
 * <p>
 * Unlike a {@link BehaviorProcessor}, a {@code PublishProcessor} doesn't retain/cache items, therefore, a new
 * {@code Subscriber} won't receive any past items.
 * <p>
 * Even though {@code PublishProcessor} implements the {@link Subscriber} interface, calling
 * {@code onSubscribe} is not required (<a href="https://github.com/reactive-streams/reactive-streams-jvm#2.12">Rule 2.12</a>)
 * if the processor is used as a standalone source. However, calling {@code onSubscribe}
 * after the {@code PublishProcessor} reached its terminal state will result in the
 * given {@link Subscription} being cancelled immediately.
 * <p>
 * Calling {@link #onNext(Object)}, {@link #offer(Object)}, {@link #onError(Throwable)} and {@link #onComplete()}
 * is required to be serialized (called from the same thread or called non-overlappingly from different threads
 * through external means of serialization). The {@link #toSerialized()} method available to all {@link FlowableProcessor}s
 * provides such serialization and also protects against reentrance (i.e., when a downstream {@code Subscriber}
 * consuming this processor also wants to call {@link #onNext(Object)} on this processor recursively).
 * Note that serializing over {@link #offer(Object)} is not supported through {@code toSerialized()} because it is a method
 * available on the {@code PublishProcessor} and {@code BehaviorProcessor} classes only.
 * <p>
 * This {@code PublishProcessor} supports the standard state-peeking methods {@link #hasComplete()}, {@link #hasThrowable()},
 * {@link #getThrowable()} and {@link #hasSubscribers()}.
 * <dl>
 *  <dt><b>背压：</b></dt>
 *  <dd>The processor does not coordinate backpressure for its subscribers and implements a weaker {@code onSubscribe} which
 *  calls requests {@link Long#MAX_VALUE} from the incoming Subscriptions. This makes it possible to subscribe the {@code PublishProcessor}
 *  to multiple sources (note on serialization though) unlike the standard {@code Subscriber} contract. Child subscribers, however, are not overflown but receive an
 *  {@link IllegalStateException} in case their requested amount is zero.</dd>
 *  <dt><b>调度器：</b></dt>
 *  <dd>{@code PublishProcessor} does not operate by default on a particular {@link io.reactivex.rxjava4.core.Scheduler} and
 *  the {@code Subscriber}s get notified on the thread the respective {@code onXXX} methods were invoked.</dd>
 *  <dt><b>错误处理：</b></dt>
 *  <dd>When the {@link #onError(Throwable)} is called, the {@code PublishProcessor} enters into a terminal state
 *  and emits the same {@code Throwable} instance to the last set of {@code Subscriber}s. During this emission,
 *  if one or more {@code Subscriber}s cancel their respective {@code Subscription}s, the
 *  {@code Throwable} is delivered to the global error handler via
 *  {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable)} (multiple times if multiple {@code Subscriber}s
 *  cancel at once).
 *  If there were no {@code Subscriber}s subscribed to this {@code PublishProcessor} when the {@code onError()}
 *  was called, the global error handler is not invoked.
 *  </dd>
 * </dl>
 *
 * 用法示例：
 * <pre> {@code

    PublishProcessor<Object> processor = PublishProcessor.create();
    // subscriber1 will receive all onNext and onComplete events
    processor.subscribe(subscriber1);
    processor.onNext("one");
    processor.onNext("two");
    // subscriber2 will only receive "three" and onComplete
    processor.subscribe(subscriber2);
    processor.onNext("three");
    processor.onComplete();

    } </pre>
 * @param <T> value 类型 multicasted to Subscribers.
 * @see MulticastProcessor
 */
public /** 内部 PublishProcessor：单播队列 drain 与终端处理。 */
 final class PublishProcessor<@NonNull T> extends FlowableProcessor<T> {
    /** The terminated indicator for 订阅者s array. */
    @SuppressWarnings("rawtypes")
    static final PublishSubscription[] TERMINATED = new PublishSubscription[0];
    /** An empty subscribers array to avoid allocating it all the time. */
    @SuppressWarnings("rawtypes")
    static final PublishSubscription[] EMPTY = new PublishSubscription[0];

    /** The array of currently subscribed subscribers. */
    final AtomicReference<PublishSubscription<T>[]> subscribers;

    /** The error, write before terminating and read after checking subscribers. */
    Throwable error;

    /**
     * Constructs a PublishProcessor.
     * @param <T> value 类型
     * @return  new PublishProcessor
     */
    @CheckReturnValue
    @NonNull
    public static <T> PublishProcessor<T> create() {
        return new PublishProcessor<>();
    }

    /**
 * 【说明】Constructs a PublishProcessor.；/
     * Constructs a PublishProcessor.
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    PublishProcessor() {
        subscribers = new AtomicReference<PublishSubscription<T>[]>(EMPTY);
    }

    /** 组装内部 Subscriber/Observer 并订阅上游。 */


    @Override


    protected void subscribeActual(@NonNull Subscriber<? super T> t) {
        PublishSubscription<T> ps = new PublishSubscription<>(t, this);
        t.onSubscribe(ps);
        if (add(ps)) {
            // if cancellation happened while a successful add, the remove() didn't work
            // so we need to do it again
            if (ps.isCancelled()) {
                remove(ps);
            }
        } else {
            Throwable ex = error;
            if (ex != null) {
                t.onError(ex);
            } else {
                t.onComplete();
            }
        }
    }

    /**
     * Tries to add the given subscriber to 订阅者s array atomically
     * or returns false if this processor has terminated.
     * @param ps 订阅者 to add
     * @return true if successful, false if this processor has terminated
     */
    boolean add(PublishSubscription<T> ps) {
        for (;;) {
            PublishSubscription<T>[] a = subscribers.get();
            if (a == TERMINATED) {
                return false;
            }

            int n = a.length;
            @SuppressWarnings("unchecked")
            PublishSubscription<T>[] b = new PublishSubscription[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = ps;

            if (subscribers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    /**
 * 【说明】Atomically removes the given subscriber if it is subscribed to this processor.；/
     * Atomically removes the given subscriber if it is subscribed to this processor.
     * @param ps Subscription wrapping a subscriber to remove
     */
    @SuppressWarnings("unchecked")
    void remove(PublishSubscription<T> ps) {
        for (;;) {
            PublishSubscription<T>[] a = subscribers.get();
            if (a == TERMINATED || a == EMPTY) {
                return;
            }

            int n = a.length;
            int j = -1;
            for (int i = 0; i < n; i++) {
                if (a[i] == ps) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }

            PublishSubscription<T>[] b;

            if (n == 1) {
                b = EMPTY;
            } else {
                b = new PublishSubscription[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (subscribers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    @Override
    /** 实例方法 void：Processor/Subject 状态或发射 API。 */

    public void onSubscribe(@NonNull Subscription s) {
        if (subscribers.get() == TERMINATED) {
            s.cancel();
            return;
        }
        // PublishProcessor doesn't bother with request coordination.
        s.request(Long.MAX_VALUE);
    }

    @Override
    /** 实例方法 void：Processor/Subject 状态或发射 API。 */

    public void onNext(@NonNull T t) {
        ExceptionHelper.nullCheck(t, "onNext called with a null value.");
        for (PublishSubscription<T> s : subscribers.get()) {
            s.onNext(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    /** 实例方法 void：Processor/Subject 状态或发射 API。 */

    public void onError(@NonNull Throwable t) {
        ExceptionHelper.nullCheck(t, "onError called with a null Throwable.");
        if (subscribers.get() == TERMINATED) {
            RxJavaPlugins.onError(t);
            return;
        }
        error = t;

        for (PublishSubscription<T> s : subscribers.getAndSet(TERMINATED)) {
            s.onError(t);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    /** 实例方法 void：Processor/Subject 状态或发射 API。 */

    public void onComplete() {
        if (subscribers.get() == TERMINATED) {
            return;
        }
        for (PublishSubscription<T> s : subscribers.getAndSet(TERMINATED)) {
            s.onComplete();
        }
    }

    /**
     * Tries to emit 项 to all currently subscribed {@link Subscriber}s if all of them
     * has requested some value, returns {@code false} otherwise.
     * <p>
     * This method should be called in a sequential manner just like the {@code onXXX} methods
     * of this {@code PublishProcessor}.
     * <p>版本历史： 2.0.8 - experimental
     * @param t 项 to emit, not {@code null}
     * @return {@code true} if 项 was emitted to all {@code Subscriber}s
     * @throws NullPointerException 若参数为 null {@code t} is {@code null}
     * @since 2.2
     */
    @CheckReturnValue
    /** 实例方法 boolean：Processor/Subject 状态或发射 API。 */

    /** 非阻塞 offer：Subscriber/Observer 就绪时尝试发射。 */


    public boolean offer(@NonNull T t) {
        ExceptionHelper.nullCheck(t, "offer called with a null value.");

        PublishSubscription<T>[] array = subscribers.get();

        for (PublishSubscription<T> s : array) {
            if (s.isFull()) {
                return false;
            }
        }

        for (PublishSubscription<T> s : array) {
            s.onNext(t);
        }
        return true;
    }

    @Override
    @CheckReturnValue
    /** 实例方法 boolean：Processor/Subject 状态或发射 API。 */

    /** 是否存在活跃 Subscriber。 */


    public boolean hasSubscribers() {
        return subscribers.get().length != 0;
    }

    @Override
    @Nullable
    @CheckReturnValue
    /** 实例方法 Throwable：Processor/Subject 状态或发射 API。 */

    /** 返回终端 Throwable（若有）。 */


    public Throwable getThrowable() {
        if (subscribers.get() == TERMINATED) {
            return error;
        }
        return null;
    }

    @Override
    @CheckReturnValue
    /** 实例方法 boolean：Processor/Subject 状态或发射 API。 */

    /** 是否因错误终止。 */


    public boolean hasThrowable() {
        return subscribers.get() == TERMINATED && error != null;
    }

    @Override
    @CheckReturnValue
    /** 实例方法 boolean：Processor/Subject 状态或发射 API。 */

    /** 是否已正常完成（onComplete）。 */


    public boolean hasComplete() {
        return subscribers.get() == TERMINATED && error == null;
    }

    /**
     * 包装 actual subscriber, tracks its requests and makes cancellation
     * to remove itself from the current subscribers array.
     *
     * @param <T> value 类型
     */
    /** 内部 PublishSubscription：协调订阅/背压/重放。 */

    static /** 内部 PublishSubscription：单播队列 drain 与终端处理。 */
 final class PublishSubscription<@NonNull T> extends AtomicLong implements Subscription {

        @Serial
        private static final long serialVersionUID = 3562861878281475070L;
        /** The actual subscriber. */
        final Subscriber<? super T> downstream;
        /** The parent processor servicing this subscriber. */
        final PublishProcessor<T> parent;

        /**
 * 【说明】Constructs a PublishSubscriber, wraps the actual subscriber and the state.；/
         * Constructs a PublishSubscriber, wraps the actual subscriber and the state.
         * @param actual the actual subscriber
         * @param parent the parent PublishProcessor
         */
        PublishSubscription(Subscriber<? super T> actual, PublishProcessor<T> parent) {
            this.downstream = actual;
            this.parent = parent;
        }

        /** 实例方法 void：Processor/Subject 状态或发射 API。 */


        public void onNext(T t) {
            long r = get();
            if (r == Long.MIN_VALUE) {
                return;
            }
            if (r != 0L) {
                downstream.onNext(t);
                BackpressureHelper.producedCancel(this, 1);
            } else {
                cancel();
                downstream.onError(MissingBackpressureException.createDefault());
            }
        }

        /** 实例方法 void：Processor/Subject 状态或发射 API。 */


        public void onError(Throwable t) {
            if (get() != Long.MIN_VALUE) {
                downstream.onError(t);
            } else {
                RxJavaPlugins.onError(t);
            }
        }

        /** 实例方法 void：Processor/Subject 状态或发射 API。 */


        public void onComplete() {
            if (get() != Long.MIN_VALUE) {
                downstream.onComplete();
            }
        }

        @Override
        /** 实例方法 void：Processor/Subject 状态或发射 API。 */

        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.addCancel(this, n);
            }
        }

        @Override
        /** 实例方法 void：Processor/Subject 状态或发射 API。 */

        public void cancel() {
            if (getAndSet(Long.MIN_VALUE) != Long.MIN_VALUE) {
                parent.remove(this);
            }
        }

        /** 实例方法 boolean：Processor/Subject 状态或发射 API。 */


        public boolean isCancelled() {
            return get() == Long.MIN_VALUE;
        }

        boolean isFull() {
            return get() == 0L;
        }
    }
}
