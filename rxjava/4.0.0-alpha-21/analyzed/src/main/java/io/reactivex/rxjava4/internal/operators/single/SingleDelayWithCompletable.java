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
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.observers.ResumeSingleObserver;

/**
 * 先订阅 other {@link CompletableSource}，其 onComplete 后再订阅 source Single。
 * other 出错则直接 onError，不订阅 source。
 * @param <T> 目标 Single 元素类型
 */
public final class SingleDelayWithCompletable<T> extends Single<T> {

    final SingleSource<T> source;

    final CompletableSource other;

    /**
     * @param source 待延迟订阅的 SingleSource
     * @param other 必须先完成的 CompletableSource
     */
    public SingleDelayWithCompletable(SingleSource<T> source, CompletableSource other) {
        this.source = source;
        this.other = other;
    }

    /** 订阅 other，完成后再 ResumeSingleObserver 订阅 source。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        other.subscribe(new OtherObserver<>(observer, source));
    }

    /** Completable 观察者：onComplete 时 ResumeSingleObserver 订阅 source。 */
    static final class OtherObserver<T>
    extends AtomicReference<Disposable>
    implements CompletableObserver, Disposable {

        @Serial
        private static final long serialVersionUID = -8565274649390031272L;

        final SingleObserver<? super T> downstream;

        final SingleSource<T> source;

        OtherObserver(SingleObserver<? super T> actual, SingleSource<T> source) {
            this.downstream = actual;
            this.source = source;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d)) {

                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        /** other 完成后用 ResumeSingleObserver 桥接 source。 */
        @Override
        public void onComplete() {
            source.subscribe(new ResumeSingleObserver<>(this, downstream));
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
