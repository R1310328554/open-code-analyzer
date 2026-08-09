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

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 在指定 {@link Scheduler} 上调度上游 {@link CompletableSource}
 * 终止事件（完成或错误）后再通知下游。
 */
public final class CompletableObserveOn extends Completable {

    final CompletableSource source;

    final Scheduler scheduler;
    /**
     * @param source 上游 CompletableSource
     * @param scheduler 执行下游通知的 Scheduler
     */
    public CompletableObserveOn(CompletableSource source, Scheduler scheduler) {
        this.source = source;
        this.scheduler = scheduler;
    }

    /** 订阅 source 并在 scheduler 上转发终止事件。 */
    @Override
    protected void subscribeActual(final CompletableObserver observer) {
        source.subscribe(new ObserveOnCompletableObserver(observer, scheduler));
    }

    /** 缓存终止事件并在 scheduler 上异步通知 downstream 的内部 observer。 */
    static final class ObserveOnCompletableObserver
    extends AtomicReference<Disposable>
    implements CompletableObserver, Disposable, Runnable {

        @Serial
        private static final long serialVersionUID = 8571289934935992137L;

        final CompletableObserver downstream;

        final Scheduler scheduler;

        Throwable error;

        ObserveOnCompletableObserver(CompletableObserver actual, Scheduler scheduler) {
            this.downstream = actual;
            this.scheduler = scheduler;
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d)) {
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onError(Throwable e) {
            this.error = e;
            DisposableHelper.replace(this, scheduler.scheduleDirect(this));
        }

        @Override
        public void onComplete() {
            DisposableHelper.replace(this, scheduler.scheduleDirect(this));
        }

        /** 在 scheduler 线程上向 downstream 发出缓存的错误或完成。 */
        @Override
        public void run() {
            Throwable ex = error;
            if (ex != null) {
                error = null;
                downstream.onError(ex);
            } else {
                downstream.onComplete();
            }
        }
    }

}
