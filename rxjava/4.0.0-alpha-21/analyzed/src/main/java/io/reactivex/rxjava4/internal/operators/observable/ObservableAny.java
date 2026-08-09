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
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 判断上游是否存在任一元素满足 {@link Predicate}；
 * 存在则 onNext(true) 后 onComplete，上游正常结束且无匹配时 onNext(false) 后 onComplete。
 *
 * @param <T> 上游元素类型
 */
public final class ObservableAny<T> extends AbstractObservableWithUpstream<T, Boolean> {
    final Predicate<? super T> predicate;
    /**
     * @param source 上游 ObservableSource
     * @param predicate 逐元素测试谓词
     */
    public ObservableAny(ObservableSource<T> source, Predicate<? super T> predicate) {
        super(source);
        this.predicate = predicate;
    }

    /** 订阅 AnyObserver 并在首个匹配或完成时终止。 */
    @Override
    protected void subscribeActual(Observer<? super Boolean> t) {
        source.subscribe(new AnyObserver<>(t, predicate));
    }

    /** 逐元素测试 predicate，首个 true 或完成/错误时终止。 */
    static final class AnyObserver<T> implements Observer<T>, Disposable {

        final Observer<? super Boolean> downstream;
        final Predicate<? super T> predicate;

        Disposable upstream;

        boolean done;

        AnyObserver(Observer<? super Boolean> actual, Predicate<? super T> predicate) {
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

        /** predicate 为 true 时 dispose 上游并 emit true 后 onComplete。 */
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
                downstream.onNext(true);
                downstream.onComplete();
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

        /** 上游完成且无匹配时 emit false 后 onComplete。 */
        @Override
        public void onComplete() {
            if (!done) {
                done = true;
                downstream.onNext(false);
                downstream.onComplete();
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
