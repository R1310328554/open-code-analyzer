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
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Action;

/**
 * 在 onSuccess、onError 或 onComplete 向下游转发前先执行 {@link Action}。
 * @param <T> 元素类型
 */
public final class MaybeDoOnTerminate<T> extends Maybe<T> {

    final MaybeSource<T> source;

    final Action onTerminate;

    /**
     * @param source 上游 MaybeSource
     * @param onTerminate 终止前执行的 Action
     */
    public MaybeDoOnTerminate(MaybeSource<T> source, Action onTerminate) {
        this.source = source;
        this.onTerminate = onTerminate;
    }

    /** 包装为 DoOnTerminate 内部观察者。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new DoOnTerminate(observer));
    }

    /** 各终止路径先 run onTerminate，异常则 CompositeException 或 onError。 */
    final class DoOnTerminate implements MaybeObserver<T> {
        final MaybeObserver<? super T> downstream;

        DoOnTerminate(MaybeObserver<? super T> observer) {
            this.downstream = observer;
        }

        @Override
        public void onSubscribe(Disposable d) {
            downstream.onSubscribe(d);
        }

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

        @Override
        public void onComplete() {
            try {
                onTerminate.run();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(ex);
                return;
            }

            downstream.onComplete();
        }
    }
}
