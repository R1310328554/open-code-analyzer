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
 * 隐藏被包装 ObservableSource 及其 Disposable 的身份：
 * 下游 onSubscribe 收到 HideDisposable 而非上游 Disposable。
 * @param <T> 元素类型
 * @since 2.0
 */
public final class ObservableHide<T> extends AbstractObservableWithUpstream<T, T> {

    /** @param source 上游 ObservableSource */
    public ObservableHide(ObservableSource<T> source) {
        super(source);
    }

    /** 订阅 HideDisposable 转发信号并屏蔽上游 Disposable 类型。 */
    @Override
    protected void subscribeActual(Observer<? super T> o) {
        source.subscribe(new HideDisposable<>(o));
    }

    /** 透明转发 onNext/onError/onComplete，dispose 委托 upstream。 */
    static final class HideDisposable<T> implements Observer<T>, Disposable {

        final Observer<? super T> downstream;

        Disposable upstream;

        HideDisposable(Observer<? super T> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
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
    }
}
