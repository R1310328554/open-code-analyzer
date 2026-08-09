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

import java.util.NoSuchElementException;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 消费上游 Observable 并转为 Single：恰一个元素时 onSuccess；
 * 零个时用 defaultValue 或 {@link NoSuchElementException}；多于一个时 onError。
 *
 * @param <T> 元素类型
 */
public final class ObservableSingleSingle<T> extends Single<T> {

    final ObservableSource<? extends T> source;

    final T defaultValue;

    /**
     * @param source 上游 ObservableSource
     * @param defaultValue 上游为空时的默认值（null 表示无默认）
     */
    public ObservableSingleSingle(ObservableSource<? extends T> source, T defaultValue) {
        this.source = source;
        this.defaultValue = defaultValue;
    }

    @Override
    public void subscribeActual(SingleObserver<? super T> t) {
        source.subscribe(new SingleElementObserver<>(t, defaultValue));
    }

    /** 与 Maybe 版类似，onComplete 时应用 defaultValue 或 NoSuchElementException。 */
    static final class SingleElementObserver<T> implements Observer<T>, Disposable {
        final SingleObserver<? super T> downstream;

        final T defaultValue;

        Disposable upstream;

        T value;

        boolean done;

        SingleElementObserver(SingleObserver<? super T> actual, T defaultValue) {
            this.downstream = actual;
            this.defaultValue = defaultValue;
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
            if (done) {
                return;
            }
            if (value != null) {
                done = true;
                upstream.dispose();
                downstream.onError(new IllegalArgumentException("Sequence contains more than one element!"));
                return;
            }
            value = t;
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            downstream.onError(t);
        }

        /** 有唯一元素或 defaultValue 时 onSuccess，否则 onError(NoSuchElementException)。 */
        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            T v = value;
            value = null;
            if (v == null) {
                v = defaultValue;
            }

            if (v != null) {
                downstream.onSuccess(v);
            } else {
                downstream.onError(new NoSuchElementException());
            }
        }
    }
}
