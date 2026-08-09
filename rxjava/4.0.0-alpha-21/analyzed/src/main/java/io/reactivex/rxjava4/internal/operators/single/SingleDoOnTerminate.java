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
import io.reactivex.rxjava4.functions.Action;

/**
 * 在上游 Single 终止（onSuccess 或 onError）时执行 onTerminate 回调，
 * 再向下游转发原信号（onError 路径下回调异常合并为 CompositeException）。
 *
 * @param <T> 元素类型
 */
public final class SingleDoOnTerminate<T> extends Single<T> {

    final SingleSource<T> source;

    final Action onTerminate;

    /**
     * @param source 上游 SingleSource
     * @param onTerminate 终止时执行的 Action
     */
    public SingleDoOnTerminate(SingleSource<T> source, Action onTerminate) {
        this.source = source;
        this.onTerminate = onTerminate;
    }

    /** 订阅 DoOnTerminate 包装 Observer 在终止时执行回调。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {
        source.subscribe(new DoOnTerminate(observer));
    }

    /** 成功/错误路径均先 onTerminate.run 再转发。 */
    final class DoOnTerminate implements SingleObserver<T> {

        final SingleObserver<? super T> downstream;

        DoOnTerminate(SingleObserver<? super T> observer) {
            this.downstream = observer;
        }

        @Override
        public void onSubscribe(Disposable d) {
            downstream.onSubscribe(d);
        }

        /** onTerminate 成功后 downstream.onSuccess(value)。 */
        @Override
        public void onSuccess(T value) {
            try {
                onTerminate.run();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }

            downstream.onSuccess(value);
        }

        /** onTerminate 后 onError；回调异常与 e 合并为 CompositeException。 */
        @Override
        public void onError(Throwable e) {
            try {
                onTerminate.run();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                e = new CompositeException(e, ex);
            }

            downstream.onError(e);
        }
    }
}
