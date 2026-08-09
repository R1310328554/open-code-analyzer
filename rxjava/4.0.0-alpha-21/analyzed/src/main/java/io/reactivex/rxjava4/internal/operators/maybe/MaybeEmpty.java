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
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.operators.ScalarSupplier;

/**
 * 立即向下游发出 onComplete（空 Maybe）。
 */
public final class MaybeEmpty extends Maybe<Object> implements ScalarSupplier<Object> {

    /** 单例空 Maybe 实例。 */
    public static final MaybeEmpty INSTANCE = new MaybeEmpty();

    /** 通过 EmptyDisposable.complete 完成。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super Object> observer) {
        EmptyDisposable.complete(observer);
    }

    @Override
    public Object get() {
        return null; // ScalarCallable 返回 null 表示空源
    }
}
