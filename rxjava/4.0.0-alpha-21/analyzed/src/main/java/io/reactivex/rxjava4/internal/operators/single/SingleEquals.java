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

package io.reactivex.rxjava4.internal.operators.single;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 并行订阅两个 SingleSource，待两者均 onSuccess 后
 * 用 Objects.equals 比较并发射 Boolean 结果。
 *
 * @param <T> 上游元素类型
 */
public final class SingleEquals<T> extends Single<Boolean> {

    final SingleSource<? extends T> first;
    final SingleSource<? extends T> second;

    /**
     * @param first 第一个 SingleSource
     * @param second 第二个 SingleSource
     */
    public SingleEquals(SingleSource<? extends T> first, SingleSource<? extends T> second) {
        this.first = first;
        this.second = second;
    }

    /** 并行订阅 first/second，count==2 时比较 values 并 onSuccess。 */
    @Override
    protected void subscribeActual(final SingleObserver<? super Boolean> observer) {

        final AtomicInteger count = new AtomicInteger();
        final Object[] values = { null, null };

        final CompositeDisposable set = new CompositeDisposable();
        observer.onSubscribe(set);

        first.subscribe(new InnerObserver<T>(0, set, values, observer, count));
        second.subscribe(new InnerObserver<T>(1, set, values, observer, count));
    }

    /** 单路 Observer：缓存 values[index]，两路到齐后 Objects.equals 比较。 */
    record InnerObserver<T>(int index, CompositeDisposable set, Object[] values,
                            SingleObserver<? super Boolean> downstream,
                            AtomicInteger count) implements SingleObserver<T> {

        @Override
            public void onSubscribe(Disposable d) {
                set.add(d);
            }

            /** 写入 values[index]；count 达 2 时发射 equals 结果。 */
            @Override
            public void onSuccess(T value) {
                values[index] = value;

                if (count.incrementAndGet() == 2) {
                    downstream.onSuccess(Objects.equals(values[0], values[1]));
                }
            }

            /** 首错时 dispose 集合并 onError；已终止则 RxJavaPlugins.onError。 */
            @Override
            public void onError(Throwable e) {
                int state = count.getAndSet(-1);
                if (state == 0 || state == 1) {
                    set.dispose();
                    downstream.onError(e);
                } else {
                    RxJavaPlugins.onError(e);
                }
            }
        }
}
