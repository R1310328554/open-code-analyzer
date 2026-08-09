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

package io.reactivex.rxjava4.internal.operators.single;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Consumer;
import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 上游调用 onSubscribe 时先执行 onSubscribe Consumer，再转发 Disposable 给 downstream。
 * 回调异常则 dispose 上游并以 EmptyDisposable.error 终止；done 门控忽略后续事件。
 * @param <T> 元素类型
 */
public final class SingleDoOnSubscribe<T> extends Single<T> {

    final SingleSource<T> source;

    final Consumer<? super Disposable> onSubscribe;

    /**
     * @param source 上游 SingleSource
     * @param onSubscribe 收到 Disposable 时执行的 Consumer
     */
    public SingleDoOnSubscribe(SingleSource<T> source, Consumer<? super Disposable> onSubscribe) {
        this.source = source;
        this.onSubscribe = onSubscribe;
    }

    /** 订阅 DoOnSubscribeSingleObserver 拦截 onSubscribe。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {
        source.subscribe(new DoOnSubscribeSingleObserver<>(observer, onSubscribe));
    }

    /** done 标志：onSubscribe 失败后忽略 onSuccess/onError。 */
    static final class DoOnSubscribeSingleObserver<T> implements SingleObserver<T> {

        final SingleObserver<? super T> downstream;

        final Consumer<? super Disposable> onSubscribe;

        boolean done;

        DoOnSubscribeSingleObserver(SingleObserver<? super T> actual, Consumer<? super Disposable> onSubscribe) {
            this.downstream = actual;
            this.onSubscribe = onSubscribe;
        }

        /** accept 成功则 downstream.onSubscribe；异常则 done 并 error 终止。 */
        @Override
        public void onSubscribe(Disposable d) {
            try {
                onSubscribe.accept(d);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                done = true;
                d.dispose();
                EmptyDisposable.error(ex, downstream);
                return;
            }

            downstream.onSubscribe(d);
        }

        @Override
        public void onSuccess(T value) {
            if (done) {
                return;
            }
            downstream.onSuccess(value);
        }

        @Override
        public void onError(Throwable e) {
            if (done) {
                RxJavaPlugins.onError(e);
                return;
            }
            downstream.onError(e);
        }
    }

}
