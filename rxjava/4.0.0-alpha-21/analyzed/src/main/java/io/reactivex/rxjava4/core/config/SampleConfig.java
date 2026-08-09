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

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.functions.Consumer;
import io.reactivex.rxjava4.internal.functions.Functions;

/**
 * 定时 sample() 算子的配置 record。
 * @param <T> 被采样序列的元素类型
 * @param emitLast
 *            若为 {@code true} 且上游完成时仍有未采样的元素，则该元素会在完成前发射给下游；
 *            若为 {@code false}，则忽略未采样的最后一项。
 * @param onDropped
 *            当当前条目被新条目替换时调用
 * @since 4.0.0
 */
public record SampleConfig<T>(boolean emitLast, @NonNull Consumer<? super T> onDropped) {

    /**
     * 默认配置：不发射最后一项，onDropped 为空消费者。
     */
    public static final SampleConfig<Object> DEFAULT = new SampleConfig<>(false, Functions.emptyConsumer());
    /**
     * 默认配置：启用 emit last，onDropped 为空消费者。
     */
    public static final SampleConfig<Object> EMIT_LAST = new SampleConfig<>(true, Functions.emptyConsumer());

    /**
     * 使用给定 emit last 选项及空 onDropped 回调创建配置。
     * @param emitLast
     *            if {@code true} and the upstream completes while there is still an unsampled item available,
     *            that item is emitted to downstream before completion
     *            if {@code false}, an unsampled last item is ignored.
     */
    public SampleConfig(boolean emitLast) {
        this(emitLast, Functions.emptyConsumer());
    }

    /**
     * 使用给定 onDropped 回调且不发射最后一项创建配置。
     * @param onDropped
     *            当当前条目被新条目替换时调用
     */
    public SampleConfig(@NonNull Consumer<? super T> onDropped) {
        this(false, onDropped);
    }

    /**
     * 使用给定参数创建配置。
     * @param emitLast
     *            if {@code true} and the upstream completes while there is still an unsampled item available,
     *            that item is emitted to downstream before completion
     *            if {@code false}, an unsampled last item is ignored.
     * @param onDropped
     *            当当前条目被新条目替换时调用
     */
    public SampleConfig {
        Objects.requireNonNull(onDropped, "onDropped is null");
    }
}
