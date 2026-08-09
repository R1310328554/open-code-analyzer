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

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 将上游 Single 的 onSuccess 值经 mapper 映射为 MaybeSource，
 * 订阅 inner Maybe 并转发其 onSuccess/onComplete/onError。
 *
 * @param <T> 上游成功值类型
 * @param <R> 下游 Maybe 元素类型
 */
public final class SingleFlatMapMaybe<T, R> extends Maybe<R> {

    final SingleSource<? extends T> source;

    final Function<? super T, ? extends MaybeSource<? extends R>> mapper;

    /**
     * @param source 上游 SingleSource
     * @param mapper 将成功值映射为 MaybeSource 的函数
     */
    public SingleFlatMapMaybe(SingleSource<? extends T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper) {
        this.mapper = mapper;
        this.source = source;
    }

    /** 订阅 FlatMapSingleObserver，onSuccess 时 flatMap inner MaybeSource。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super R> downstream) {
        source.subscribe(new FlatMapSingleObserver<T, R>(downstream, mapper));
    }

    /** 上游 SingleObserver：onSuccess 时 mapper 并订阅 inner MaybeSource。 */
    static final class FlatMapSingleObserver<T, R>
    extends AtomicReference<Disposable>
    implements SingleObserver<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -5843758257109742742L;

        final MaybeObserver<? super R> downstream;

        final Function<? super T, ? extends MaybeSource<? extends R>> mapper;

        FlatMapSingleObserver(MaybeObserver<? super R> actual, Function<? super T, ? extends MaybeSource<? extends R>> mapper) {
            this.downstream = actual;
            this.mapper = mapper;
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d)) {
                downstream.onSubscribe(this);
            }
        }

        /** mapper 获取 MaybeSource 并订阅 FlatMapMaybeObserver。 */
        @Override
        public void onSuccess(T value) {
            MaybeSource<? extends R> ms;

            try {
                ms = Objects.requireNonNull(mapper.apply(value), "The mapper returned a null MaybeSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                onError(ex);
                return;
            }

            if (!isDisposed()) {
                ms.subscribe(new FlatMapMaybeObserver<R>(this, downstream));
            }
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }
    }

    /** inner MaybeSource 的 Observer：转发 onSuccess/onComplete/onError。 */
    record FlatMapMaybeObserver<R>(AtomicReference<Disposable> parent,
                                   MaybeObserver<? super R> downstream) implements MaybeObserver<R> {

        @Override
            public void onSubscribe(final Disposable d) {
                DisposableHelper.replace(parent, d);
            }

            @Override
            public void onSuccess(final R value) {
                downstream.onSuccess(value);
            }

            @Override
            public void onError(final Throwable e) {
                downstream.onError(e);
            }

            @Override
            public void onComplete() {
                downstream.onComplete();
            }
        }
}
