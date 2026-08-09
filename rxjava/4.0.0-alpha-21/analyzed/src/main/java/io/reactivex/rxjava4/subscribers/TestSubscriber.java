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

package io.reactivex.rxjava4.subscribers;

import java.util.Objects;
import java.util.concurrent.Flow.*;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.observers.BaseTestConsumer;

/**
 * 记录事件的 {@link Subscriber} 实现，支持断言。
 *
 * <p>可覆盖 onSubscribe/onNext/onError/onComplete，其余方法不可覆盖。
 *
 * <p>request 代表被包装的实际 Subscriber 向上游请求。
 *
 * @param <T> 元素类型
 */
public class TestSubscriber<T>
extends BaseTestConsumer<T, TestSubscriber<T>>
implements FlowableSubscriber<T>, Subscription {
    /** 转发事件的实际 Subscriber。 */
    private final Subscriber<? super T> downstream;

    /** 标记是否已 cancel。 */
    private volatile boolean cancelled;

    /** 当前 Subscription（若有）。 */
    private final AtomicReference<Subscription> upstream;

    /** 在 Subscription 到达前缓存 request 数量。 */
    private final AtomicLong missedRequested;

    /**
     * 创建初始 request 为 {@link Long#MAX_VALUE} 的 TestSubscriber。
     * @param <T> 元素类型
     * @return 新实例
     * @see #create(long)
     */
    @NonNull
    public static <T> TestSubscriber<T> create() {
        return new TestSubscriber<>();
    }

    /**
     * 以给定初始 request 创建 TestSubscriber。
     * @param initialRequested 初始请求量
     * @return 新实例
     */
    @NonNull
    public static <T> TestSubscriber<T> create(long initialRequested) {
        return new TestSubscriber<>(initialRequested);
    }

    /**
     * 创建转发事件的 TestSubscriber。
     * @param delegate 转发目标 Subscriber
     * @return 新实例
     */
    public static <T> TestSubscriber<T> create(@NonNull Subscriber<? super T> delegate) {
        return new TestSubscriber<>(delegate);
    }

    /** 构造不转发、初始 request 为 MAX_VALUE 的 TestSubscriber。 */
    public TestSubscriber() {
        this(EmptySubscriber.INSTANCE, Long.MAX_VALUE);
    }

    /**
     * 构造不转发、指定初始 request 的 TestSubscriber（不校验 initialRequest）。
     * @param initialRequest 初始请求量
     */
    public TestSubscriber(long initialRequest) {
        this(EmptySubscriber.INSTANCE, initialRequest);
    }

    /**
     * Constructs a forwarding {@code TestSubscriber} but leaves the requesting to the wrapped {@link Subscriber}.
     * @param downstream the actual {@code Subscriber} to forward events to
     */
    public TestSubscriber(@NonNull Subscriber<? super T> downstream) {
        this(downstream, Long.MAX_VALUE);
    }

    /**
     * Constructs a forwarding {@code TestSubscriber} with the specified initial request amount
     * and an actual {@link Subscriber} to forward events to.
     * <p>The {@code TestSubscriber} doesn't validate the initialRequest value so one can
     * test sources with invalid values as well.
     * @param actual the actual {@code Subscriber} to forward events to
     * @param initialRequest the initial request amount
     */
    public TestSubscriber(@NonNull Subscriber<? super T> actual, long initialRequest) {
        super();
        if (initialRequest < 0) {
            throw new IllegalArgumentException("Negative initial request not allowed");
        }
        this.downstream = actual;
        this.upstream = new AtomicReference<>();
        this.missedRequested = new AtomicLong(initialRequest);
    }

    /** 记录线程与 upstream，flush missedRequested 后 onStart。 */
    @Override
    public void onSubscribe(@NonNull Subscription s) {
        try {
            lastThread = Thread.currentThread();

            if (s == null) {
                errors.add(new NullPointerException("onSubscribe received a null Subscription"));
                return;
            }
            if (!upstream.compareAndSet(null, s)) {
                s.cancel();
                if (upstream.get() != SubscriptionHelper.CANCELLED) {
                    errors.add(new IllegalStateException("onSubscribe received multiple subscriptions: " + s));
                }
                return;
            }

            downstream.onSubscribe(s);

            long mr = missedRequested.getAndSet(0L);
            if (mr != 0L) {
                s.request(mr);
            }

            onStart();
        } finally {
            onSubscribeReady.countDown();
        }
    }

    /** onSubscribe 处理完成后的钩子方法。 */
    protected void onStart() {

    }

    /** 记录值与线程，校验订阅顺序后转发 downstream。 */
    @Override
    public void onNext(@NonNull T t) {
        if (!checkSubscriptionOnce) {
            checkSubscriptionOnce = true;
            if (upstream.get() == null) {
                errors.add(new IllegalStateException("onSubscribe not called in proper order"));
            }
        }
        lastThread = Thread.currentThread();

        values.add(t);

        if (t == null) {
            errors.add(new NullPointerException("onNext received a null value"));
        }

        downstream.onNext(t);
    }

    /** 记录错误，转发 downstream 并 countDown done。 */
    @Override
    public void onError(@NonNull Throwable t) {
        if (!checkSubscriptionOnce) {
            checkSubscriptionOnce = true;
            if (upstream.get() == null) {
                errors.add(new IllegalStateException("onSubscribe not called in proper order"));
            }
        }
        try {
            lastThread = Thread.currentThread();

            errors.add(Objects.requireNonNullElseGet(t, () -> new NullPointerException("onError received a null Throwable")));

            downstream.onError(t);
        } finally {
            done.countDown();
        }
    }

    /** 递增 completions，转发 downstream 并 countDown done。 */
    @Override
    public void onComplete() {
        if (!checkSubscriptionOnce) {
            checkSubscriptionOnce = true;
            if (upstream.get() == null) {
                errors.add(new IllegalStateException("onSubscribe not called in proper order"));
            }
        }
        try {
            lastThread = Thread.currentThread();
            completions++;

            downstream.onComplete();
        } finally {
            done.countDown();
        }
    }

    /** deferredRequest 向上游请求。 */
    @Override
    public final void request(long n) {
        SubscriptionHelper.deferredRequest(upstream, missedRequested, n);
    }

    /** 取消 upstream 并置 cancelled。 */
    @Override
    public final void cancel() {
        if (!cancelled) {
            cancelled = true;
            SubscriptionHelper.cancel(upstream);
        }
    }

    /**
     * 是否已 cancel。
     * @return 已 cancel 则为 true
     */
    public final boolean isCancelled() {
        return cancelled;
    }

    @Override
    protected final void dispose() {
        cancel();
    }

    @Override
    protected final boolean isDisposed() {
        return cancelled;
    }

    // state retrieval methods

    /**
     * 是否已通过 onSubscribe 收到 Subscription。
     * @return 已收到则为 true
     */
    public final boolean hasSubscription() {
        return upstream.get() != null;
    }

    // assertion methods

    /**
     * 断言 onSubscribe 恰好调用一次。
     * @return this
     */
    @Override
    protected final TestSubscriber<T> assertSubscribed() {
        if (upstream.get() == null) {
            throw fail("Not subscribed!");
        }
        return this;
    }

    /**
     * 调用 request(n) 并返回 this。
     * @param n 请求数量
     * @return this
     */
    public final TestSubscriber<T> requestMore(long n) {
        request(n);
        return this;
    }

    /**
     * 将本 TestSubscriber 暴露为 {@link Disposable} 视图。
     * @return Disposable 包装
     * @since 4.0.0
     */
    public final Disposable asDisposable() {
        return new TestSubscriberDisposable(this);
    }

    /** 忽略所有事件且不上报错误的 Subscriber。 */
    enum EmptySubscriber implements FlowableSubscriber<Object> {
        INSTANCE;

        @Override
        public void onSubscribe(Subscription s) {
        }

        @Override
        public void onNext(Object t) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
        }
    }

    record TestSubscriberDisposable(TestSubscriber<?> to) implements Disposable {

        @Override
        public void dispose() {
            to.dispose();
        }

        @Override
        public boolean isDisposed() {
            return to.isDisposed();
        }
    }
}
