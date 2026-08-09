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

package io.reactivex.rxjava4.internal.operators.completable;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 隐藏上游 Completable 的身份，以及通过 onSubscribe 传递的 Disposable。
 */
public final class CompletableHide extends Completable {

    final CompletableSource source;

    /** @param source 上游 CompletableSource */
    public CompletableHide(CompletableSource source) {
        this.source = source;
    }

    @Override
    protected void subscribeActual(CompletableObserver observer) {
        source.subscribe(new HideCompletableObserver(observer));
    }

    static final class HideCompletableObserver implements CompletableObserver, Disposable {

        final CompletableObserver downstream;

        Disposable upstream;

        HideCompletableObserver(CompletableObserver downstream) {
            this.downstream = downstream;
        }

        /** dispose 上游并将自身标记为已 dispose。 */
        @Override
        public void dispose() {
            upstream.dispose();
            upstream = DisposableHelper.DISPOSED;
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        /** 包装上游 Disposable，向下游传递自身作为 Disposable。 */
        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
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
