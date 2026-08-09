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
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 先订阅主流 {@link Observable}，正常完成后订阅 {@link CompletableSource}，
 * 再 relay Completable 的 onComplete/onError。
 * <p>History: 2.1.10 - experimental
 * @param <T> 主流及输出元素类型
 * @since 2.2
 */
public final class ObservableConcatWithCompletable<T> extends AbstractObservableWithUpstream<T, T> {

    final CompletableSource other;

    /**
     * @param source 主流 Observable
     * @param other 主流完成后串行订阅的 CompletableSource
     */
    public ObservableConcatWithCompletable(Observable<T> source, CompletableSource other) {
        super(source);
        this.other = other;
    }

    /** 订阅 ConcatWithObserver 串行 relay 主流与 Completable。 */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new ConcatWithObserver<>(observer, other));
    }

    /** 主流完成后切换为 CompletableObserver。 */
    static final class ConcatWithObserver<T>
    extends AtomicReference<Disposable>
    implements Observer<T>, CompletableObserver, Disposable {

        @Serial
        private static final long serialVersionUID = -1953724749712440952L;

        final Observer<? super T> downstream;

        CompletableSource other;

        boolean inCompletable;

        ConcatWithObserver(Observer<? super T> actual, CompletableSource other) {
            this.downstream = actual;
            this.other = other;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d) && !inCompletable) {
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            downstream.onNext(t);
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        /** 主流完成时订阅 other Completable；inner 完成时 onComplete。 */
        @Override
        public void onComplete() {
            if (inCompletable) {
                downstream.onComplete();
            } else {
                inCompletable = true;
                DisposableHelper.replace(this, null);
                CompletableSource cs = other;
                other = null;
                cs.subscribe(this);
            }
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
}
