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

package io.reactivex.rxjava4.internal.operators.observable;

import java.util.concurrent.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.internal.observers.DeferredScalarDisposable;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;

/**
 * 阻塞调用 {@link Future#get}（可选超时）并将单个结果作为 onNext 后 onComplete；
 * 异常经 onError 转发。
 *
 * @param <T> 元素类型
 */
public final class ObservableFromFuture<T> extends Observable<T> {
    final Future<? extends T> future;
    final long timeout;
    final TimeUnit unit;

    /**
     * @param future 待求值的 Future
     * @param timeout 超时量（unit 为 null 时忽略，使用无超时 get）
     * @param unit 超时时间单位
     */
    public ObservableFromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {
        this.future = future;
        this.timeout = timeout;
        this.unit = unit;
    }

    /** 经 DeferredScalarDisposable 阻塞 get future 并 complete 单值。 */
    @Override
    public void subscribeActual(Observer<? super T> observer) {
        DeferredScalarDisposable<T> d = new DeferredScalarDisposable<>(observer);
        observer.onSubscribe(d);
        if (!d.isDisposed()) {
            T v;
            try {
                v = ExceptionHelper.nullCheck(unit != null ? future.get(timeout, unit) : future.get(), "Future returned a null value.");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                if (!d.isDisposed()) {
                    observer.onError(ex);
                }
                return;
            }
            d.complete(v);
        }
    }
}
