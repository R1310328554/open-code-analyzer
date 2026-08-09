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

import java.util.Iterator;
import java.util.Objects;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 将上游每个元素经 mapper 转为 {@link Iterable}，
 * 逐元素展开为下游 onNext 序列。
 *
 * @param <T> 上游元素类型
 * @param <R> Iterable 元素及输出类型
 */
public final class ObservableFlattenIterable<T, R> extends AbstractObservableWithUpstream<T, R> {

    final Function<? super T, ? extends Iterable<? extends R>> mapper;

    /**
     * @param source 上游 ObservableSource
     * @param mapper 将元素映射为 Iterable 的函数
     */
    public ObservableFlattenIterable(ObservableSource<T> source,
            Function<? super T, ? extends Iterable<? extends R>> mapper) {
        super(source);
        this.mapper = mapper;
    }

    /** 订阅 FlattenIterableObserver 并展开 Iterable。 */
    @Override
    protected void subscribeActual(Observer<? super R> observer) {
        source.subscribe(new FlattenIterableObserver<>(observer, mapper));
    }

    /** 对每个上游值迭代 Iterable 并逐元素 onNext。 */
    static final class FlattenIterableObserver<T, R> implements Observer<T>, Disposable {
        final Observer<? super R> downstream;

        final Function<? super T, ? extends Iterable<? extends R>> mapper;

        Disposable upstream;

        FlattenIterableObserver(Observer<? super R> actual, Function<? super T, ? extends Iterable<? extends R>> mapper) {
            this.downstream = actual;
            this.mapper = mapper;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        /** 展开 mapper(value) 的 iterator；null 元素或迭代异常转 onError。 */
        @Override
        public void onNext(T value) {
            if (upstream == DisposableHelper.DISPOSED) {
                return;
            }

            Iterator<? extends R> it;

            try {
                it = mapper.apply(value).iterator();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                upstream.dispose();
                onError(ex);
                return;
            }

            Observer<? super R> a = downstream;

            for (;;) {
                boolean b;

                try {
                    b = it.hasNext();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    upstream.dispose();
                    onError(ex);
                    return;
                }

                if (b) {
                    R v;

                    try {
                        v = Objects.requireNonNull(it.next(), "The iterator returned a null value");
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        upstream.dispose();
                        onError(ex);
                        return;
                    }

                    a.onNext(v);
                } else {
                    break;
                }
            }
        }

        @Override
        public void onError(Throwable e) {
            if (upstream == DisposableHelper.DISPOSED) {
                RxJavaPlugins.onError(e);
                return;
            }
            upstream = DisposableHelper.DISPOSED;
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            if (upstream == DisposableHelper.DISPOSED) {
                return;
            }
            upstream = DisposableHelper.DISPOSED;
            downstream.onComplete();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        @Override
        public void dispose() {
            upstream.dispose();
            upstream = DisposableHelper.DISPOSED;
        }
    }
}
