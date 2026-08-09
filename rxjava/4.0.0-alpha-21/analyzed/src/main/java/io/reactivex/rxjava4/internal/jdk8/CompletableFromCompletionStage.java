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

package io.reactivex.rxjava4.internal.jdk8;

import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.Disposable;
import io.reactivex.rxjava4.internal.jdk8.FlowableFromCompletionStage.BiConsumerAtomicReference;

/**
 * 包装 {@link CompletionStage} 并将其结果作为信号发出。
 * @param <T> CompletionStage 的元素类型
 * @since 3.0.0
 */
public final class CompletableFromCompletionStage<T> extends Completable {

    final CompletionStage<T> stage;

    public CompletableFromCompletionStage(CompletionStage<T> stage) {
        this.stage = stage;
    }

    @Override
    protected void subscribeActual(CompletableObserver observer) {
        // 需要一层间接引用：无法从 whenComplete 解绑，且取消时不应继续持有 stage。
        BiConsumerAtomicReference<Object> whenReference = new BiConsumerAtomicReference<>();
        CompletionStageHandler<Object> handler = new CompletionStageHandler<>(observer, whenReference);
        whenReference.lazySet(handler);

        observer.onSubscribe(handler);
        stage.whenComplete(whenReference);
    }

    record CompletionStageHandler<T>(CompletableObserver downstream, BiConsumerAtomicReference<T> whenReference)
            implements Disposable, BiConsumer<T, Throwable> {

        @Override
            public void accept(T item, Throwable error) {
                if (error != null) {
                    downstream.onError(error);
                } else {
                    downstream.onComplete();
                }
            }

            @Override
            public void dispose() {
                whenReference.set(null);
            }

            @Override
            public boolean isDisposed() {
                return whenReference.get() == null;
            }
        }
}
