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
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 等待 {@link CompletableSource} 完成后再订阅主 {@link MaybeSource}。
 *
 * @param <T> 元素类型
 */
public final class MaybeDelayWithCompletable<T> extends Maybe<T> {

    final MaybeSource<T> source;

    final CompletableSource other;

    /**
     * @param source 主 Maybe
     * @param other 需先完成的 Completable
     */
    public MaybeDelayWithCompletable(MaybeSource<T> source, CompletableSource other) {
        this.source = source;
        this.other = other;
    }

    /** 先订阅 other Completable，onComplete 后订阅主源。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        other.subscribe(new OtherObserver<>(observer, source));
    }

    /** Completable 完成后订阅主 Maybe。 */
    static final class OtherObserver<T>
    extends AtomicReference<Disposable>
    implements CompletableObserver, Disposable {
        @Serial
        private static final long serialVersionUID = 703409937383992161L;

        final MaybeObserver<? super T> downstream;

        final MaybeSource<T> source;

        OtherObserver(MaybeObserver<? super T> actual, MaybeSource<T> source) {
            this.downstream = actual;
            this.source = source;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d)) {

                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        /** other 完成，订阅主 MaybeSource。 */
        @Override
        public void onComplete() {
            source.subscribe(new DelayWithMainObserver<>(this, downstream));
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }
    }

    /** 透传主 Maybe 信号并更新 parent 的 Disposable。 */
    record DelayWithMainObserver<T>(AtomicReference<Disposable> parent,
                                    MaybeObserver<? super T> downstream) implements MaybeObserver<T> {

        @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.replace(parent, d);
            }

            @Override
            public void onSuccess(T value) {
                downstream.onSuccess(value);
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
