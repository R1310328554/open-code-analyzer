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

package io.reactivex.rxjava4.internal.operators.parallel;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.parallel.ParallelFlowable;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * 将多个 Publisher 包装为 ParallelFlowable，各 Publisher 对应一条并行轨道。
 *
 * @param <T> 元素类型
 */
public final class ParallelFromArray<T> extends ParallelFlowable<T> {
    final Publisher<T>[] sources;

    /** @param sources 与并行度等长的 Publisher 数组 */
    public ParallelFromArray(Publisher<T>[] sources) {
        this.sources = sources;
    }

    @Override
    public int parallelism() {
        return sources.length;
    }

    /** 按索引将 sources[i] 订阅至 subscribers[i]。 */
    @Override
    public void subscribe(Subscriber<? super T>[] subscribers) {
        subscribers = RxJavaPlugins.onSubscribe(this, subscribers);

        if (!validate(subscribers)) {
            return;
        }

        int n = subscribers.length;

        for (int i = 0; i < n; i++) {
            sources[i].subscribe(subscribers[i]);
        }
    }
}
