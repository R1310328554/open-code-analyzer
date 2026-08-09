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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.util.Objects;

/**
 * 物理解包：将上游元素经 selector 转为 {@link Notification}，
 * 按 onNext/onError/onComplete 语义展开为下游 R 流或终止信号。
 *
 * @param <T> 上游元素类型
 * @param <R> 解包后的元素类型
 */
public final class ObservableDematerialize<T, R> extends AbstractObservableWithUpstream<T, R> {

    final Function<? super T, ? extends Notification<R>> selector;

    /**
     * @param source 上游 ObservableSource
     * @param selector 将元素映射为 Notification 的函数
     */
    public ObservableDematerialize(ObservableSource<T> source, Function<? super T, ? extends Notification<R>> selector) {
        super(source);
        this.selector = selector;
    }

    /** 订阅 DematerializeObserver 解包 Notification。 */
    @Override
    public void subscribeActual(Observer<? super R> observer) {
        source.subscribe(new DematerializeObserver<>(observer, selector));
    }

    /** 按 Notification 类型转发 onNext 或终止序列。 */
    static final class DematerializeObserver<T, R> implements Observer<T>, Disposable {
        final Observer<? super R> downstream;

        final Function<? super T, ? extends Notification<R>> selector;

        boolean done;

        Disposable upstream;

        DematerializeObserver(Observer<? super R> downstream, Function<? super T, ? extends Notification<R>> selector) {
            this.downstream = downstream;
            this.selector = selector;
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

        /** 解包 Notification：onError/onComplete 终止，否则转发 getValue。 */
        @Override
        public void onNext(T item) {
            if (done) {
                if (item instanceof Notification<?> notification) {
                    if (notification.isOnError()) {
                        RxJavaPlugins.onError(notification.getError());
                    }
                }
                return;
            }

            Notification<R> notification;

            try {
                notification = Objects.requireNonNull(selector.apply(item), "The selector returned a null Notification");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                upstream.dispose();
                onError(ex);
                return;
            }
            if (notification.isOnError()) {
                upstream.dispose();
                onError(notification.getError());
            }
            else if (notification.isOnComplete()) {
                upstream.dispose();
                onComplete();
            } else {
                downstream.onNext(notification.getValue());
            }
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;

            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;

            downstream.onComplete();
        }
    }
}
