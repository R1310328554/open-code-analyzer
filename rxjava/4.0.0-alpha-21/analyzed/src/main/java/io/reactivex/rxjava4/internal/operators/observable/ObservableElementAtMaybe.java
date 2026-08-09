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

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.fuseable.FuseToObservable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 取上游第 index 个（0-based）元素并以 {@link Maybe} 信号返回：
 * 命中时 onSuccess，上游完成而未命中时 onComplete。
 *
 * @param <T> 上游元素类型
 */
public final class ObservableElementAtMaybe<T> extends Maybe<T> implements FuseToObservable<T> {
    final ObservableSource<T> source;
    final long index;
    /**
     * @param source 上游 ObservableSource
     * @param index 目标元素索引（0-based）
     */
    public ObservableElementAtMaybe(ObservableSource<T> source, long index) {
        this.source = source;
        this.index = index;
    }

    /** 订阅 ElementAtObserver 并计数至 index。 */
    @Override
    public void subscribeActual(MaybeObserver<? super T> t) {
        source.subscribe(new ElementAtObserver<>(t, index));
    }

    /** 融合为 {@link ObservableElementAt}（无默认值、元素不足时 onComplete）。 */
    @Override
    public Observable<T> fuseToObservable() {
        return RxJavaPlugins.onAssembly(new ObservableElementAt<>(source, index, null, false));
    }

    /** 计数上游元素，count==index 时 onSuccess 并 dispose 上游。 */
    static final class ElementAtObserver<T> implements Observer<T>, Disposable {
        final MaybeObserver<? super T> downstream;
        final long index;

        Disposable upstream;

        long count;

        boolean done;

        ElementAtObserver(MaybeObserver<? super T> actual, long index) {
            this.downstream = actual;
            this.index = index;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        /** count 达 index 时 onSuccess 并终止上游。 */
        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            long c = count;
            if (c == index) {
                done = true;
                upstream.dispose();
                downstream.onSuccess(t);
                return;
            }
            count = c + 1;
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            downstream.onError(t);
        }

        /** 未命中 index 时 onComplete（空 Maybe）。 */
        @Override
        public void onComplete() {
            if (!done) {
                done = true;
                downstream.onComplete();
            }
        }
    }
}
