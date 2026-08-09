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
 * 顺序执行两个 {@link CompletableSource}：
 * 先订阅 source，其正常完成后才订阅 next。
 */
public final class CompletableAndThenCompletable extends Completable {

    final CompletableSource source;

    final CompletableSource next;

    /**
     * @param source 先执行的 CompletableSource
     * @param next source 完成后执行的 CompletableSource
     */
    public CompletableAndThenCompletable(CompletableSource source, CompletableSource next) {
        this.source = source;
        this.next = next;
    }

    /** 订阅 source，完成后链接到 next。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        source.subscribe(new SourceObserver(observer, next));
    }

    /** 订阅第一个 source 并在完成时启动 next 的内部 observer。 */
    static final class SourceObserver
            extends AtomicReference<Disposable>
            implements CompletableObserver, Disposable {

        @Serial
        private static final long serialVersionUID = -4101678820158072998L;

        final CompletableObserver actualObserver;

        final CompletableSource next;

        SourceObserver(CompletableObserver actualObserver, CompletableSource next) {
            this.actualObserver = actualObserver;
            this.next = next;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d)) {
                actualObserver.onSubscribe(this);
            }
        }

        @Override
        public void onError(Throwable e) {
            actualObserver.onError(e);
        }

        /** source 完成后订阅 next。 */
        @Override
        public void onComplete() {
            next.subscribe(new NextObserver(this, actualObserver));
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

    record NextObserver(AtomicReference<Disposable> parent,
                        CompletableObserver downstream) implements CompletableObserver {

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.replace(parent, d);
            }

            @Override
            public void onComplete() {
                downstream.onComplete();
            }

            @Override
            public void onError(Throwable e) {
                downstream.onError(e);
            }
        }
}
