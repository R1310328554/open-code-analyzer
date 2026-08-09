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
 * 将上游 Maybe 的 onSuccess 值映射为 {@link CompletableSource} 并订阅。
 * @param <T> 上游元素类型
 */
public final class MaybeFlatMapCompletable<T> extends Completable {

    final MaybeSource<T> source;

    final Function<? super T, ? extends CompletableSource> mapper;

    /**
     * @param source 上游 MaybeSource
     * @param mapper 由 T 映射 CompletableSource
     */
    public MaybeFlatMapCompletable(MaybeSource<T> source, Function<? super T, ? extends CompletableSource> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    /** FlatMapCompletableObserver 同时实现 Maybe 与 Completable 观察者。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        FlatMapCompletableObserver<T> parent = new FlatMapCompletableObserver<>(observer, mapper);
        observer.onSubscribe(parent);
        source.subscribe(parent);
    }

    /** onSuccess 时 mapper 得 Completable 并 subscribe(this)。 */
    static final class FlatMapCompletableObserver<T>
    extends AtomicReference<Disposable>
    implements MaybeObserver<T>, CompletableObserver, Disposable {

        @Serial
        private static final long serialVersionUID = -2177128922851101253L;

        final CompletableObserver downstream;

        final Function<? super T, ? extends CompletableSource> mapper;

        FlatMapCompletableObserver(CompletableObserver actual,
                Function<? super T, ? extends CompletableSource> mapper) {
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
            DisposableHelper.replace(this, d);
        }

        @Override
        public void onSuccess(T value) {
            CompletableSource cs;

            try {
                cs = Objects.requireNonNull(mapper.apply(value), "The mapper returned a null CompletableSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                onError(ex);
                return;
            }

            if (!isDisposed()) {
                cs.subscribe(this);
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
}
