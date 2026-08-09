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

package io.reactivex.rxjava4.internal.operators.streamable;

import java.util.Objects;
import java.util.concurrent.*;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.core.StreamSink;
import io.reactivex.rxjava4.exceptions.Exceptions;
import io.reactivex.rxjava4.functions.Function;

/**
 * 通过 onNext/onFinish 两个 lambda 回调创建 {@link StreamSink}。
 * @param <T> 流元素类型
 * @param onNext {@code next} 方法的回调
 * @param onFinish {@code finish} 方法的回调
 * @since 4.0.0
 */
public record StreamSinkLambda<@NonNull T>(
        @NonNull Function<? super T, ? extends CompletionStage<Boolean>> onNext,
        @NonNull Function<? super Throwable, ? extends CompletionStage<Void>> onFinish
) implements StreamSink<T> {

    /** 调用 onNext.apply；异常或 null 返回 failedStage。 */
    @Override
    public @NonNull CompletionStage<Boolean> next(@NonNull T item) {
        try {
            return Objects.requireNonNull(onNext.apply(item), "onNext returned a null CompletionStage");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            return CompletableFuture.failedStage(ex);
        }
    }

    /** 调用 onFinish.apply；异常时 addSuppressed 原 throwable 并 failedStage。 */
    @Override
    public @NonNull CompletionStage<Void> finish(@Nullable Throwable throwable) {
        try {
            return Objects.requireNonNull(onFinish.apply(throwable), "onFinish returned a null CompletionStage");
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            if (throwable != null) {
                ex.addSuppressed(throwable);
            }
            return CompletableFuture.failedStage(ex);
        }
    }
}
