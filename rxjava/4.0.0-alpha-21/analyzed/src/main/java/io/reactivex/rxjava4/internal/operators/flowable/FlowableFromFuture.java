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

package io.reactivex.rxjava4.internal.operators.flowable;

import java.util.concurrent.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.Flowable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.internal.subscriptions.DeferredScalarSubscription;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;

/**
 * 阻塞等待 {@link Future} 完成并将其结果作为唯一元素发出。
 * @param <T> 元素类型
 */
public final class FlowableFromFuture<T> extends Flowable<T> {
    final Future<? extends T> future;
    final long timeout;
    final TimeUnit unit;

    /**
     * @param future 要等待的 Future
     * @param timeout 超时时间（unit 为 null 时不限时）
     * @param unit 超时时间单位
     */
    public FlowableFromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {
        this.future = future;
        this.timeout = timeout;
        this.unit = unit;
    }

    /** 调用 future.get 并通过 DeferredScalarSubscription 发出结果。 */
    @Override
    public void subscribeActual(Subscriber<? super T> s) {
        DeferredScalarSubscription<T> deferred = new DeferredScalarSubscription<>(s);
        s.onSubscribe(deferred);

        T v;
        try {
            v = unit != null ? future.get(timeout, unit) : future.get();
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            if (!deferred.isCancelled()) {
                s.onError(ex);
            }
            return;
        }
        if (v == null) {
            s.onError(ExceptionHelper.createNullPointerException("The future returned a null value."));
        } else {
            deferred.complete(v);
        }
    }
}
