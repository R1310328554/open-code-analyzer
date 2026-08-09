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
 * 跳过前 n 个 onNext，之后原样转发；onError/onComplete 不受影响。
 *
 * @param <T> 元素类型
 */
public final class ObservableSkip<T> extends AbstractObservableWithUpstream<T, T> {
    final long n;
    /**
     * @param source 上游 ObservableSource
     * @param n 要跳过的元素个数
     */
    public ObservableSkip(ObservableSource<T> source, long n) {
        super(source);
        this.n = n;
    }

    @Override
    public void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new SkipObserver<>(observer, n));
    }

    /** remaining 递减至 0 后开始向下游 onNext。 */
    static final class SkipObserver<T> implements Observer<T>, Disposable {
        final Observer<? super T> downstream;
        long remaining;

        Disposable upstream;

        SkipObserver(Observer<? super T> actual, long n) {
            this.downstream = actual;
            this.remaining = n;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        /** remaining > 0 时仅递减；否则转发 t。 */
        @Override
        public void onNext(T t) {
            if (remaining != 0L) {
                remaining--;
            } else {
                downstream.onNext(t);
            }
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
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }
    }
}
