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
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Predicate;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;
import io.reactivex.rxjava4.internal.fuseable.FuseToObservable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * {@link ObservableAny} 的 Single 变体：
 * 存在满足 predicate 的元素时 onSuccess(true)，否则 onSuccess(false)。
 *
 * @param <T> 上游元素类型
 */
public final class ObservableAnySingle<T> extends Single<Boolean> implements FuseToObservable<Boolean> {
    final ObservableSource<T> source;

    final Predicate<? super T> predicate;

    /**
     * @param source 上游 ObservableSource
     * @param predicate 逐元素测试谓词
     */
    public ObservableAnySingle(ObservableSource<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    /** 订阅 AnyObserver 并映射结果为 Single 信号。 */
    @Override
    protected void subscribeActual(SingleObserver<? super Boolean> t) {
        source.subscribe(new AnyObserver<>(t, predicate));
    }

    /** 融合为 {@link ObservableAny} 实例。 */
    @Override
    public Observable<Boolean> fuseToObservable() {
        return RxJavaPlugins.onAssembly(new ObservableAny<>(source, predicate));
    }

    /** 逐元素测试 predicate，首个 true 或完成/错误时以 onSuccess 终止。 */
    static final class AnyObserver<T> implements Observer<T>, Disposable {

        final SingleObserver<? super Boolean> downstream;
        final Predicate<? super T> predicate;

        Disposable upstream;

        boolean done;

        AnyObserver(SingleObserver<? super Boolean> actual, Predicate<? super T> predicate) {
            this.downstream = actual;
            this.predicate = predicate;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        /** predicate 为 true 时 dispose 上游并 onSuccess(true)。 */
        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            boolean b;
            try {
                b = predicate.test(t);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                upstream.dispose();
                onError(e);
                return;
            }
            if (b) {
                done = true;
                upstream.dispose();
                downstream.onSuccess(true);
            }
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

        /** 上游完成且无匹配时 onSuccess(false)。 */
        @Override
        public void onComplete() {
            if (!done) {
                done = true;
                downstream.onSuccess(false);
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
    }
}
