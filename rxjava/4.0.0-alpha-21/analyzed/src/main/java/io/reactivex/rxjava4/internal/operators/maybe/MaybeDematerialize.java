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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

import java.util.Objects;

/**
 * 将上游 onSuccess 值映射为 {@link Notification}，
 * 再还原为对应的 onSuccess/onError/onComplete 信号。
 * <p>History: 2.2.4 - experimental
 * @param <T> 上游元素类型
 * @param <R> Notification 及结果元素类型
 * @since 3.0.0
 */
public final class MaybeDematerialize<T, R> extends AbstractMaybeWithUpstream<T, R> {

    final Function<? super T, Notification<R>> selector;

    /**
     * @param source 上游 Maybe
     * @param selector 将成功值映射为 Notification 的函数
     */
    public MaybeDematerialize(Maybe<T> source, Function<? super T, Notification<R>> selector) {
        super(source);
        this.selector = selector;
    }

    /** 订阅 DematerializeObserver 并还原 Notification 信号。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super R> observer) {
        source.subscribe(new DematerializeObserver<>(observer, selector));
    }

    /** onSuccess 时应用 selector 并按 Notification 类型转发。 */
    static final class DematerializeObserver<T, R> implements MaybeObserver<T>, Disposable {

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

        /** 按 Notification 的 onNext/onComplete/onError 分支转发。 */
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

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}
