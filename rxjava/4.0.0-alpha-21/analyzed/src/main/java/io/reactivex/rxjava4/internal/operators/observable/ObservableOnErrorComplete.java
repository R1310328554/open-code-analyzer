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
import io.reactivex.rxjava4.functions.Predicate;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 上游 onError 时若 {@link Predicate} 对 Throwable 返回 true，则转为 onComplete；
 * 否则原样转发 onError。predicate 异常则 {@link CompositeException}。
 *
 * @param <T> 上游元素类型
 * @since 3.0.0
 */
public final class ObservableOnErrorComplete<T> extends AbstractObservableWithUpstream<T, T> {

    final Predicate<? super Throwable> predicate;

    /**
     * @param source 上游 ObservableSource
     * @param predicate 判定是否吞掉错误的谓词
     */
    public ObservableOnErrorComplete(ObservableSource<T> source,
            Predicate<? super Throwable> predicate) {
        super(source);
        this.predicate = predicate;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new OnErrorCompleteObserver<>(observer, predicate));
    }

    /** onNext/onComplete 直通；onError 经 predicate 决定 onComplete 或 onError。 */
    public static final class OnErrorCompleteObserver<T>
    implements Observer<T>, Disposable {

        final Observer<? super T> downstream;

        final Predicate<? super Throwable> predicate;

        Disposable upstream;

        public OnErrorCompleteObserver(Observer<? super T> actual, Predicate<? super Throwable> predicate) {
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
        public void onNext(T value) {
            downstream.onNext(value);
        }

        /** predicate.test 为 true 时 onComplete，否则转发原错误。 */
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
