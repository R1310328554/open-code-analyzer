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

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.Function;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.observers.ResumeSingleObserver;

/**
 * 上游 onError 时由 nextFunction 返回备用 SingleSource 并订阅恢复。
 * nextFunction 异常则 CompositeException 合并转发。
 * @param <T> 元素类型
 */
public final class SingleResumeNext<T> extends Single<T> {
    final SingleSource<? extends T> source;

    final Function<? super Throwable, ? extends SingleSource<? extends T>> nextFunction;

    /**
     * @param source 主 SingleSource
     * @param nextFunction 错误时选择备用 SingleSource 的函数
     */
    public SingleResumeNext(SingleSource<? extends T> source,
            Function<? super Throwable, ? extends SingleSource<? extends T>> nextFunction) {
        this.source = source;
        this.nextFunction = nextFunction;
    }

    /** 订阅 ResumeMainSingleObserver 在 onError 时切换备用源。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super T> observer) {
        source.subscribe(new ResumeMainSingleObserver<>(observer, nextFunction));
    }

    /** onError 时 apply nextFunction 并用 ResumeSingleObserver 订阅备用源。 */
    static final class ResumeMainSingleObserver<T> extends AtomicReference<Disposable>
    implements SingleObserver<T>, Disposable {
        @Serial
        private static final long serialVersionUID = -5314538511045349925L;

        final SingleObserver<? super T> downstream;

        final Function<? super Throwable, ? extends SingleSource<? extends T>> nextFunction;

        ResumeMainSingleObserver(SingleObserver<? super T> actual,
                Function<? super Throwable, ? extends SingleSource<? extends T>> nextFunction) {
            this.downstream = actual;
            this.nextFunction = nextFunction;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d)) {
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onSuccess(T value) {
            downstream.onSuccess(value);
        }

        /** nextFunction 得 source 后 ResumeSingleObserver 桥接订阅。 */
        @Override
        public void onError(Throwable e) {
            SingleSource<? extends T> source;

            try {
                source = Objects.requireNonNull(nextFunction.apply(e), "The nextFunction returned a null SingleSource.");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                downstream.onError(new CompositeException(e, ex));
                return;
            }

            source.subscribe(new ResumeSingleObserver<T>(this, downstream));
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }
    }
}
