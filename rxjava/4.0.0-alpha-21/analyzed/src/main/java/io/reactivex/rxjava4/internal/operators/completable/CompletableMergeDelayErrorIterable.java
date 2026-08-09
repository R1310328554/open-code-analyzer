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

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.CompositeDisposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.internal.operators.completable.CompletableMergeArrayDelayError.*;
import io.reactivex.rxjava4.internal.util.AtomicThrowable;

/**
 * 并行合并 {@link Iterable} 中的 {@link CompletableSource} 并延迟错误：
 * 收集所有错误，全部源终止后一次性报告。
 */
public final class CompletableMergeDelayErrorIterable extends Completable {

    final Iterable<? extends CompletableSource> sources;

    /** @param sources 要并行合并的 CompletableSource 可迭代对象 */
    public CompletableMergeDelayErrorIterable(Iterable<? extends CompletableSource> sources) {
        this.sources = sources;
    }

    /** 迭代 sources 并并行订阅各 CompletableSource，延迟聚合错误。 */
    @Override
    public void subscribeActual(final CompletableObserver observer) {
        final CompositeDisposable set = new CompositeDisposable();

        observer.onSubscribe(set);

        Iterator<? extends CompletableSource> iterator;

        try {
            iterator = Objects.requireNonNull(sources.iterator(), "The source iterator returned is null");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            observer.onError(e);
            return;
        }

        final AtomicInteger wip = new AtomicInteger(1);

        final AtomicThrowable errors = new AtomicThrowable();
        set.add(new TryTerminateAndReportDisposable(errors));

        for (;;) {
            if (set.isDisposed()) {
                return;
            }

            boolean b;
            try {
                b = iterator.hasNext();
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                errors.tryAddThrowableOrReport(e);
                break;
            }

            if (!b) {
                break;
            }

            if (set.isDisposed()) {
                return;
            }

            CompletableSource c;

            try {
                c = Objects.requireNonNull(iterator.next(), "The iterator returned a null CompletableSource");
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                errors.tryAddThrowableOrReport(e);
                break;
            }

            if (set.isDisposed()) {
                return;
            }

            wip.getAndIncrement();

            c.subscribe(new MergeInnerCompletableObserver(observer, set, errors, wip));
        }

        if (wip.decrementAndGet() == 0) {
            errors.tryTerminateConsumer(observer);
        }
    }
}
