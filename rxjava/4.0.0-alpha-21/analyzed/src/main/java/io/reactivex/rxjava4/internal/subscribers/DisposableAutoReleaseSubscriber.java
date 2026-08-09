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

/*
 * Copyright 2016-2019 David Karnok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactivex.rxjava4.internal.subscribers;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.FlowableSubscriber;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.functions.Functions;
import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava4.observers.LambdaConsumerIntrospection;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 包装 lambda 回调；上游终止或本 subscriber 被 dispose 时，
 * 从 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 中移除自身。
 * <p>History: 0.18.0 @ RxJavaExtensions
 * @param <T> 消费的元素类型
 * @since 3.1.0
 */
public final class DisposableAutoReleaseSubscriber<T>
extends AtomicReference<Subscription>
implements FlowableSubscriber<T>, Disposable, LambdaConsumerIntrospection {

    @Serial
    private static final long serialVersionUID = 8924480688481408726L;

    final AtomicReference<DisposableContainer> composite;

    final Consumer<? super T> onNext;

    final Consumer<? super Throwable> onError;

    final Action onComplete;

    /**
     * @param composite 要从中移除自身的复合容器
     * @param onNext 下一项回调
     * @param onError 错误回调
     * @param onComplete 完成回调
     */
    public DisposableAutoReleaseSubscriber(
            DisposableContainer composite,
            Consumer<? super T> onNext,
            Consumer<? super Throwable> onError,
            Action onComplete
    ) {
        this.onNext = onNext;
        this.onError = onError;
        this.onComplete = onComplete;
        this.composite = new AtomicReference<>(composite);
    }

    @Override
    public void onNext(T t) {
        if (get() != SubscriptionHelper.CANCELLED) {
            try {
                onNext.accept(t);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                get().cancel();
                onError(e);
            }
        }
    }

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
        removeSelf();
    }

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
        removeSelf();
    }

    @Override
    public void dispose() {
        SubscriptionHelper.cancel(this);
        removeSelf();
    }

    /** 从 CompositeDisposable 中删除自身引用。 */
    void removeSelf() {
        DisposableContainer c = composite.getAndSet(null);
        if (c != null) {
            c.delete(this);
        }
    }

    @Override
    public boolean isDisposed() {
        return SubscriptionHelper.CANCELLED == get();
    }

    /** 设置 subscription 并无界 request 上游。 */
    @Override
    public void onSubscribe(Subscription s) {
        if (SubscriptionHelper.setOnce(this, s)) {
            s.request(Long.MAX_VALUE);
        }
    }

    @Override
    public boolean hasCustomOnError() {
        return onError != Functions.ON_ERROR_MISSING;
    }

}
