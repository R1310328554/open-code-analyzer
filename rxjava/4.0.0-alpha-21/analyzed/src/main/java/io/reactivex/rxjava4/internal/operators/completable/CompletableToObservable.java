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

package io.reactivex.rxjava4.internal.operators.completable;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.internal.operators.observable.ObservableFromCompletable;

/**
 * 包装 Completable 并以 {@link Observable} 形式暴露。
 *
 * @param <T> 值类型
 */
public final class CompletableToObservable<T> extends Observable<T> {

    final CompletableSource source;

    /** @param source 上游 CompletableSource */
    public CompletableToObservable(CompletableSource source) {
        this.source = source;
    }

    /** 通过 {@link ObservableFromCompletable.FromCompletableObserver} 订阅上游。 */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new ObservableFromCompletable.FromCompletableObserver<>(observer));
    }
}
