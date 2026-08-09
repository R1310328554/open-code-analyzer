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

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.CompletableObserver;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 包装另一个 {@link CompletableObserver}，捕获其 {@code onSubscribe}、
 * {@code onError} 或 {@code onComplete} 方法抛出的异常（尽管协议禁止如此）。
 * <p>
 * 此类异常会路由到 {@link RxJavaPlugins#onError(Throwable)} 处理器。
 *
 * @since 3.0.0
 */
public final class SafeCompletableObserver implements CompletableObserver {

    final CompletableObserver downstream;

    boolean onSubscribeFailed;

    /** @param downstream 被包装的下游 CompletableObserver */
    public SafeCompletableObserver(CompletableObserver downstream) {
        this.downstream = downstream;
    }

    /** 安全调用下游 onSubscribe；异常时 dispose 上游并上报 RxJavaPlugins。 */
    @Override
    public void onSubscribe(@NonNull Disposable d) {
        try {
            downstream.onSubscribe(d);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            onSubscribeFailed = true;
            d.dispose();
            RxJavaPlugins.onError(ex);
        }
    }

    /** 安全转发错误或上报；onSubscribe 失败时仅上报。 */
    @Override
    public void onError(@NonNull Throwable e) {
        if (onSubscribeFailed) {
            RxJavaPlugins.onError(e);
        } else {
            try {
                downstream.onError(e);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                RxJavaPlugins.onError(new CompositeException(e, ex));
            }
        }
    }

    /** 安全转发完成信号；onSubscribe 失败时忽略。 */
    @Override
    public void onComplete() {
        if (!onSubscribeFailed) {
            try {
                downstream.onComplete();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                RxJavaPlugins.onError(ex);
            }
        }
    }
}
