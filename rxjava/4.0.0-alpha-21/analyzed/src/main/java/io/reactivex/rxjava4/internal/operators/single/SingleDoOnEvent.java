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
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.BiConsumer;

/**
 * 在转发终端事件前调用 onEvent：成功时 (value, null)，错误时 (null, error)。
 * 回调异常：成功路径转 onError；错误路径合并 CompositeException。
 * @param <T> 元素类型
 */
public final class SingleDoOnEvent<T> extends Single<T> {
    final SingleSource<T> source;

    final BiConsumer<? super T, ? super Throwable> onEvent;

    /**
     * @param source 上游 SingleSource
     * @param onEvent 终端事件回调 (value, error)
     */
    public SingleDoOnEvent(SingleSource<T> source, BiConsumer<? super T, ? super Throwable> onEvent) {
        this.source = source;
        this.onEvent = onEvent;
    }

    /** 订阅 DoOnEvent 在 onSuccess/onError 前触发 BiConsumer。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {

        source.subscribe(new DoOnEvent(observer));
    }

    /** onSuccess/onError 均先 onEvent.accept 再转发。 */
    final class DoOnEvent implements SingleObserver<T> {
        private final SingleObserver<? super T> downstream;

        DoOnEvent(SingleObserver<? super T> observer) {
            this.downstream = observer;
        }

        @Override
        public void onSubscribe(Disposable d) {
            downstream.onSubscribe(d);
        }

        /** accept(value, null) 成功后 downstream.onSuccess。 */
        @Override
        public void onSuccess(T value) {
            try {
                onEvent.accept(value, null);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }

            downstream.onSuccess(value);
        }

        /** accept(null, e) 后转发；回调异常合并 CompositeException。 */
        @Override
        public void onError(Throwable e) {
            try {
                onEvent.accept(null, e);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                e = new CompositeException(e, ex);
            }
            downstream.onError(e);
        }
    }
}
