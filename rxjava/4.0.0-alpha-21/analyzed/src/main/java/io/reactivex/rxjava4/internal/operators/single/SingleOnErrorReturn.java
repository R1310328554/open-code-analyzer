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
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Function;

/**
 * 上游 onError 时用 valueSupplier 或固定 value 转为 onSuccess。
 * valueSupplier 异常合并 CompositeException；结果为 null 则 NPE 链式转发。
 * @param <T> 元素类型
 */
public final class SingleOnErrorReturn<T> extends Single<T> {
    final SingleSource<? extends T> source;

    final Function<? super Throwable, ? extends T> valueSupplier;

    final T value;

    /**
     * @param source 上游 SingleSource
     * @param valueSupplier 按错误计算替代值的函数，可为 null
     * @param value valueSupplier 为 null 时使用的固定替代值
     */
    public SingleOnErrorReturn(SingleSource<? extends T> source,
            Function<? super Throwable, ? extends T> valueSupplier, T value) {
        this.source = source;
        this.valueSupplier = valueSupplier;
        this.value = value;
    }

    /** 订阅 OnErrorReturn 拦截 onError 并转为 onSuccess。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {

        source.subscribe(new OnErrorReturn(observer));
    }

    /** onError 时计算替代值；成功/onSubscribe 直接透传。 */
    final class OnErrorReturn implements SingleObserver<T> {

        private final SingleObserver<? super T> observer;

        OnErrorReturn(SingleObserver<? super T> observer) {
            this.observer = observer;
        }

        /** valueSupplier 或固定 value；null 则 NPE.initCause(e) 转发。 */
        @Override
        public void onError(Throwable e) {
            T v;

            if (valueSupplier != null) {
                try {
                    v = valueSupplier.apply(e);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    observer.onError(new CompositeException(e, ex));
                    return;
                }
            } else {
                v = value;
            }

            if (v == null) {
                NullPointerException npe = new NullPointerException("Value supplied was null");
                npe.initCause(e);
                observer.onError(npe);
                return;
            }

            observer.onSuccess(v);
        }

        @Override
        public void onSubscribe(Disposable d) {
            observer.onSubscribe(d);
        }

        @Override
        public void onSuccess(T value) {
            observer.onSuccess(value);
        }

    }
}
