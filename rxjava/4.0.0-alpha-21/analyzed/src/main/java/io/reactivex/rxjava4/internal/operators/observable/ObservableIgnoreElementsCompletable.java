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
import io.reactivex.rxjava4.internal.fuseable.FuseToObservable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * {@link ObservableIgnoreElements} 的 Completable 变体：
 * 忽略所有 onNext，仅关心 onError/onComplete 终止信号。
 *
 * @param <T> 上游元素类型
 */
public final class ObservableIgnoreElementsCompletable<T> extends Completable implements FuseToObservable<T> {

    final ObservableSource<T> source;

    /** @param source 上游 ObservableSource */
    public ObservableIgnoreElementsCompletable(ObservableSource<T> source) {
        this.source = source;
    }

    /** 订阅 IgnoreObservable 映射为 Completable 信号。 */
    @Override
    public void subscribeActual(final CompletableObserver t) {
        source.subscribe(new IgnoreObservable<>(t));
    }

    /** 融合为 {@link ObservableIgnoreElements} 实例。 */
    @Override
    public Observable<T> fuseToObservable() {
        return RxJavaPlugins.onAssembly(new ObservableIgnoreElements<>(source));
    }

    /** 丢弃 onNext，向 CompletableObserver 转发终止信号。 */
    static final class IgnoreObservable<T> implements Observer<T>, Disposable {
        final CompletableObserver downstream;

        Disposable upstream;

        IgnoreObservable(CompletableObserver t) {
            this.downstream = t;
        }

        @Override
        public void onSubscribe(Disposable d) {
            this.upstream = d;
            downstream.onSubscribe(this);
        }

        @Override
        public void onNext(T v) {
            // 有意忽略 onNext
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
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
