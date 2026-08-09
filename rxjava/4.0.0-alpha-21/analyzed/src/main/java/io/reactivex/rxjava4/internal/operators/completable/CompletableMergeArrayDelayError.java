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

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.internal.util.AtomicThrowable;

/**
 * 并行合并 {@link CompletableSource} 数组并延迟错误：
 * 收集所有错误，全部源终止后一次性报告。
 */
public final class CompletableMergeArrayDelayError extends Completable {

    final CompletableSource[] sources;

    /** @param sources 要并行合并的 CompletableSource 数组 */
    public CompletableMergeArrayDelayError(CompletableSource[] sources) {
        this.sources = sources;
    }

    /** 订阅数组中所有源并延迟聚合错误。 */
    @Override
    public void subscribeActual(final CompletableObserver observer) {
        final CompositeDisposable set = new CompositeDisposable();
        final AtomicInteger wip = new AtomicInteger(sources.length + 1);

        final AtomicThrowable errors = new AtomicThrowable();
        set.add(new TryTerminateAndReportDisposable(errors));

        observer.onSubscribe(set);

        for (CompletableSource c : sources) {
            if (set.isDisposed()) {
                return;
            }

            if (c == null) {
                Throwable ex = new NullPointerException("A completable source is null");
                errors.tryAddThrowableOrReport(ex);
                wip.decrementAndGet();
                continue;
            }

            c.subscribe(new MergeInnerCompletableObserver(observer, set, errors, wip));
        }

        if (wip.decrementAndGet() == 0) {
            errors.tryTerminateConsumer(observer);
        }
    }

    /** dispose 时尝试终止并上报未交付的错误。 */
    record TryTerminateAndReportDisposable(AtomicThrowable errors) implements Disposable {

        @Override
        public void dispose() {
            errors.tryTerminateAndReport();
        }

        @Override
        public boolean isDisposed() {
            return errors.isTerminated();
        }
    }

    /** 单个内部源的 observer；完成或错误时递减 wip 并尝试终止。 */
    record MergeInnerCompletableObserver(CompletableObserver downstream, CompositeDisposable set,
                                         AtomicThrowable errors, AtomicInteger wip)
            implements CompletableObserver {

        @Override
        public void onSubscribe(Disposable d) {
            set.add(d);
        }

        @Override
        public void onError(Throwable e) {
            if (errors.tryAddThrowableOrReport(e)) {
                tryTerminate();
            }
        }

        @Override
        public void onComplete() {
            tryTerminate();
        }

        /** wip 归零时向 downstream 发出聚合的终止事件。 */
        void tryTerminate() {
            if (wip.decrementAndGet() == 0) {
                errors.tryTerminateConsumer(downstream);
            }
        }
    }
}
