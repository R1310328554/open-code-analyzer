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
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 并行合并 {@link CompletableSource} 数组中的所有源；
 * 全部正常完成后才通知下游完成，首个错误立即终止其余源。
 */
public final class CompletableMergeArray extends Completable {
    final CompletableSource[] sources;

    /** @param sources 要并行合并的 CompletableSource 数组 */
    public CompletableMergeArray(CompletableSource[] sources) {
        this.sources = sources;
    }

    /** 订阅数组中所有 CompletableSource 并共享完成计数。 */
    @Override
    public void subscribeActual(final CompletableObserver observer) {
        final CompositeDisposable set = new CompositeDisposable();
        final AtomicBoolean once = new AtomicBoolean();

        InnerCompletableObserver shared = new InnerCompletableObserver(observer, once, set, sources.length + 1);
        observer.onSubscribe(shared);

        for (CompletableSource c : sources) {
            if (set.isDisposed()) {
                return;
            }

            if (c == null) {
                set.dispose();
                NullPointerException npe = new NullPointerException("A completable source is null");
                shared.onError(npe);
                return;
            }

            c.subscribe(shared);
        }

        shared.onComplete();
    }

    /** 共享完成计数并在全部完成或首个错误时通知下游。 */
    static final class InnerCompletableObserver extends AtomicInteger implements CompletableObserver, Disposable {
        @Serial
        private static final long serialVersionUID = -8360547806504310570L;

        final CompletableObserver downstream;

        final AtomicBoolean once;

        final CompositeDisposable set;

        InnerCompletableObserver(CompletableObserver actual, AtomicBoolean once, CompositeDisposable set, int n) {
            this.downstream = actual;
            this.once = once;
            this.set = set;
            this.lazySet(n);
        }

        @Override
        public void onSubscribe(Disposable d) {
            set.add(d);
        }

        /** 取消所有内部源并转发首个错误。 */
        @Override
        public void onError(Throwable e) {
            set.dispose();
            if (once.compareAndSet(false, true)) {
                downstream.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        /** 递减计数；全部完成后通知下游完成。 */
        @Override
        public void onComplete() {
            if (decrementAndGet() == 0) {
                // errors don't decrement this
                downstream.onComplete();
            }
        }

        @Override
        public void dispose() {
            set.dispose();
            once.set(true);
        }

        @Override
        public boolean isDisposed() {
            return set.isDisposed();
        }
    }
}
