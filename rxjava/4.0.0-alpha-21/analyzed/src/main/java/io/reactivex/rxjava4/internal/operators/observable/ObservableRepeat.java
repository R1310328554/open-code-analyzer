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
import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;

/**
 * 上游 onComplete 后按 count 次数重新订阅（Long.MAX_VALUE 表示无限）。
 *
 * @param <T> 元素类型
 */
public final class ObservableRepeat<T> extends AbstractObservableWithUpstream<T, T> {
    final long count;
    /**
     * @param source 上游 Observable
     * @param count 重复次数（含首次后的重订阅次数）
     */
    public ObservableRepeat(Observable<T> source, long count) {
        super(source);
        this.count = count;
    }

    /** 创建 RepeatObserver 并在 onComplete 时 subscribeNext 重订阅。 */
    @Override
    public void subscribeActual(Observer<? super T> observer) {
        SequentialDisposable sd = new SequentialDisposable();
        observer.onSubscribe(sd);

        RepeatObserver<T> rs = new RepeatObserver<>(observer, count != Long.MAX_VALUE ? count - 1 : Long.MAX_VALUE, sd, source);
        rs.subscribeNext();
    }

    /** 转发 onNext；onComplete 时递减 remaining 并重订阅或完成。 */
    static final class RepeatObserver<T> extends AtomicInteger implements Observer<T> {

        @Serial
        private static final long serialVersionUID = -7098360935104053232L;

        final Observer<? super T> downstream;
        final SequentialDisposable sd;
        final ObservableSource<? extends T> source;
        long remaining;
        RepeatObserver(Observer<? super T> actual, long count, SequentialDisposable sd, ObservableSource<? extends T> source) {
            this.downstream = actual;
            this.sd = sd;
            this.source = source;
            this.remaining = count;
        }

        @Override
        public void onSubscribe(Disposable d) {
            sd.replace(d);
        }

        @Override
        public void onNext(T t) {
            downstream.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            long r = remaining;
            if (r != Long.MAX_VALUE) {
                remaining = r - 1;
            }
            if (r != 0L) {
                subscribeNext();
            } else {
                downstream.onComplete();
            }
        }

        /** 通过 trampolining 再次订阅上游（missed 计数防重入）。 */
        void subscribeNext() {
            if (getAndIncrement() == 0) {
                int missed = 1;
                for (;;) {
                    if (sd.isDisposed()) {
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
