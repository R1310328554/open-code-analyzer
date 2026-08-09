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

package io.reactivex.rxjava4.internal.operators.observable;

import java.io.Serial;
import java.util.concurrent.atomic.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.util.*;

/**
 * 合并 {@link Observable} 与 {@link Completable}：转发 Observable 的 onNext，
 * 且两者均正常完成时才向下游 onComplete。
 * <p>History: 2.1.10 - experimental
 * @param <T> Observable 元素类型
 * @since 2.2
 */
public final class ObservableMergeWithCompletable<T> extends AbstractObservableWithUpstream<T, T> {

    final CompletableSource other;

    /**
     * @param source 主 Observable 上游
     * @param other 与之合并的 Completable
     */
    public ObservableMergeWithCompletable(Observable<T> source, CompletableSource other) {
        super(source);
        this.other = other;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        MergeWithObserver<T> parent = new MergeWithObserver<>(observer);
        observer.onSubscribe(parent);
        source.subscribe(parent);
        other.subscribe(parent.otherObserver);
    }

    /** 协调主序列与 Completable：双端均完成时经 {@link HalfSerializer} 终止。 */
    static final class MergeWithObserver<T> extends AtomicInteger
    implements Observer<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -4592979584110982903L;

        final Observer<? super T> downstream;

        final AtomicReference<Disposable> mainDisposable;

        final OtherObserver otherObserver;

        final AtomicThrowable errors;

        volatile boolean mainDone;

        volatile boolean otherDone;

        MergeWithObserver(Observer<? super T> downstream) {
            this.downstream = downstream;
            this.mainDisposable = new AtomicReference<>();
            this.otherObserver = new OtherObserver(this);
            this.errors = new AtomicThrowable();
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(mainDisposable, d);
        }

        @Override
        public void onNext(T t) {
            HalfSerializer.onNext(downstream, t, this, errors);
        }

        @Override
        public void onError(Throwable ex) {
            DisposableHelper.dispose(otherObserver);
            HalfSerializer.onError(downstream, ex, this, errors);
        }

        /** 主序列完成；若 other 已完成则向下游 onComplete。 */
        @Override
        public void onComplete() {
            mainDone = true;
            if (otherDone) {
                HalfSerializer.onComplete(downstream, this, errors);
            }
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(mainDisposable.get());
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(mainDisposable);
            DisposableHelper.dispose(otherObserver);
            errors.tryTerminateAndReport();
        }

        void otherError(Throwable ex) {
            DisposableHelper.dispose(mainDisposable);
            HalfSerializer.onError(downstream, ex, this, errors);
        }

        /** Completable 完成；若主序列已完成则向下游 onComplete。 */
        void otherComplete() {
            otherDone = true;
            if (mainDone) {
                HalfSerializer.onComplete(downstream, this, errors);
            }
        }

        /** Completable 侧 Observer，完成/错误回调至 parent。 */
        static final class OtherObserver extends AtomicReference<Disposable>
        implements CompletableObserver {

            @Serial
            private static final long serialVersionUID = -2935427570954647017L;

            final MergeWithObserver<?> parent;

            OtherObserver(MergeWithObserver<?> parent) {
                this.parent = parent;
            }

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override
            public void onError(Throwable e) {
                parent.otherError(e);
            }

            @Override
            public void onComplete() {
                parent.otherComplete();
            }
        }
    }
}
