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

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Predicate;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 上游 onError 时若 {@link Predicate} 对异常返回 true，
 * 则向下游 onComplete 而非 onError。
 *
 * @param <T> 元素类型
 */
public final class MaybeOnErrorComplete<T> extends AbstractMaybeWithUpstream<T, T> {

    final Predicate<? super Throwable> predicate;

    /**
     * @param source 上游 MaybeSource
     * @param predicate 判定是否吞掉异常
     */
    public MaybeOnErrorComplete(MaybeSource<T> source,
            Predicate<? super Throwable> predicate) {
        super(source);
        this.predicate = predicate;
    }

    /** 包装为 OnErrorCompleteMultiObserver。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new OnErrorCompleteMultiObserver<>(observer, predicate));
    }

    /** onError 时 test predicate；true 则 onComplete。 */
    public static final class OnErrorCompleteMultiObserver<T>
    implements MaybeObserver<T>, SingleObserver<T>, Disposable {

        final MaybeObserver<? super T> downstream;

        final Predicate<? super Throwable> predicate;

        Disposable upstream;

        public OnErrorCompleteMultiObserver(MaybeObserver<? super T> actual, Predicate<? super Throwable> predicate) {
            this.downstream = actual;
            this.predicate = predicate;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onSuccess(T value) {
            downstream.onSuccess(value);
        }

        /** predicate 异常则 CompositeException；否则按 test 结果转发。 */
        @Override
        public void onError(Throwable e) {
            boolean b;

            try {
                b = predicate.test(e);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(new CompositeException(e, ex));
                return;
            }

            if (b) {
                downstream.onComplete();
            } else {
                downstream.onError(e);
            }
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
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
