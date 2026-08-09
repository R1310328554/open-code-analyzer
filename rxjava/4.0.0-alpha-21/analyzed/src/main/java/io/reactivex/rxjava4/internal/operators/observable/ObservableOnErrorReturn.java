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
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 上游 onError 时由 {@link Function} 提供替代值，以 onNext 发射后 onComplete；
 * valueSupplier 异常则 {@link CompositeException}，返回 null 则 NPE。
 *
 * @param <T> 元素类型
 */
public final class ObservableOnErrorReturn<T> extends AbstractObservableWithUpstream<T, T> {
    final Function<? super Throwable, ? extends T> valueSupplier;
    /**
     * @param source 上游 ObservableSource
     * @param valueSupplier 由 Throwable 映射替代值的函数
     */
    public ObservableOnErrorReturn(ObservableSource<T> source, Function<? super Throwable, ? extends T> valueSupplier) {
        super(source);
        this.valueSupplier = valueSupplier;
    }

    @Override
    public void subscribeActual(Observer<? super T> t) {
        source.subscribe(new OnErrorReturnObserver<>(t, valueSupplier));
    }

    /** onNext/onComplete 直通；onError 经 valueSupplier 转为 onNext+onComplete。 */
    static final class OnErrorReturnObserver<T> implements Observer<T>, Disposable {
        final Observer<? super T> downstream;
        final Function<? super Throwable, ? extends T> valueSupplier;

        Disposable upstream;

        OnErrorReturnObserver(Observer<? super T> actual, Function<? super Throwable, ? extends T> valueSupplier) {
            this.downstream = actual;
            this.valueSupplier = valueSupplier;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        @Override
        public void onNext(T t) {
            downstream.onNext(t);
        }

        /** valueSupplier 求值后 onNext 替代值并 onComplete。 */
        @Override
        public void onError(Throwable t) {
            T v;
            try {
                v = valueSupplier.apply(t);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                downstream.onError(new CompositeException(t, e));
                return;
            }

            if (v == null) {
                NullPointerException e = new NullPointerException("The supplied value is null");
                e.initCause(t);
                downstream.onError(e);
                return;
            }

            downstream.onNext(v);
            downstream.onComplete();
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}
