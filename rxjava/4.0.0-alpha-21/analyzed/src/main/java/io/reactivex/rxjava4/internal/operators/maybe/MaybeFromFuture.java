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

package io.reactivex.rxjava4.internal.operators.maybe;

import java.util.concurrent.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.Exceptions;

/**
 * 等待 {@link Future} 完成或超时；
 * {@code null} 结果触发 onComplete 而非 onSuccess。
 *
 * @param <T> 元素类型
 */
public final class MaybeFromFuture<T> extends Maybe<T> {

    final Future<? extends T> future;

    final long timeout;

    final TimeUnit unit;

    /**
     * @param future 待等待的 Future
     * @param timeout 超时时间（≤0 表示无限等待）
     * @param unit 时间单位
     */
    public MaybeFromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {
        this.future = future;
        this.timeout = timeout;
        this.unit = unit;
    }

    /** 调用 future.get 并按结果或异常转发信号。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        Disposable d = Disposable.empty();
        observer.onSubscribe(d);
        if (!d.isDisposed()) {
            T v;
            try {
                if (timeout <= 0L) {
                    v = future.get();
                } else {
                    v = future.get(timeout, unit);
                }
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                if (ex instanceof ExecutionException) {
                    ex = ex.getCause();
                }
                Exceptions.throwIfFatal(ex);
                if (!d.isDisposed()) {
                    observer.onError(ex);
                }
                return;
            }
            if (!d.isDisposed()) {
                if (v == null) {
                    observer.onComplete();
                } else {
                    observer.onSuccess(v);
                }
            }
        }
    }
}
