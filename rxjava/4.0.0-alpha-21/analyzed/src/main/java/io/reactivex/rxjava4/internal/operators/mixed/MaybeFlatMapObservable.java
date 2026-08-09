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

package io.reactivex.rxjava4.internal.operators.mixed;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * Maybe onSuccess 时将值映射为 {@link ObservableSource} 并订阅，
 * 将其信号 relay 到下游 {@link Observer}。
 * @param <T> Maybe 成功值类型
 * @param <R> ObservableSource 及本算子结果类型
 * @since 2.1.15
 */
public final class MaybeFlatMapObservable<T, R> extends Observable<R> {

    final MaybeSource<T> source;

    final Function<? super T, ? extends ObservableSource<? extends R>> mapper;

    /**
     * @param source 上游 MaybeSource
     * @param mapper 由成功值映射 ObservableSource 的函数
     */
    public MaybeFlatMapObservable(MaybeSource<T> source,
            Function<? super T, ? extends ObservableSource<? extends R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    /** 先 onSubscribe FlatMapObserver，再订阅 Maybe。 */
    @Override
    protected void subscribeActual(Observer<? super R> observer) {
        FlatMapObserver<T, R> parent = new FlatMapObserver<>(observer, mapper);
        observer.onSubscribe(parent);
        source.subscribe(parent);
    }

    /** onSuccess 时 flatMap 订阅 inner Observable 并 relay 信号。 */
    static final class FlatMapObserver<T, R>
    extends AtomicReference<Disposable>
    implements Observer<R>, MaybeObserver<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -8948264376121066672L;

        final Observer<? super R> downstream;

        final Function<? super T, ? extends ObservableSource<? extends R>> mapper;

        FlatMapObserver(Observer<? super R> downstream, Function<? super T, ? extends ObservableSource<? extends R>> mapper) {
            this.downstream = downstream;
            this.mapper = mapper;
        }

        @Override
        public void onNext(R t) {
            downstream.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
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
            DisposableHelper.replace(this, d);
        }

        /** apply mapper 得 ObservableSource 并 subscribe(this)。 */
        @Override
        public void onSuccess(T t) {
            ObservableSource<? extends R> o;

            try {
                o = Objects.requireNonNull(mapper.apply(t), "The mapper returned a null Publisher");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }

            if (!isDisposed()) {
                o.subscribe(this);
            }
        }

    }
}
