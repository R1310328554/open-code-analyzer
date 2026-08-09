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

package io.reactivex.rxjava4.internal.subscribers;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.functions.Functions;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.observers.LambdaConsumerIntrospection;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 基于回调的 Flowable 订阅者：同时实现 {@link Subscription} 与 {@link Disposable}，
 * 将 onNext/onError/onComplete/onSubscribe 委托给对应 {@link Consumer}/{@link Action}。
 */
public final class LambdaSubscriber<T> extends AtomicReference<Subscription>
        implements FlowableSubscriber<T>, Subscription, Disposable, LambdaConsumerIntrospection {

    @Serial
    private static final long serialVersionUID = -7251123623727029452L;
    final Consumer<? super T> onNext;
    final Consumer<? super Throwable> onError;
    final Action onComplete;
    final Consumer<? super Subscription> onSubscribe;

    /** @param onNext/onError/onComplete/onSubscribe 各阶段回调 */
    public LambdaSubscriber(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
            Action onComplete,
            Consumer<? super Subscription> onSubscribe) {
        super();
        this.onNext = onNext;
        this.onError = onError;
        this.onComplete = onComplete;
        this.onSubscribe = onSubscribe;
    }

    /** setOnce 成功后调用 onSubscribe.accept(this)。 */
    @Override
    public void onSubscribe(Subscription s) {
        if (SubscriptionHelper.setOnce(this, s)) {
            try {
                onSubscribe.accept(this);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                s.cancel();
                onError(ex);
            }
        }
    }

    /** 未 dispose 时 accept(t)；异常则 cancel 并 onError。 */
    @Override
    public void onNext(T t) {
        if (!isDisposed()) {
            try {
                onNext.accept(t);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                get().cancel();
                onError(e);
            }
        }
    }

    /** 置 CANCELLED 后调用 onError；已取消则上报 RxJavaPlugins。 */
    @Override
    public void onError(Throwable t) {
        if (get() != SubscriptionHelper.CANCELLED) {
            lazySet(SubscriptionHelper.CANCELLED);
            try {
                onError.accept(t);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                RxJavaPlugins.onError(new CompositeException(t, e));
            }
        } else {
            RxJavaPlugins.onError(t);
        }
    }

    /** 置 CANCELLED 后运行 onComplete.run()。 */
    @Override
    public void onComplete() {
        if (get() != SubscriptionHelper.CANCELLED) {
            lazySet(SubscriptionHelper.CANCELLED);
            try {
                onComplete.run();
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                RxJavaPlugins.onError(e);
            }
        }
    }

    /** 等价于 cancel()。 */
    @Override
    public void dispose() {
        cancel();
    }

    /** 当前 Subscription 是否为 CANCELLED。 */
    @Override
    public boolean isDisposed() {
        return get() == SubscriptionHelper.CANCELLED;
    }

    /** 转发 request 至上游 Subscription。 */
    @Override
    public void request(long n) {
        get().request(n);
    }

    /** 调用 SubscriptionHelper.cancel(this)。 */
    @Override
    public void cancel() {
        SubscriptionHelper.cancel(this);
    }

    /** 是否提供了非默认 ON_ERROR_MISSING 的 onError 回调。 */
    @Override
    public boolean hasCustomOnError() {
        return onError != Functions.ON_ERROR_MISSING;
    }
}
