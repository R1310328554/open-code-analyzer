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
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.fuseable.HasUpstreamMaybeSource;
import io.reactivex.rxjava4.internal.observers.DeferredScalarDisposable;

import java.io.Serial;

/**
 * 将 {@link MaybeSource} 包装为 {@link Observable}，
 * 转发 Maybe 信号并贯通 dispose。
 *
 * @param <T> 元素类型
 */
public final class MaybeToObservable<T> extends Observable<T> implements HasUpstreamMaybeSource<T> {

    final MaybeSource<T> source;

    /** @param source 上游 MaybeSource */
    public MaybeToObservable(MaybeSource<T> source) {
        this.source = source;
    }

    @Override
    public MaybeSource<T> source() {
        return source;
    }

    /** 用 create 包装 Observer 后订阅上游 Maybe。 */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(create(observer));
    }

    /**
     * 将 {@link Observer} 包装为 {@link MaybeObserver}。
     * <p>History: 2.1.11 - experimental
     * @param <T> 元素类型
     * @param downstream 下游 Observer
     * @return 新的 MaybeObserver 实例
     * @since 2.2
     */
    public static <T> MaybeObserver<T> create(Observer<? super T> downstream) {
        return new MaybeToObservableObserver<>(downstream);
    }

    /** onSuccess 经 DeferredScalarDisposable 发射；dispose 时 dispose 上游。 */
    static final class MaybeToObservableObserver<T> extends DeferredScalarDisposable<T>
    implements MaybeObserver<T> {

        @Serial
        private static final long serialVersionUID = 7603343402964826922L;

        Disposable upstream;

        MaybeToObservableObserver(Observer<? super T> downstream) {
            super(downstream);
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
            complete(value);
        }

        @Override
        public void onError(Throwable e) {
            error(e);
        }

        @Override
        public void onComplete() {
            complete();
        }

        @Override
        public void dispose() {
            super.dispose();
            upstream.dispose();
        }
    }
}
