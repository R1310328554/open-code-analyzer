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

package io.reactivex.rxjava4.observers;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 组合 {@link Observer}、{@link MaybeObserver}、{@link SingleObserver} 与
 * {@link CompletableObserver}，记录 {@link Observable}、{@link Maybe}、
 * {@link Single} 与 {@link Completable} 的事件并支持断言。
 *
 * <p>可覆盖 {@link #onSubscribe(Disposable)}、{@link #onNext(Object)}、
 * {@link #onError(Throwable)}、{@link #onComplete()} 与 {@link #onSuccess(Object)}，
 * 其余方法不可覆盖（设计如此）。
 *
 * <p>自 4.0.0 起 {@code TestObserver} 不再直接实现 {@link Disposable}；
 * 请用 {@link #asDisposable()} 获取会调用 {@link #dispose()} 的包装。
 * <strong>Implementation note</strong><br>
 * {@code Disposable} 现实现 {@link AutoCloseable}，可避免资源警告。
 *
 * @param <T> 值类型
 * @see io.reactivex.rxjava4.subscribers.TestSubscriber
 */
public class TestObserver<T>
extends BaseTestConsumer<T, TestObserver<T>>
implements Observer<T>, MaybeObserver<T>, SingleObserver<T>, CompletableObserver {
    /** 转发事件的实际 observer。 */
    private final Observer<? super T> downstream;

    /** 保存当前订阅（若有）。 */
    private final AtomicReference<Disposable> upstream = new AtomicReference<>();

    /**
     * 构造不转发事件的 {@code TestObserver}。
     * @param <T> 接收的值类型
     * @return 新的 {@code TestObserver} 实例
     */
    @NonNull
    public static <T> TestObserver<T> create() {
        return new TestObserver<>();
    }

    /**
     * 构造转发事件的 {@code TestObserver}。
     * @param <T> 接收的值类型
     * @param delegate 转发目标 {@link Observer}
     * @return 新的 {@code TestObserver} 实例
     */
    @NonNull
    public static <T> TestObserver<T> create(@NonNull Observer<? super T> delegate) {
        return new TestObserver<>(delegate);
    }

    /** 构造不转发事件的 TestObserver（使用 EmptyObserver）。 */
    public TestObserver() {
        this(EmptyObserver.INSTANCE);
    }

    /**
     * 构造转发事件的 {@code TestObserver}。
     * @param downstream 转发目标 {@link Observer}
     */
    public TestObserver(@NonNull Observer<? super T> downstream) {
        this.downstream = downstream;
    }

    /** 记录订阅线程与 upstream，校验重复订阅后转发 downstream。 */
    @Override
    public void onSubscribe(@NonNull Disposable d) {
        try {
            lastThread = Thread.currentThread();

            if (d == null) {
                errors.add(new NullPointerException("onSubscribe received a null Subscription"));
                return;
            }
            if (!upstream.compareAndSet(null, d)) {
                d.dispose();
                if (upstream.get() != DisposableHelper.DISPOSED) {
                    errors.add(new IllegalStateException("onSubscribe received multiple subscriptions: " + d));
                }
                return;
            }

            downstream.onSubscribe(d);
        } finally {
            onSubscribeReady.countDown();
        }
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

    /** 记录错误与线程，转发 downstream 并 countDown done。 */
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

    /** 通过 DisposableHelper 取消 upstream。 */
    @Override
    public final void dispose() {
        DisposableHelper.dispose(upstream);
    }

    @Override
    public final boolean isDisposed() {
        return DisposableHelper.isDisposed(upstream.get());
    }

    /**
     * 将本 {@code TestObserver} 暴露为 {@link Disposable} 视图。
     * @return 本 {@code TestObserver} 的 {@code Disposable} 包装
     * @since 4.0.0
     */
    public final Disposable asDisposable() {
        return new TestObserverDisposable(this);
    }

    // state retrieval methods
    /**
     * 判断本 {@code TestObserver} 是否已收到订阅。
     * @return 若已收到订阅则为 true
     */
    public final boolean hasSubscription() {
        return upstream.get() != null;
    }

    /**
     * 断言 {@link #onSubscribe(Disposable)} 恰好被调用一次。
     * @return this
     */
    @Override
    @NonNull
    protected final TestObserver<T> assertSubscribed() {
        if (upstream.get() == null) {
            throw fail("Not subscribed!");
        }
        return this;
    }

    /** Single/Maybe 成功：记录 onNext 后 onComplete。 */
    @Override
    public void onSuccess(@NonNull T value) {
        onNext(value);
        onComplete();
    }

    /** 忽略所有事件且不上报错误的 observer。 */
    enum EmptyObserver implements Observer<Object> {
        INSTANCE;

        @Override
        public void onSubscribe(Disposable d) {
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

    record TestObserverDisposable(TestObserver<?> to) implements Disposable {

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
