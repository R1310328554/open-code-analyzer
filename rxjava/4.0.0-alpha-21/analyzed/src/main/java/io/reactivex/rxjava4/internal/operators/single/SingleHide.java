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
 * 隐藏上游 Disposable：downstream 的 onSubscribe 收到 HideSingleObserver 自身。
 * dispose 时转发至 upstream，防止下游直接操作上游订阅。
 * @param <T> 元素类型
 */
public final class SingleHide<T> extends Single<T> {

    final SingleSource<? extends T> source;

    /** @param source 上游 SingleSource */
    public SingleHide(SingleSource<? extends T> source) {
        this.source = source;
    }

    /** 用 HideSingleObserver 包装 downstream 后订阅 source。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new HideSingleObserver<T>(observer));
    }

    /** 拦截 onSubscribe 将自身作为 Disposable 暴露给 downstream。 */
    static final class HideSingleObserver<T> implements SingleObserver<T>, Disposable {

        final SingleObserver<? super T> downstream;

        Disposable upstream;

        HideSingleObserver(SingleObserver<? super T> downstream) {
            this.downstream = downstream;
        }

        /** 转发 dispose 至 upstream。 */
        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        /** validate 成功后保存 upstream 并向 downstream 传递 this。 */
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
    }

}
