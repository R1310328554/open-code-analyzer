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

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;

/**
 * 订阅后立即 onError 固定 {@link Throwable}。
 *
 * @param <T> 元素类型
 */
public final class MaybeError<T> extends Maybe<T> {

    final Throwable error;

    /** @param error 要发出的异常 */
    public MaybeError(Throwable error) {
        this.error = error;
    }

    /** onSubscribe(Disposable.disposed()) 后 onError。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        observer.onSubscribe(Disposable.disposed());
        observer.onError(error);
    }
}
