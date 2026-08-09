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
 * 在 Completable 终止时断开上游与下游之间的引用，便于垃圾回收。
 * <p>History: 2.1.5 - experimental
 * @since 2.2
 */
public final class CompletableDetach extends Completable {

    final CompletableSource source;

    /** @param source 上游 CompletableSource */
    public CompletableDetach(CompletableSource source) {
        this.source = source;
    }

    /** 订阅 source 并在终止后清除对下游的引用。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        source.subscribe(new DetachCompletableObserver(observer));
    }

    /** 终止时断开 upstream/downstream 引用的内部 observer。 */
    static final class DetachCompletableObserver implements CompletableObserver, Disposable {

        CompletableObserver downstream;

        Disposable upstream;

        DetachCompletableObserver(CompletableObserver downstream) {
            this.downstream = downstream;
        }

        /** 清除 downstream 引用并 dispose 上游。 */
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

        @Override
        public void onError(Throwable e) {
            upstream = DisposableHelper.DISPOSED;
            CompletableObserver a = downstream;
            if (a != null) {
                downstream = null;
                a.onError(e);
            }
        }

        @Override
        public void onComplete() {
            upstream = DisposableHelper.DISPOSED;
            CompletableObserver a = downstream;
            if (a != null) {
                downstream = null;
                a.onComplete();
            }
        }
    }
}
