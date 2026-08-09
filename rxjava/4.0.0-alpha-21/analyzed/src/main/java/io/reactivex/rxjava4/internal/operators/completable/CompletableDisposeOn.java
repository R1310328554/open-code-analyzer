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
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 在指定 {@link Scheduler} 上执行上游 disposable 的 dispose 操作。
 */
public final class CompletableDisposeOn extends Completable {

    final CompletableSource source;

    final Scheduler scheduler;

    /**
     * @param source 上游 CompletableSource
     * @param scheduler 执行 dispose 的 Scheduler
     */
    public CompletableDisposeOn(CompletableSource source, Scheduler scheduler) {
        this.source = source;
        this.scheduler = scheduler;
    }

    /** 订阅 source；下游 dispose 时在 scheduler 上异步取消上游。 */
    @Override
    protected void subscribeActual(final CompletableObserver observer) {
        source.subscribe(new DisposeOnObserver(observer, scheduler));
    }

    /** 在 scheduler 上异步 dispose 上游的内部 observer。 */
    static final class DisposeOnObserver implements CompletableObserver, Disposable, Runnable {
        final CompletableObserver downstream;

        final Scheduler scheduler;

        Disposable upstream;

        volatile boolean disposed;

        DisposeOnObserver(CompletableObserver observer, Scheduler scheduler) {
            this.downstream = observer;
            this.scheduler = scheduler;
        }

        @Override
        public void onComplete() {
            if (disposed) {
                return;
            }
            downstream.onComplete();
        }

        @Override
        public void onError(Throwable e) {
            if (disposed) {
                RxJavaPlugins.onError(e);
                return;
            }
            downstream.onError(e);
        }

        @Override
        public void onSubscribe(final Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        /** 标记 disposed 并在 scheduler 上调度上游 dispose。 */
        @Override
        public void dispose() {
            disposed = true;
            scheduler.scheduleDirect(this);
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        /** 在 scheduler 线程上 dispose 上游 disposable。 */
        @Override
        public void run() {
            upstream.dispose();
            upstream = DisposableHelper.DISPOSED;
        }
    }

}
