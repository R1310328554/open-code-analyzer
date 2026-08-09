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
 * 将上游 onNext/onError/onComplete 包装为 {@link Notification} 并以 onNext 发射；
 * 终端事件（onError/onComplete）之后仍 onComplete 下游。
 *
 * @param <T> 上游元素类型
 */
public final class ObservableMaterialize<T> extends AbstractObservableWithUpstream<T, Notification<T>> {

    /** @param source 上游 ObservableSource */
    public ObservableMaterialize(ObservableSource<T> source) {
        super(source);
    }

    @Override
    public void subscribeActual(Observer<? super Notification<T>> t) {
        source.subscribe(new MaterializeObserver<>(t));
    }

    /** 将每个上游信号转为对应 Notification 并转发。 */
    static final class MaterializeObserver<T> implements Observer<T>, Disposable {
        final Observer<? super Notification<T>> downstream;

        Disposable upstream;

        MaterializeObserver(Observer<? super Notification<T>> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        /** 发射 {@link Notification#createOnNext}。 */
        @Override
        public void onNext(T t) {
            downstream.onNext(Notification.createOnNext(t));
        }

        /** 发射 {@link Notification#createOnError} 后 onComplete。 */
        @Override
        public void onError(Throwable t) {
            Notification<T> v = Notification.createOnError(t);
            downstream.onNext(v);
            downstream.onComplete();
        }

        /** 发射 {@link Notification#createOnComplete} 后 onComplete。 */
        @Override
        public void onComplete() {
            Notification<T> v = Notification.createOnComplete();

            downstream.onNext(v);
            downstream.onComplete();
        }
    }
}
