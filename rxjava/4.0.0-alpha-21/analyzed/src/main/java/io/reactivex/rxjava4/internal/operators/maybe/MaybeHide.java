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

/**
 * 隐藏上游 {@link Maybe} 及其通过 onSubscribe 传递的 {@link Disposable} 身份，
 * 防止下游直接 dispose 上游。
 *
 * @param <T> 元素类型
 */
public final class MaybeHide<T> extends AbstractMaybeWithUpstream<T, T> {

    /** @param source 上游 Maybe */
    public MaybeHide(MaybeSource<T> source) {
        super(source);
    }

    /** 订阅 HideMaybeObserver 并包装 Disposable。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new HideMaybeObserver<>(observer));
    }

    /** 透传信号，onSubscribe 时向下游传递自身而非上游 Disposable。 */
    static final class HideMaybeObserver<T> implements MaybeObserver<T>, Disposable {

        final MaybeObserver<? super T> downstream;

        Disposable upstream;

        HideMaybeObserver(MaybeObserver<? super T> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void dispose() {
            upstream.dispose();
            upstream = DisposableHelper.DISPOSED;
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
        public void onSuccess(T value) {
            downstream.onSuccess(value);
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
