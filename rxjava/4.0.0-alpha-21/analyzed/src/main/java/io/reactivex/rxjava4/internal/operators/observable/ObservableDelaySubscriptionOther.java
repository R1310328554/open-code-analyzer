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
import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 延迟订阅主流：待 other Observable 发出任意 onNext 或 onComplete 后再订阅 main。
 * @param <T> 主流元素类型
 * @param <U> 辅助 Observable 元素类型（仅用于触发，值被忽略）
 */
public final class ObservableDelaySubscriptionOther<T, U> extends Observable<T> {
    final ObservableSource<? extends T> main;
    final ObservableSource<U> other;

    /**
     * @param main 延迟订阅的主流
     * @param other 触发订阅的辅助 Observable
     */
    public ObservableDelaySubscriptionOther(ObservableSource<? extends T> main, ObservableSource<U> other) {
        this.main = main;
        this.other = other;
    }

    /** 先订阅 other，由其 DelayObserver 在 onComplete 时订阅 main。 */
    @Override
    public void subscribeActual(final Observer<? super T> child) {
        final SequentialDisposable serial = new SequentialDisposable();
        child.onSubscribe(serial);

        Observer<U> otherObserver = new DelayObserver(serial, child);

        other.subscribe(otherObserver);
    }

    /** 监听 other：onNext 等同 onComplete，完成后订阅 main。 */
    final class DelayObserver implements Observer<U> {
        final SequentialDisposable serial;
        final Observer<? super T> child;
        boolean done;

        DelayObserver(SequentialDisposable serial, Observer<? super T> child) {
            this.serial = serial;
            this.child = child;
        }

        @Override
        public void onSubscribe(Disposable d) {
            serial.update(d);
        }

        @Override
        public void onNext(U t) {
            onComplete();
        }

        @Override
        public void onError(Throwable e) {
            if (done) {
                RxJavaPlugins.onError(e);
                return;
            }
            done = true;
            child.onError(e);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;

            main.subscribe(new OnComplete());
        }

        /** 订阅 main 并将信号转发至 child。 */
        final class OnComplete implements Observer<T> {
            @Override
            public void onSubscribe(Disposable d) {
                serial.update(d);
            }

            @Override
            public void onNext(T value) {
                child.onNext(value);
            }

            @Override
            public void onError(Throwable e) {
                child.onError(e);
            }

            @Override
            public void onComplete() {
                child.onComplete();
            }
        }
    }
}
