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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.BiFunction;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.util.Objects;

/**
 * 用 BiFunction 将 Observable 序列归约为单个值：
 * 有元素则 onSuccess，空序列则 onComplete。
 *
 * @param <T> 源与结果类型
 */
public final class ObservableReduceMaybe<T> extends Maybe<T> {

    final ObservableSource<T> source;

    final BiFunction<T, T, T> reducer;

    /**
     * @param source 上游 ObservableSource
     * @param reducer 两元素累加器 (acc, value) -> acc
     */
    public ObservableReduceMaybe(ObservableSource<T> source, BiFunction<T, T, T> reducer) {
        this.source = source;
        this.reducer = reducer;
    }

    /** 订阅 ReduceObserver 逐次 apply reducer。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new ReduceObserver<>(observer, reducer));
    }

    /** 首元素缓存为 acc，后续 apply reducer；终止时 onSuccess 或 onComplete。 */
    static final class ReduceObserver<T> implements Observer<T>, Disposable {

        final MaybeObserver<? super T> downstream;

        final BiFunction<T, T, T> reducer;

        boolean done;

        T value;

        Disposable upstream;

        ReduceObserver(MaybeObserver<? super T> observer, BiFunction<T, T, T> reducer) {
            this.downstream = observer;
            this.reducer = reducer;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        /** 首值直接缓存，其后 apply reducer；null 结果转 onError。 */
        @Override
        public void onNext(T value) {
            if (!done) {
                T v = this.value;

                if (v == null) {
                    this.value = value;
                } else {
                    try {
                        this.value = Objects.requireNonNull(reducer.apply(v, value), "The reducer returned a null value");
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        upstream.dispose();
                        onError(ex);
                    }
                }
            }
        }

        @Override
        public void onError(Throwable e) {
            if (done) {
                RxJavaPlugins.onError(e);
                return;
            }
            done = true;
            value = null;
            downstream.onError(e);
        }

        /** 有 acc 则 onSuccess，否则 onComplete。 */
        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            T v = value;
            value = null;
            if (v != null) {
                downstream.onSuccess(v);
            } else {
                downstream.onComplete();
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
    }
}
