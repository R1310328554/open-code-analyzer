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
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.operators.ScalarSupplier;

/**
 * 不发射任何元素的空 {@link Observable} 单例；
 * 订阅后立即 onComplete。实现 {@link ScalarSupplier} 时 null 标量表示空序列。
 */
public final class ObservableEmpty extends Observable<Object> implements ScalarSupplier<Object> {
    public static final Observable<Object> INSTANCE = new ObservableEmpty();

    private ObservableEmpty() {
    }

    /** 经 {@link EmptyDisposable#complete} 立即完成下游。 */
    @Override
    protected void subscribeActual(Observer<? super Object> o) {
        EmptyDisposable.complete(o);
    }

    /** 标量融合路径：null 表示空序列。 */
    @Override
    public Object get() {
        return null; // null scalar is interpreted as being empty
    }
}
