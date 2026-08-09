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
import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;

/**
 * 若主序列在 onComplete 前未发射任何 onNext，则订阅 other 作为替代；
 * 否则行为与主序列相同。
 *
 * @param <T> 元素类型
 */
public final class ObservableSwitchIfEmpty<T> extends AbstractObservableWithUpstream<T, T> {
    final ObservableSource<? extends T> other;
    /**
     * @param source 主 ObservableSource
     * @param other 主序列空完成时的备用源
     */
    public ObservableSwitchIfEmpty(ObservableSource<T> source, ObservableSource<? extends T> other) {
        super(source);
        this.other = other;
    }

    @Override
    public void subscribeActual(Observer<? super T> t) {
        SwitchIfEmptyObserver<T> parent = new SwitchIfEmptyObserver<>(t, other);
        t.onSubscribe(parent.arbiter);
        source.subscribe(parent);
    }

    /** empty 初始 true；首个 onNext 置 false；空完成时 other.subscribe(this) 复用同一 Observer。 */
    static final class SwitchIfEmptyObserver<T> implements Observer<T> {
        final Observer<? super T> downstream;
        final ObservableSource<? extends T> other;
        final SequentialDisposable arbiter;

        boolean empty;

        SwitchIfEmptyObserver(Observer<? super T> actual, ObservableSource<? extends T> other) {
            this.downstream = actual;
            this.other = other;
            this.empty = true;
            this.arbiter = new SequentialDisposable();
        }

        @Override
        public void onSubscribe(Disposable d) {
            arbiter.update(d);
        }

        @Override
        public void onNext(T t) {
            if (empty) {
                empty = false;
            }
            downstream.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        /** empty 仍为 true 时切换订阅 other；否则正常 onComplete。 */
        @Override
        public void onComplete() {
            if (empty) {
                empty = false;
                other.subscribe(this);
            } else {
                downstream.onComplete();
            }
        }
    }
}
