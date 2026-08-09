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
 * 带预取缓冲的 lambda {@link FlowableSubscriber}；
 * 消费达到 limit 时向上游补 request，实现有界背压。
 *
 * @param <T> 元素类型
 */
public final class BoundedSubscriber<T> extends AtomicReference<Subscription>
        implements FlowableSubscriber<T>, Subscription, Disposable, LambdaConsumerIntrospection {

    @Serial
    private static final long serialVersionUID = -7251123623727029452L;
    final Consumer<? super T> onNext;
    final Consumer<? super Throwable> onError;
    final Action onComplete;
    final Consumer<? super Subscription> onSubscribe;

    final int bufferSize;
    int consumed;
    final int limit;

    /**
     * @param onNext 下一项回调
     * @param onError 错误回调
     * @param onComplete 完成回调
     * @param onSubscribe 订阅回调
     * @param bufferSize 预取缓冲大小
     */
    public BoundedSubscriber(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
                            Action onComplete, Consumer<? super Subscription> onSubscribe, int bufferSize) {
        super();
        this.onNext = onNext;
        this.onError = onError;
        this.onComplete = onComplete;
        this.onSubscribe = onSubscribe;
        this.bufferSize = bufferSize;
        this.limit = bufferSize - (bufferSize >> 2);
    }

    @Override
    public void onSubscribe(Subscription s) {
        if (SubscriptionHelper.setOnce(this, s)) {
            try {
                onSubscribe.accept(this);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                s.cancel();
                onError(e);
            }
        }
    }

    @Override
    public void onNext(T t) {
        if (!isDisposed()) {
            try {
                onNext.accept(t);

                int c = consumed + 1;
                if (c == limit) {
                    consumed = 0;
                    get().request(limit);
                } else {
                    consumed = c;
                }
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
    }

    @Override
    public void dispose() {
        cancel();
    }

    @Override
    public boolean isDisposed() {
        return get() == SubscriptionHelper.CANCELLED;
    }

    @Override
    public void request(long n) {
        get().request(n);
    }

    @Override
    public void cancel() {
        SubscriptionHelper.cancel(this);
    }

    /** 若 onError 不是默认的 ON_ERROR_MISSING 则返回 true。 */
    @Override
    public boolean hasCustomOnError() {
        return onError != Functions.ON_ERROR_MISSING;
    }
}
