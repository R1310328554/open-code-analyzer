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

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.internal.operators.observable.ObservableScalarXMap.ScalarDisposable;
import io.reactivex.rxjava4.operators.ScalarSupplier;

/**
 * 发射单个常量标量值的 Observable，实现 {@link ScalarSupplier} 供融合优化。
 * @param <T> 元素类型
 */
public final class ObservableJust<T> extends Observable<T> implements ScalarSupplier<T> {

    private final T value;
    /** @param value 唯一发射的元素 */
    public ObservableJust(final T value) {
        this.value = value;
    }

    /** 经 ScalarDisposable 立即 onNext(value) 并 onComplete。 */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        ScalarDisposable<T> sd = new ScalarDisposable<>(observer, value);
        observer.onSubscribe(sd);
        sd.run();
    }

    /** ScalarSupplier：返回常量 value。 */
    @Override
    public T get() {
        return value;
    }
}
