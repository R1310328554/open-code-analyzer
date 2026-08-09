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

package io.reactivex.rxjava4.internal.operators.maybe;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 将上游 Maybe 的 onSuccess 值映射为 {@link SingleSource} 并订阅，
 * 以 Maybe 形式向下游发射 Single 结果。
 * <p>History: 2.0.2 - experimental
 * @param <T> 输入元素类型
 * @param <R> 结果类型
 * @since 2.1
 */
public final class MaybeFlatMapSingle<T, R> extends Maybe<R> {

    final MaybeSource<T> source;

    final Function<? super T, ? extends SingleSource<? extends R>> mapper;

    /**
     * @param source 上游 MaybeSource
     * @param mapper 由 T 映射 SingleSource
     */
    public MaybeFlatMapSingle(MaybeSource<T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    /** FlatMapMaybeObserver onSuccess 时 subscribe FlatMapSingleObserver。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super R> downstream) {
        source.subscribe(new FlatMapMaybeObserver<>(downstream, mapper));
    }

    /** 持有 mapper；onSuccess 后切换到 Single 订阅。 */
    static final class FlatMapMaybeObserver<T, R>
    extends AtomicReference<Disposable>
    implements MaybeObserver<T>, Disposable {

        @Serial
        private static final long serialVersionUID = 4827726964688405508L;

        final MaybeObserver<? super R> downstream;

        final Function<? super T, ? extends SingleSource<? extends R>> mapper;

        FlatMapMaybeObserver(MaybeObserver<? super R> actual, Function<? super T, ? extends SingleSource<? extends R>> mapper) {
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

        @Override
        public void onSuccess(T value) {
            SingleSource<? extends R> ss;

            try {
                ss = Objects.requireNonNull(mapper.apply(value), "The mapper returned a null SingleSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                onError(ex);
                return;
            }

            if (!isDisposed()) {
                ss.subscribe(new FlatMapSingleObserver<R>(this, downstream));
            }
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }

    /** Single 结果 relay 到 Maybe downstream；Disposable 写入 parent。 */
    record FlatMapSingleObserver<R>(AtomicReference<Disposable> parent,
                                    MaybeObserver<? super R> downstream) implements SingleObserver<R> {

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
        }
}
