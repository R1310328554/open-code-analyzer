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

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.functions.Consumer;
import io.reactivex.rxjava4.internal.observers.BasicFuseableObserver;

/**
 * 在向下游 onNext 之后调用 {@link Consumer} 副作用。
 * <p>History: 2.0.1 - experimental
 * @param <T> 元素类型
 * @since 2.1
 */
public final class ObservableDoAfterNext<T> extends AbstractObservableWithUpstream<T, T> {

    final Consumer<? super T> onAfterNext;

    /**
     * @param source 上游 ObservableSource
     * @param onAfterNext 每个元素转发后执行的 Consumer
     */
    public ObservableDoAfterNext(ObservableSource<T> source, Consumer<? super T> onAfterNext) {
        super(source);
        this.onAfterNext = onAfterNext;
    }

    /** 订阅 DoAfterObserver。 */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new DoAfterObserver<>(observer, onAfterNext));
    }

    /** 先 downstream.onNext 再 onAfterNext.accept。 */
    static final class DoAfterObserver<T> extends BasicFuseableObserver<T, T> {

        final Consumer<? super T> onAfterNext;

        DoAfterObserver(Observer<? super T> actual, Consumer<? super T> onAfterNext) {
            super(actual);
            this.onAfterNext = onAfterNext;
        }

        /** 转发后执行 onAfterNext；fusion 模式下 poll 后同样回调。 */
        @Override
        public void onNext(T t) {
            downstream.onNext(t);

            if (sourceMode == NONE) {
                try {
                    onAfterNext.accept(t);
                } catch (Throwable ex) {
                    fail(ex);
                }
            }
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Nullable
        @Override
        public T poll() throws Throwable {
            T v = qd.poll();
            if (v != null) {
                onAfterNext.accept(v);
            }
            return v;
        }
    }
}
