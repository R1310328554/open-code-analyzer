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
import io.reactivex.rxjava4.functions.Predicate;
import io.reactivex.rxjava4.internal.observers.BasicFuseableObserver;

/**
 * 按 {@link Predicate} 过滤上游元素，仅转发 test 为 true 的 onNext。
 * 支持 queue fusion 的 poll 路径同步过滤。
 *
 * @param <T> 上游元素类型
 */
public final class ObservableFilter<T> extends AbstractObservableWithUpstream<T, T> {
    final Predicate<? super T> predicate;
    /**
     * @param source 上游 ObservableSource
     * @param predicate 逐元素测试谓词
     */
    public ObservableFilter(ObservableSource<T> source, Predicate<? super T> predicate) {
        super(source);
        this.predicate = predicate;
    }

    /** 订阅 FilterObserver 并应用 predicate。 */
    @Override
    public void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new FilterObserver<>(observer, predicate));
    }

    /** 非 fusion 模式下 test 通过才 onNext；fusion 模式下 poll 循环过滤。 */
    static final class FilterObserver<T> extends BasicFuseableObserver<T, T> {
        final Predicate<? super T> filter;

        FilterObserver(Observer<? super T> actual, Predicate<? super T> filter) {
            super(actual);
            this.filter = filter;
        }

        /** predicate.test 为 true 时转发 t；fusion 模式发 null 占位。 */
        @Override
        public void onNext(T t) {
            if (sourceMode == NONE) {
                boolean b;
                try {
                    b = filter.test(t);
                } catch (Throwable e) {
                    fail(e);
                    return;
                }
                if (b) {
                    downstream.onNext(t);
                }
            } else {
                downstream.onNext(null);
            }
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        /** 从队列 poll 直至元素通过 filter 或队列空。 */
        @Nullable
        @Override
        public T poll() throws Throwable {
            for (;;) {
                T v = qd.poll();
                if (v == null || filter.test(v)) {
                    return v;
                }
            }
        }
    }
}
