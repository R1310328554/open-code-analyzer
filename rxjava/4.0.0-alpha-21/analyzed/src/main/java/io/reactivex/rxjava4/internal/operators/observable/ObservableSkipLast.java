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
import java.util.ArrayDeque;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.disposables.DisposableHelper;

/**
 * 滑动窗口跳过最后 skip 个元素：用固定容量 {@link ArrayDeque} 缓冲，
 * 队列满后再 onNext 时先 poll 队首再 offer 新项。
 *
 * @param <T> 元素类型
 */
public final class ObservableSkipLast<T> extends AbstractObservableWithUpstream<T, T> {
    final int skip;

    public ObservableSkipLast(ObservableSource<T> source, int skip) {
        super(source);
        this.skip = skip;
    }

    @Override
    public void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new SkipLastObserver<>(observer, skip));
    }

    /** 自身即容量为 skip 的 deque；满员后 emit poll() 再 offer 新值。 */
    static final class SkipLastObserver<T> extends ArrayDeque<T> implements Observer<T>, Disposable {

        @Serial
        private static final long serialVersionUID = -3807491841935125653L;
        final Observer<? super T> downstream;
        final int skip;

        Disposable upstream;

        SkipLastObserver(Observer<? super T> actual, int skip) {
            super(skip);
            this.downstream = actual;
            this.skip = skip;
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

        /** size()==skip 时先下发队首，再将 t 入队。 */
        @Override
        public void onNext(T t) {
            if (skip == size()) {
                downstream.onNext(poll());
            }
            offer(t);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}
