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

package io.reactivex.rxjava4.internal.operators.single;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.observers.DeferredScalarDisposable;

import java.io.Serial;

/**
 * 将 Single 包装为 Observable：成功时 onNext 后 onComplete，
 * 错误时 onError（DeferredScalarDisposable 语义）。
 *
 * @param <T> 元素类型
 */
public final class SingleToObservable<T> extends Observable<T> {

    final SingleSource<? extends T> source;

    /** @param source 上游 SingleSource */
    public SingleToObservable(SingleSource<? extends T> source) {
        this.source = source;
    }

    /** 订阅 create(observer) 返回的 SingleObserver。 */
    @Override
    public void subscribeActual(final Observer<? super T> observer) {
        source.subscribe(create(observer));
    }

    /**
     * 为 {@link Observer} 创建 {@link SingleObserver} 包装。
     * <p>History: 2.0.1 - experimental
     * @param <T> 元素类型
     * @param downstream 下游 {@code Observer}
     * @return 新的 SingleObserver 实例
     * @since 2.2
     */
    public static <T> SingleObserver<T> create(Observer<? super T> downstream) {
        return new SingleToObservableObserver<>(downstream);
    }

    /** Single→Observable 适配：onSuccess 时 complete，dispose 时释放 upstream。 */
    static final class SingleToObservableObserver<T>
    extends DeferredScalarDisposable<T>
    implements SingleObserver<T> {

        @Serial
        private static final long serialVersionUID = 3786543492451018833L;
        Disposable upstream;

        SingleToObservableObserver(Observer<? super T> downstream) {
            super(downstream);
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        /** 调用 DeferredScalarDisposable.complete 发射单元素。 */
        @Override
        public void onSuccess(T value) {
            complete(value);
        }

        @Override
        public void onError(Throwable e) {
            error(e);
        }

        @Override
        public void dispose() {
            super.dispose();
            upstream.dispose();
        }

    }
}
