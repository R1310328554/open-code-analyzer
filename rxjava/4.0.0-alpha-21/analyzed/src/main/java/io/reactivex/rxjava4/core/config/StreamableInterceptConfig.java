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

package io.reactivex.rxjava4.core.config;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.functions.*;

/**
 * intercept() 算子的配置 record，包含各生命周期阶段的转换回调
 * @param <T> 序列的元素类型
 * @param onStream 调用 {@link Streamable#stream(StreamerCancellation)} 时触发
 * @param onNext 调用 {@link Streamer#next()} 时触发
 * @param onCurrent 调用 {@link Streamer#current()} 时触发
 * @param onFinish 调用 {@link Streamer#finish()} 时触发
 * @since 4.0.0
 */
public record StreamableInterceptConfig<T>(
        @NonNull BiFunction<? super StreamerCancellation, ? super Streamer<? extends T>, ? extends Streamer<? extends T>> onStream,
        @NonNull BiFunction<? super StreamerCancellation, ? super CompletionStage<Boolean>, ? extends CompletionStage<Boolean>> onNext,
        @NonNull Function<? super T, ? extends T> onCurrent,
        @NonNull BiFunction<? super StreamerCancellation, ? super CompletionStage<Void>, ? extends CompletionStage<Void>> onFinish
) {

    /**
     * 构造仅自定义 {@link #onNext()} 拦截、其余为透传的配置。
     * @param onNext 拦截 {@code next()} 调用的回调
     */
    public StreamableInterceptConfig(
            @NonNull BiFunction<? super StreamerCancellation, ? super CompletionStage<Boolean>, ? extends CompletionStage<Boolean>> onNext) {
        this((_, v) -> v, onNext, v -> v, (_, v) -> v);
    }

    /**
     * 构造仅自定义 {@link #onCurrent()} 拦截、其余为透传的配置。
     * @param onCurrent 元素就绪时的回调
     */
    public StreamableInterceptConfig(@NonNull Function<? super T, ? extends T> onCurrent) {
        this((_, v) -> v, (_, v) -> v, onCurrent, (_, v) -> v);
    }

    /**
     * 构造完整配置 record。
     * @param onStream 调用 {@link Streamable#stream(StreamerCancellation)} 时触发
     * @param onNext 调用 {@link Streamer#next()} 时触发
     * @param onCurrent 调用 {@link Streamer#current()} 时触发
     * @param onFinish 调用 {@link Streamer#finish()} 时触发
     */
    public StreamableInterceptConfig {
        Objects.requireNonNull(onStream, "onStream is null");
        Objects.requireNonNull(onNext, "onNext is null");
        Objects.requireNonNull(onCurrent, "onCurrent is null");
        Objects.requireNonNull(onFinish, "onFinish is null");
    }
}
