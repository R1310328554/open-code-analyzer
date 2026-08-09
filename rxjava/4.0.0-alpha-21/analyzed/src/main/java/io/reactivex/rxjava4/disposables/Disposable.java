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

package io.reactivex.rxjava4.disposables;

import java.util.Objects;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Future;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.functions.Action;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.internal.functions.Functions;

/**
 * 表示可 dispose 的资源或进行中的任务。
 */
public interface Disposable {
    /**
     * dispose 资源，此操作应为幂等的。
     */
    void dispose();

    /**
     * 若本资源已被 dispose 则返回 true。
     * @return 若本资源已被 dispose 则为 true
     */
    boolean isDisposed();

    /**
     * 通过包装 {@link Runnable} 构造 {@code Disposable}，在 dispose 时恰好执行一次。
     * @param run 要包装的 {@code Runnable}
     * @return 新的 {@code Disposable} 实例
     * @throws NullPointerException 若 {@code run} 为 {@code null}
     * @since 3.0.0
     */
    @NonNull
    static Disposable fromRunnable(@NonNull Runnable run) {
        Objects.requireNonNull(run, "run is null");
        return new RunnableDisposable(run);
    }

    /**
     * 通过包装 {@link Action} 构造 {@code Disposable}，在 dispose 时恰好执行一次。
     * @param action 要包装的 {@code Action}
     * @return 新的 {@code Disposable} 实例
     * @throws NullPointerException 若 {@code action} 为 {@code null}
     * @since 3.0.0
     */
    @NonNull
    static Disposable fromAction(@NonNull Action action) {
        Objects.requireNonNull(action, "action is null");
        return new ActionDisposable(action);
    }

    /**
     * 通过包装 {@link Future} 构造 {@code Disposable}，在 dispose 时恰好取消一次。
     * <p>
     * {@code Future} 以 {@code mayInterruptIfRunning == true} 取消。
     * @param future 要包装的 {@code Future}
     * @return 新的 {@code Disposable} 实例
     * @throws NullPointerException 若 {@code future} 为 {@code null}
     * @see #fromFuture(Future, boolean)
     * @since 3.0.0
     */
    @NonNull
    static Disposable fromFuture(@NonNull Future<?> future) {
        Objects.requireNonNull(future, "future is null");
        return fromFuture(future, true);
    }

    /**
     * 通过包装 {@link Future} 构造 {@code Disposable}，在 dispose 时恰好取消一次。
     * @param future 要包装的 {@code Future}
     * @param allowInterrupt 若为 true，则通过 {@code Future.cancel(true)} 取消 future
     * @return 新的 {@code Disposable} 实例
     * @throws NullPointerException 若 {@code future} 为 {@code null}
     * @since 3.0.0
     */
    @NonNull
    static Disposable fromFuture(@NonNull Future<?> future, boolean allowInterrupt) {
        Objects.requireNonNull(future, "future is null");
        return new FutureDisposable(future, allowInterrupt);
    }

    /**
     * 通过包装 {@link Subscription} 构造 {@code Disposable}，在 dispose 时恰好取消一次。
     * @param subscription 要包装的 {@code Subscription}
     * @return 新的 {@code Disposable} 实例
     * @throws NullPointerException 若 {@code subscription} 为 {@code null}
     * @since 3.0.0
     */
    @NonNull
    static Disposable fromSubscription(@NonNull Subscription subscription) {
        Objects.requireNonNull(subscription, "subscription is null");
        return new SubscriptionDisposable(subscription);
    }

    /**
     * 通过包装 {@link AutoCloseable} 构造 {@code Disposable}，在 dispose 时恰好关闭一次。
     * @param autoCloseable 要包装的 {@code AutoCloseable}
     * @return 新的 {@code Disposable} 实例
     * @throws NullPointerException 若 {@code autoCloseable} 为 {@code null}
     * @since 3.0.0
     */
    @NonNull
    static Disposable fromAutoCloseable(@NonNull AutoCloseable autoCloseable) {
        Objects.requireNonNull(autoCloseable, "autoCloseable is null");
        return new AutoCloseableDisposable(autoCloseable);
    }

    /**
     * 通过包装 {@code Disposable} 构造 {@link AutoCloseable}，在关闭返回的 {@code AutoCloseable} 时 dispose。
     * @param disposable {@code Disposable} 实例
     * @return 新的 {@code AutoCloseable} 实例
     * @throws NullPointerException 若 {@code disposable} 为 {@code null}
     * @since 3.0.0
     */
    @NonNull
    static AutoCloseable toAutoCloseable(@NonNull Disposable disposable) {
        Objects.requireNonNull(disposable, "disposable is null");
        return disposable::dispose;
    }

    /**
     * 将本 {@code Disposable} 包装为可用于 try-with-resources 的 {@link AutoCloseable} 实例。
     * @return 新的 {@code AutoCloseable} 实例
     * @since 4.0.0
     */
    @NonNull
    default AutoCloseable asAutoCloseable() {
        return this::dispose;
    }

    /**
     * 返回新的、未 dispose 的 {@code Disposable} 实例。
     * @return 新的、未 dispose 的 {@code Disposable} 实例
     * @since 3.0.0
     */
    @NonNull
    static Disposable empty() {
        return fromRunnable(Functions.EMPTY_RUNNABLE);
    }

    /**
     * 返回共享的、已 dispose 的 {@code Disposable} 实例。
     * @return 共享的、已 dispose 的 {@code Disposable} 实例
     * @since 3.0.0
     */
    @NonNull
    static Disposable disposed() {
        return EmptyDisposable.INSTANCE;
    }
}
