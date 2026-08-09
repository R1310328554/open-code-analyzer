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
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.functions.BiPredicate;
import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;

/**
 * onError 时用 BiPredicate&lt;Integer, Throwable&gt; 判定是否重试；
 * 第一个参数为当前重试次数（从 1 起）。
 *
 * @param <T> 元素类型
 */
    final BiPredicate<? super Integer, ? super Throwable> predicate;
    /**
     * @param source 上游 Observable
     * @param predicate (重试次数, 异常) -> 是否继续重试
     */
    public ObservableRetryBiPredicate(
            Observable<T> source,
            BiPredicate<? super Integer, ? super Throwable> predicate) {
        super(source);
        this.predicate = predicate;
    }

    /** 创建 RetryBiObserver，onError 时 test predicate 决定是否 subscribeNext。 */
    @Override
    public void subscribeActual(Observer<? super T> observer) {
        SequentialDisposable sa = new SequentialDisposable();
        observer.onSubscribe(sa);

        RetryBiObserver<T> rs = new RetryBiObserver<>(observer, predicate, sa, source);
        rs.subscribeNext();
    }

    /** onError 递增 retries 并 test predicate；true 则重订阅。 */
    static final class RetryBiObserver<T> extends AtomicInteger implements Observer<T> {

        @Serial
        private static final long serialVersionUID = -7098360935104053232L;

        final Observer<? super T> downstream;
        final SequentialDisposable upstream;
        final ObservableSource<? extends T> source;
        final BiPredicate<? super Integer, ? super Throwable> predicate;
        int retries;
        RetryBiObserver(Observer<? super T> actual,
                BiPredicate<? super Integer, ? super Throwable> predicate, SequentialDisposable sa, ObservableSource<? extends T> source) {
            this.downstream = actual;
            this.upstream = sa;
            this.source = source;
            this.predicate = predicate;
        }

        @Override
        public void onSubscribe(Disposable d) {
            upstream.replace(d);
        }

        @Override
        public void onNext(T t) {
            downstream.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            boolean b;
            try {
                b = predicate.test(++retries, t);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                downstream.onError(new CompositeException(t, e));
                return;
            }
            if (!b) {
                downstream.onError(t);
                return;
            }
            subscribeNext();
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        /** 通过 trampolining 再次订阅上游。 */
        void subscribeNext() {
            if (getAndIncrement() == 0) {
                int missed = 1;
                for (;;) {
                    if (upstream.isDisposed()) {
                        return;
                    }
                    source.subscribe(this);

                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                }
            }
        }
    }
}
