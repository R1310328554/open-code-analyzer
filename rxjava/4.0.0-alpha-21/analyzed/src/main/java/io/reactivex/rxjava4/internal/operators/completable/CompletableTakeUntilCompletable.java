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
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 当主源或其他 {@link CompletableSource} 任一终止时结束序列。
 * <p>History: 2.1.17 - experimental
 * @since 2.2
 */
public final class CompletableTakeUntilCompletable extends Completable {

    final Completable source;

    final CompletableSource other;

    /**
     * @param source 主 Completable 源
     * @param other 触发提前终止的其他 CompletableSource
     */
    public CompletableTakeUntilCompletable(Completable source,
            CompletableSource other) {
        this.source = source;
        this.other = other;
    }

    /** 同时订阅 source 与 other；任一终止即结束并取消另一方。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        TakeUntilMainObserver parent = new TakeUntilMainObserver(observer);
        observer.onSubscribe(parent);

        other.subscribe(parent.other);
        source.subscribe(parent);
    }

    /** 监听主源终止并在 other 终止时提前完成的内部 observer。 */
    static final class TakeUntilMainObserver extends AtomicReference<Disposable>
    implements CompletableObserver, Disposable {

        @Serial
        private static final long serialVersionUID = 3533011714830024923L;

        final CompletableObserver downstream;

        final OtherObserver other;

        final AtomicBoolean once;

        TakeUntilMainObserver(CompletableObserver downstream) {
            this.downstream = downstream;
            this.other = new OtherObserver(this);
            this.once = new AtomicBoolean();
        }

        @Override
        public void dispose() {
            if (once.compareAndSet(false, true)) {
                DisposableHelper.dispose(this);
                DisposableHelper.dispose(other);
            }
        }

        @Override
        public boolean isDisposed() {
            return once.get();
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override
        public void onComplete() {
            if (once.compareAndSet(false, true)) {
                DisposableHelper.dispose(other);
                downstream.onComplete();
            }
        }

        @Override
        public void onError(Throwable e) {
            if (once.compareAndSet(false, true)) {
                DisposableHelper.dispose(other);
                downstream.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        /** other 完成时取消主源并通知 downstream 完成。 */
        void innerComplete() {
            if (once.compareAndSet(false, true)) {
                DisposableHelper.dispose(this);
                downstream.onComplete();
            }
        }

        void innerError(Throwable e) {
            if (once.compareAndSet(false, true)) {
                DisposableHelper.dispose(this);
                downstream.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        /** 监听 other CompletableSource 终止事件的内部 observer。 */
        static final class OtherObserver extends AtomicReference<Disposable>
        implements CompletableObserver {

            @Serial
            private static final long serialVersionUID = 5176264485428790318L;
            final TakeUntilMainObserver parent;

            OtherObserver(TakeUntilMainObserver parent) {
                this.parent = parent;
            }

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override
            public void onComplete() {
                parent.innerComplete();
            }

            @Override
            public void onError(Throwable e) {
                parent.innerError(e);
            }
        }
    }
}
