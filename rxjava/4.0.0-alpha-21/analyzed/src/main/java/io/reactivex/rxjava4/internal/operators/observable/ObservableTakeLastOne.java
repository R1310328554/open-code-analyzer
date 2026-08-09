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
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 缓存上游最后一个 onNext 值，onComplete 时发射该值（若有）后完成。
 * @param <T> 元素类型
 */
public final class ObservableTakeLastOne<T> extends AbstractObservableWithUpstream<T, T> {

    /** @param source 上游 ObservableSource */
    public ObservableTakeLastOne(ObservableSource<T> source) {
        super(source);
    }

    /** 订阅 TakeLastOneObserver。 */
    @Override
    public void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new TakeLastOneObserver<>(observer));
    }

    /** 每次 onNext 覆盖 value；onComplete 时 emit 后完成。 */
    static final class TakeLastOneObserver<T> implements Observer<T>, Disposable {
        final Observer<? super T> downstream;

        Disposable upstream;

        T value;

        TakeLastOneObserver(Observer<? super T> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        /** 覆盖缓存的最后一个值。 */
        @Override
        public void onNext(T t) {
            value = t;
        }

        @Override
        public void onError(Throwable t) {
            value = null;
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
                emit();
        }

        /** 非 null 时 onNext 缓存值，然后 onComplete。 */
        void emit() {
            T v = value;
            if (v != null) {
                value = null;
                downstream.onNext(v);
            }
            downstream.onComplete();
        }

        @Override
        public void dispose() {
            value = null;
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }
    }
}
