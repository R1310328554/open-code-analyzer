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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

import java.util.Objects;

/**
 * 将上游 Single 成功值经 selector 映射为 {@link Notification}，
 * 再按 Notification 类型转为 Maybe 的 onSuccess/onComplete/onError。
 * <p>History: 2.2.4 - experimental
 * @param <T> 上游元素类型
 * @param <R> Notification 与结果元素类型
 * @since 3.0.0
 */
public final class SingleDematerialize<T, R> extends Maybe<R> {

    final Single<T> source;

    final Function<? super T, Notification<R>> selector;

    /**
     * @param source 上游 Single
     * @param selector 将成功值映射为 Notification 的函数
     */
    public SingleDematerialize(Single<T> source, Function<? super T, Notification<R>> selector) {
        this.source = source;
        this.selector = selector;
    }

    /** 订阅 DematerializeObserver 处理 Notification 分支。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super R> observer) {
        source.subscribe(new DematerializeObserver<>(observer, selector));
    }

    /** 按 Notification.isOnNext/isOnComplete/isOnError 转发 Maybe 信号。 */
    static final class DematerializeObserver<T, R> implements SingleObserver<T>, Disposable {

        final MaybeObserver<? super R> downstream;

        final Function<? super T, Notification<R>> selector;

        Disposable upstream;

        DematerializeObserver(MaybeObserver<? super R> downstream,
                Function<? super T, Notification<R>> selector) {
            this.downstream = downstream;
            this.selector = selector;
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
            if (DisposableHelper.validate(upstream, d)) {
                upstream = d;
                downstream.onSubscribe(this);
            }
        }

        /** selector 映射 Notification 后分支转发 onSuccess/onComplete/onError。 */
        @Override
        public void onSuccess(T t) {
            Notification<R> notification;

            try {
                notification = Objects.requireNonNull(selector.apply(t), "The selector returned a null Notification");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }
            if (notification.isOnNext()) {
                downstream.onSuccess(notification.getValue());
            } else if (notification.isOnComplete()) {
                downstream.onComplete();
            } else {
                downstream.onError(notification.getError());
            }
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }
    }
}
