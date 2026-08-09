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
import io.reactivex.rxjava4.disposables.Disposable;

/**
 * 将 {@link ObservableSource} 转为 {@link Completable}，忽略 onNext，仅传递 onComplete/onError。
 * @param <T> Observable 元素类型
 */
public final class CompletableFromObservable<T> extends Completable {

    final ObservableSource<T> observable;

    /** @param observable 上游 ObservableSource */
    public CompletableFromObservable(ObservableSource<T> observable) {
        this.observable = observable;
    }

    @Override
    protected void subscribeActual(final CompletableObserver observer) {
        observable.subscribe(new CompletableFromObservableObserver<>(observer));
    }

    record CompletableFromObservableObserver<T>(CompletableObserver co) implements Observer<T> {

        @Override
            public void onSubscribe(Disposable d) {
                co.onSubscribe(d);
            }

            @Override
            /** 故意忽略 onNext 值。 */
            public void onNext(T value) {
                // Deliberately ignored.
            }

            @Override
            public void onError(Throwable e) {
                co.onError(e);
            }

            @Override
            public void onComplete() {
                co.onComplete();
            }
        }
}
