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

/**
 * 终止时断开 upstream 与 downstream 的引用，便于 GC 回收观察者链。
 * onSuccess/onError 后将 downstream 置 null，upstream 置 DISPOSED。
 * <p>History: 2.1.5 - experimental
 * @param <T> 元素类型
 * @since 2.2
 */
public final class SingleDetach<T> extends Single<T> {

    final SingleSource<T> source;

    /** @param source 上游 SingleSource */
    public SingleDetach(SingleSource<T> source) {
        this.source = source;
    }

    /** 订阅 DetachSingleObserver 在终止时清空引用。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new DetachSingleObserver<>(observer));
    }

    /** dispose 时清空 downstream；终止后 upstream 置 DISPOSED。 */
    static final class DetachSingleObserver<T> implements SingleObserver<T>, Disposable {

        SingleObserver<? super T> downstream;

        Disposable upstream;

        DetachSingleObserver(SingleObserver<? super T> downstream) {
            this.downstream = downstream;
        }

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

        /** 清空引用后转发 onSuccess，避免持有 downstream。 */
        @Override
        public void onSuccess(T value) {
            upstream = DisposableHelper.DISPOSED;
            SingleObserver<? super T> a = downstream;
            if (a != null) {
                downstream = null;
                a.onSuccess(value);
            }
        }

        /** 清空引用后转发 onError。 */
        @Override
        public void onError(Throwable e) {
            upstream = DisposableHelper.DISPOSED;
            SingleObserver<? super T> a = downstream;
            if (a != null) {
                downstream = null;
                a.onError(e);
            }
        }
    }
}
