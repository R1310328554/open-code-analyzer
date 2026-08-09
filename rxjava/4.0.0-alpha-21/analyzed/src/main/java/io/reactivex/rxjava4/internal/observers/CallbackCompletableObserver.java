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
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.functions.Functions;
import io.reactivex.rxjava4.observers.LambdaConsumerIntrospection;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 基于 {@link Consumer} 与 {@link Action} 回调的 {@link CompletableObserver} 实现，
 * 同时作为 {@link Disposable} 管理上游订阅。
 */
public final class CallbackCompletableObserver
extends AtomicReference<Disposable>
        implements CompletableObserver, Disposable, LambdaConsumerIntrospection {

    @Serial
    private static final long serialVersionUID = -4361286194466301354L;

    final Consumer<? super Throwable> onError;
    final Action onComplete;

    /**
     * @param onError 错误回调
     * @param onComplete 完成回调
     */
    public CallbackCompletableObserver(Consumer<? super Throwable> onError, Action onComplete) {
        this.onError = onError;
        this.onComplete = onComplete;
    }

    /** 调用 onComplete 回调并将自身标记为已 dispose。 */
    @Override
    public void onComplete() {
        try {
            onComplete.run();
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            RxJavaPlugins.onError(ex);
        }
        lazySet(DisposableHelper.DISPOSED);
    }

    /** 调用 onError 回调并将自身标记为已 dispose。 */
    @Override
    public void onError(Throwable e) {
        try {
            onError.accept(e);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            RxJavaPlugins.onError(ex);
        }
        lazySet(DisposableHelper.DISPOSED);
    }

    @Override
    public void onSubscribe(Disposable d) {
        DisposableHelper.setOnce(this, d);
    }

    @Override
    public void dispose() {
        DisposableHelper.dispose(this);
    }

    @Override
    public boolean isDisposed() {
        return get() == DisposableHelper.DISPOSED;
    }

    /** 若 onError 不是默认的 ON_ERROR_MISSING 则返回 true。 */
    @Override
    public boolean hasCustomOnError() {
        return onError != Functions.ON_ERROR_MISSING;
    }
}
