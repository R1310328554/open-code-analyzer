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
 * 以 ArrayDeque 滑动窗口缓存上游最后 count 个元素，
 * 上游 onComplete 后依次 poll 并 onNext。
 * @param <T> 元素类型
 */
public final class ObservableTakeLast<T> extends AbstractObservableWithUpstream<T, T> {
    final int count;

    /**
     * @param source 上游 ObservableSource
     * @param count 保留的末尾元素个数
     */
    public ObservableTakeLast(ObservableSource<T> source, int count) {
        super(source);
        this.count = count;
    }

    /** 订阅 TakeLastObserver（继承 ArrayDeque）。 */
    @Override
    public void subscribeActual(Observer<? super T> t) {
        source.subscribe(new TakeLastObserver<>(t, count));
    }

    /** deque 满时 poll 队首再 offer；onComplete 时 drain 发射。 */
    static final class TakeLastObserver<T> extends ArrayDeque<T> implements Observer<T>, Disposable {

        @Serial
        private static final long serialVersionUID = 7240042530241604978L;
        final Observer<? super T> downstream;
        final int count;

        Disposable upstream;

        volatile boolean cancelled;

        TakeLastObserver(Observer<? super T> actual, int count) {
            this.downstream = actual;
            this.count = count;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        /** 队列 size 达 count 时 poll 最旧元素再 offer 新值。 */
        @Override
        public void onNext(T t) {
            if (count == size()) {
                poll();
            }
            offer(t);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        /** 循环 poll 并 onNext 直至 deque 空，然后 onComplete。 */
        @Override
        public void onComplete() {
            Observer<? super T> a = downstream;
            for (;;) {
                if (cancelled) {
                    return;
                }
                T v = poll();
                if (v == null) {
                    a.onComplete();
                    return;
                }
                a.onNext(v);
            }
        }

        @Override
        public void dispose() {
            if (!cancelled) {
                cancelled = true;
                upstream.dispose();
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled;
        }
    }
}
