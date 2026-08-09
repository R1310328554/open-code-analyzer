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

package io.reactivex.rxjava4.internal.observers;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.CompletableObserver;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.OnErrorNotImplementedException;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.observers.LambdaConsumerIntrospection;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 无自定义回调的 {@link CompletableObserver}；
 * onError 时通过 {@link OnErrorNotImplementedException} 通知 {@link RxJavaPlugins}。
 */
public final class EmptyCompletableObserver
extends AtomicReference<Disposable>
implements CompletableObserver, Disposable, LambdaConsumerIntrospection {

    @Serial
    private static final long serialVersionUID = -7545121636549663526L;

    @Override
    public void dispose() {
        DisposableHelper.dispose(this);
    }

    @Override
    public boolean isDisposed() {
        return get() == DisposableHelper.DISPOSED;
    }

    /** 无操作完成处理。 */
    @Override
    public void onComplete() {
        // no-op
        lazySet(DisposableHelper.DISPOSED);
    }

    @Override
    public void onError(Throwable e) {
        lazySet(DisposableHelper.DISPOSED);
        RxJavaPlugins.onError(new OnErrorNotImplementedException(e));
    }

    @Override
    public void onSubscribe(Disposable d) {
        DisposableHelper.setOnce(this, d);
    }

    /** 始终返回 false，表示未实现自定义 onError。 */
    @Override
    public boolean hasCustomOnError() {
        return false;
    }
}
