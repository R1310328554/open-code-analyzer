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

import java.util.NoSuchElementException;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 消费上游 {@link ObservableSource} 并发射最后一个元素：
 * 有元素时 onSuccess；为空时发 defaultItem 或 {@link NoSuchElementException}。
 *
 * @param <T> 上游元素类型
 */
public final class ObservableLastSingle<T> extends Single<T> {

    final ObservableSource<T> source;

    final T defaultItem;

    /**
     * @param source 上游 ObservableSource
     * @param defaultItem 上游为空时的默认值（null 表示无默认值）
     */
    public ObservableLastSingle(ObservableSource<T> source, T defaultItem) {
        this.source = source;
        this.defaultItem = defaultItem;
    }

    // TODO 融合回 Observable

    /** 订阅 LastObserver 并将最后一项映射为 Single 信号。 */
    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new LastObserver<>(observer, defaultItem));
    }

    /** 缓存最后一项，完成时 onSuccess 或处理 defaultItem 逻辑。 */
    static final class LastObserver<T> implements Observer<T>, Disposable {

        final SingleObserver<? super T> downstream;

        final T defaultItem;

        Disposable upstream;

        T item;

        LastObserver(SingleObserver<? super T> actual, T defaultItem) {
            this.downstream = actual;
            this.defaultItem = defaultItem;
        }

        @Override
        public void dispose() {
            upstream.dispose();
            upstream = DisposableHelper.DISPOSED;
        }

        @Override
        public boolean isDisposed() {
            return upstream == DisposableHelper.DISPOSED;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;

                downstream.onSubscribe(this);
            }
        }

        /** 覆盖保存最后一项。 */
        @Override
        public void onNext(T t) {
            item = t;
        }

        @Override
        public void onError(Throwable t) {
            upstream = DisposableHelper.DISPOSED;
            item = null;
            downstream.onError(t);
        }

        /** 有缓存项或 defaultItem 时 onSuccess，否则 onError(NoSuchElementException)。 */
        @Override
        public void onComplete() {
            upstream = DisposableHelper.DISPOSED;
            T v = item;
            if (v != null) {
                item = null;
                downstream.onSuccess(v);
            } else {
                v = defaultItem;
                if (v != null) {
                    downstream.onSuccess(v);
                } else {
                    downstream.onError(new NoSuchElementException());
                }
            }
        }
    }
}
