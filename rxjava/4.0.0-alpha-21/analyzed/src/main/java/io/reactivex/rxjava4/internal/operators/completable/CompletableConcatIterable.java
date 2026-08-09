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
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.internal.disposables.*;

/**
 * 按 {@link Iterable} 迭代顺序串联多个 {@link CompletableSource}，
 * 前一个正常完成后才订阅下一个。
 */
public final class CompletableConcatIterable extends Completable {
    final Iterable<? extends CompletableSource> sources;

    /** @param sources 要顺序执行的 CompletableSource 可迭代对象 */
    public CompletableConcatIterable(Iterable<? extends CompletableSource> sources) {
        this.sources = sources;
    }

    /** 从 Iterable 迭代器开始顺序订阅各源。 */
    @Override
    public void subscribeActual(CompletableObserver observer) {

        Iterator<? extends CompletableSource> it;

        try {
            it = Objects.requireNonNull(sources.iterator(), "The iterator returned is null");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            EmptyDisposable.error(e, observer);
            return;
        }

        ConcatInnerObserver inner = new ConcatInnerObserver(observer, it);
        observer.onSubscribe(inner.sd);
        inner.next();
    }

    /** 顺序遍历 Iterable 并订阅各源的内部 observer。 */
    static final class ConcatInnerObserver extends AtomicInteger implements CompletableObserver {

        @Serial
        private static final long serialVersionUID = -7965400327305809232L;

        final CompletableObserver downstream;
        final Iterator<? extends CompletableSource> sources;

        final SequentialDisposable sd;

        ConcatInnerObserver(CompletableObserver actual, Iterator<? extends CompletableSource> sources) {
            this.downstream = actual;
            this.sources = sources;
            this.sd = new SequentialDisposable();
        }

        @Override
        public void onSubscribe(Disposable d) {
            sd.replace(d);
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            next();
        }

        /** 订阅迭代器中的下一个 CompletableSource，或全部完成后通知下游。 */
        void next() {
            if (sd.isDisposed()) {
                return;
            }

            if (getAndIncrement() != 0) {
                return;
            }

            Iterator<? extends CompletableSource> a = sources;
            do {
                if (sd.isDisposed()) {
                    return;
                }

                boolean b;
                try {
                    b = a.hasNext();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    downstream.onError(ex);
                    return;
                }

                if (!b) {
                    downstream.onComplete();
                    return;
                }

                CompletableSource c;

                try {
                    c = Objects.requireNonNull(a.next(), "The CompletableSource returned is null");
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    downstream.onError(ex);
                    return;
                }

                c.subscribe(this);
            } while (decrementAndGet() != 0);
        }
    }
}
