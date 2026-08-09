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
 * {@link Maybe} 终止时断开上游与下游之间的引用，
 * 避免长期持有 observer 导致内存泄漏。
 *
 * @param <T> 元素类型
 */
public final class MaybeDetach<T> extends AbstractMaybeWithUpstream<T, T> {

    /** @param source 上游 Maybe */
    public MaybeDetach(MaybeSource<T> source) {
        super(source);
    }

    /** 订阅 DetachMaybeObserver 并在终止后清空引用。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new DetachMaybeObserver<>(observer));
    }

    /** 终止事件后将 downstream 置 null 断开引用。 */
    static final class DetachMaybeObserver<T> implements MaybeObserver<T>, Disposable {

        MaybeObserver<? super T> downstream;

        Disposable upstream;

        DetachMaybeObserver(MaybeObserver<? super T> downstream) {
            this.downstream = downstream;
        }

        /** 清空 downstream 并 dispose 上游。 */
        @Override
        public void dispose() {
            downstream = null;
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

        /** 转发后清空 downstream 引用。 */
        @Override
        public void onSuccess(T value) {
            upstream = DisposableHelper.DISPOSED;
            MaybeObserver<? super T> a = downstream;
            if (a != null) {
                downstream = null;
                a.onSuccess(value);
            }
        }

        @Override
        public void onError(Throwable e) {
            upstream = DisposableHelper.DISPOSED;
            MaybeObserver<? super T> a = downstream;
            if (a != null) {
                downstream = null;
                a.onError(e);
            }
        }

        @Override
        public void onComplete() {
            upstream = DisposableHelper.DISPOSED;
            MaybeObserver<? super T> a = downstream;
            if (a != null) {
                downstream = null;
                a.onComplete();
            }
        }
    }
}
